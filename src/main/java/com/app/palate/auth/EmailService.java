package com.app.palate.auth;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    public void sendVerificationEmail(String email, String token) {
        String link = "http://localhost:8080/palate/auth/verify-email?token=" + token;
        log.info("=================================================");
        log.info("TO: {}", email);
        log.info("SUBJECT: Verify Your Cravings Account");
        log.info("BODY: Click here to verify your email: {}", link);
        log.info("=================================================");
    }

    public void sendPasswordResetEmail(String email, String token) {
        String link = "http://localhost:8080/palate/auth/reset-password?token=" + token;
        log.info("=================================================");
        log.info("TO: {}", email);
        log.info("SUBJECT: Reset Your Password Request");
        log.info("BODY: Click here to reset your account password: {}", link);
        log.info("=================================================");
    }
}