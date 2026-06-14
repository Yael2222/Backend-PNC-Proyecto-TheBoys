package com.example.backend_tallerautomotriz.service;


import com.example.backend_tallerautomotriz.dto.request.LoginRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.RegisterRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO register(RegisterRequestDTO request);
}
