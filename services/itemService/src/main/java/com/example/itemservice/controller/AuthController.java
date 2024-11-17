package com.example.itemservice.controller;

import com.example.itemservice.domain.dto.JwtAuthenticationResponseDto;
import com.example.itemservice.domain.dto.SignInRequest;
import com.example.itemservice.domain.dto.SignUpRequest;
import com.example.itemservice.service.AuthenticationServiceData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationServiceData authenticationServiceData;

    @PostMapping("/sign-up")
    public JwtAuthenticationResponseDto signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationServiceData.signUp(request);
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponseDto signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationServiceData.signIn(request);
    }

    @PostMapping("/auth_logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        authenticationServiceData.logout(new JwtAuthenticationResponseDto(token));
        return ResponseEntity.ok("Logged out successfully!");
    }

}
