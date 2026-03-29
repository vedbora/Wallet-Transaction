package com.example.wallet.security;

import com.example.wallet.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityProblemWriter {

    private final ObjectMapper objectMapper;

    public void writeJson(HttpServletRequest request, HttpServletResponse response, int status,
                          String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        List<String> empty = Collections.emptyList();
        ApiError body = new ApiError(
                Instant.now(),
                status,
                error,
                message,
                request.getRequestURI(),
                empty);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
