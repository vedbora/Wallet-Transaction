package com.example.wallet.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * HS256 signing key (UTF-8); must be at least 256 bits (32+ chars) for JJWT.
     */
    @NotBlank
    private String secret;

    /**
     * Access token lifetime in milliseconds (default 1 hour).
     */
    @Positive
    private long expirationMs = 3_600_000L;
}
