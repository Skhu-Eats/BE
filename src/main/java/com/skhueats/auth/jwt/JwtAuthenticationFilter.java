package com.skhueats.auth.jwt;

import com.skhueats.global.exception.ErrorCode;
import com.skhueats.global.security.SecurityErrorResponseWriter;
import com.skhueats.user.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",
            "/auth/send-code",
            "/auth/password/reset",
            "/auth/password/reset/send-code",
            "/auth/password/reset/verify-code",
            "/auth/verify-code",
            "/auth/login",
            "/auth/refresh",
            "/auth/logout",
            "/auth/check-nickname",
            "/error"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return HttpMethod.OPTIONS.matches(request.getMethod())
                || PUBLIC_PATHS.contains(path)
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtTokenProvider.getEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            securityErrorResponseWriter.write(request, response, ErrorCode.EXPIRED_TOKEN);
            return;
        } catch (JwtException | IllegalArgumentException e) {
            securityErrorResponseWriter.write(request, response, ErrorCode.INVALID_TOKEN);
            return;
        } catch (UsernameNotFoundException e) {
            securityErrorResponseWriter.write(request, response, ErrorCode.USER_NOT_FOUND);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }
}
