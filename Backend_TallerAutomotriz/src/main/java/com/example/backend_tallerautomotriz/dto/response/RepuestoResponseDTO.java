package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class RepuestoResponseDTO {
    private Integer id;
    private String nombre;
    private BigDecimal precioUnitario;
    private String proveedor;
    private Integer proveedorId;
    private String categoria;
    private String descripcion;
}
