package com.skhueats.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender javaMailSender;

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("[SKHU Eats] 이메일 인증 코드 안내");
        message.setText(
                "안녕하세요. SKHU Eats입니다.\n\n" +
                        "이메일 인증 코드는 다음과 같습니다.\n\n" +
                        "인증 코드: " + code + "\n\n" +
                        "인증 코드는 일정 시간이 지나면 만료됩니다."
        );

        javaMailSender.send(message);
    }
}