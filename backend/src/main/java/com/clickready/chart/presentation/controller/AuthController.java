package com.clickready.chart.presentation.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.clickready.chart.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String username = request.getOrDefault("username", "testuser");
        String token = jwtTokenProvider.generateToken(username, List.of("ROLE_USER"));
        return Map.of(
                "token", token,
                "username", username,
                "type", "Bearer"
        );
    }
}