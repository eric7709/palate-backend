package com.app.palate.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class QrCodeService {

    // Character pool for generating the token (Numbers + Uppercase letters)
    private static final String CHAR_POOL = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int TOKEN_LENGTH = 7;
    private final SecureRandom random = new SecureRandom();

    public String generateRandomQrCode() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int randomIndex = random.nextInt(CHAR_POOL.length());
            token.append(CHAR_POOL.charAt(randomIndex));
        }
        
        // This will produce a neat 7-character value like: X7R9W2B
        return token.toString();
    }
}