package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ProveedorResponseDTO {
    private Integer id;
    private String nombre;
    private String marca;
    private String contacto;
}