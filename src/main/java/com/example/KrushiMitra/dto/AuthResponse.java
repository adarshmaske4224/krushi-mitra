package com.example.KrushiMitra.dto;

import lombok.*;

@Data @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
    private String role;
    private String preferredLanguage;
}