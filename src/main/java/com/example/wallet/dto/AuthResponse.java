package com.example.wallet.dto;

import com.example.wallet.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "JWT and profile returned after login or register")
public class AuthResponse {

    @Schema(description = "Bearer token value (without 'Bearer ' prefix)", example = "eyJhbGciOiJIUzI1NiIs...")
    private String accessToken;

    @Schema(description = "Authorization scheme", example = "Bearer")
    private String tokenType;

    @Schema(description = "Lifetime of the access token in seconds", example = "3600")
    private long expiresInSeconds;

    @Schema(description = "Authenticated user id")
    private Long userId;

    @Schema(description = "Display name")
    private String name;

    @Schema(description = "Email address")
    private String email;

    @Schema(description = "Application role")
    private UserRole role;
}
