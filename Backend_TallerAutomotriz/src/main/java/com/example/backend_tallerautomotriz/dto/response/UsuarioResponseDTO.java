package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UsuarioResponseDTO {
    private Integer id;
    private String email;
    private String nombre;
    private String apellido;
    private String rol;
    private boolean bloqueado;
}