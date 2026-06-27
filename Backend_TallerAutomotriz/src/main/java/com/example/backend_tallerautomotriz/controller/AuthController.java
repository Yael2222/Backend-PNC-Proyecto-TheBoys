package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.LoginRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.RegisterRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.AuthResponseDTO;
import com.example.backend_tallerautomotriz.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @PreAuthorize("#request.rol == T(com.example.backend_tallerautomotriz.enums.NombreRol).CLIENTE or hasRole('ADMIN')")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}
