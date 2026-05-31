package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String email;
    private String rol;
    private String nombre;
}