package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class VehiculoResponseDTO {
    private String patente;
    private String marca;
    private String modelo;
    private String clienteNombre;
}
