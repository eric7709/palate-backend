package com.app.palate.auth;

import java.util.List;

public record TokenDataDTO(String firstName, String email, List<String> roles) {
    
}
