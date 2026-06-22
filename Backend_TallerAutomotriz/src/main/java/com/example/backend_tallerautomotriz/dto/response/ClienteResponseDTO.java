package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ClienteResponseDTO {
    private Integer id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
}
