package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Integer id;
    private String email;
    private String rol;
    private String nombre;
    private String apellido;
}