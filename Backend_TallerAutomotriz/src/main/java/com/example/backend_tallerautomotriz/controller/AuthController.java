package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.LoginRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.RegisterRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.AuthResponseDTO;
import com.example.backend_tallerautomotriz.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }
}
