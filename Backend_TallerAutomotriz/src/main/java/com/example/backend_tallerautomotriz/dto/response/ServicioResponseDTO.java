package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class ServicioResponseDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private Integer tiempoEstimadoMinutos;
    private BigDecimal precioBase;
    private String estado;
}

