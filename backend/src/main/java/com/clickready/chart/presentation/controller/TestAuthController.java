package com.clickready.chart.presentation.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.clickready.chart.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestAuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/token")
    public String generateToken() {
        return jwtTokenProvider.generateToken("testuser", List.of("ROLE_USER"));
    }
}