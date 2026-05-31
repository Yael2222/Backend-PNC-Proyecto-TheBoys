package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class SucursalResponseDTO {
    private Integer id;
    private String nombre;
    private String direccion;
    private String departamento;
}