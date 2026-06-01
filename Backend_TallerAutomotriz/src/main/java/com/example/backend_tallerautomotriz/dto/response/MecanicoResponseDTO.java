package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class MecanicoResponseDTO {
    private Integer id;
    private String nombre;
    private String apellido;
    private String email;
    private String sucursal;
}

