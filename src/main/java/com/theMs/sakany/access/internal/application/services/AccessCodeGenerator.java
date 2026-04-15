package com.theMs.sakany.access.internal.application.services;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AccessCodeGenerator {
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    public String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return code.toString();
    }
}
