package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenRepuestoResponseDTO {
    private Integer repuestoId;
    private String nombreRepuesto;
    private Integer cantidad;
    private BigDecimal precioAplicado;
}