package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class RegisterRequestDTO {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;
    @NotBlank
    private String rol; // ADMIN | MECANICO | CLIENTE
}