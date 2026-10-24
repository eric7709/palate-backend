package com.app.palate.auth.dto;

import java.util.List;

public record TokenDataDTO(String firstName, String email, List<String> roles) {
    
}
