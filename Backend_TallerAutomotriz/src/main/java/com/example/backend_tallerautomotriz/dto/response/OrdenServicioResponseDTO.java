package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenServicioResponseDTO {
    private Integer servicioId;
    private String nombreServicio;
    private BigDecimal precioAplicado;
}
