package com.skhueats.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.skhueats.global.response.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                      HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse(ErrorCode.INVALID_REQUEST.getMessage());

        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_REQUEST, message, request.getRequestURI());
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e,
                                                                   HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_REQUEST, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_REQUEST, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus()).body(response);
    }
}