package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class InventarioResponseDTO {
    private Integer id;
    private Integer sucursalId;
    private String sucursal;
    private Integer repuestoId;
    private String repuesto;
    private String categoria;
    private BigDecimal precioUnitario;
    private Integer stockTotal;
    private LocalDate fechaActualizacion;
}
