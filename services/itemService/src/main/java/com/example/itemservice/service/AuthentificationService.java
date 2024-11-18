package com.example.itemservice.service;

import com.example.itemservice.domain.dto.JwtAuthenticationResponseDto;
import com.example.itemservice.domain.dto.SignInRequest;
import com.example.itemservice.domain.dto.SignUpRequest;

public interface AuthentificationService {


    JwtAuthenticationResponseDto signUp(SignUpRequest request);

    JwtAuthenticationResponseDto signIn(SignInRequest request);

    void logout(JwtAuthenticationResponseDto jwtAuthenticationResponseDto);

}
