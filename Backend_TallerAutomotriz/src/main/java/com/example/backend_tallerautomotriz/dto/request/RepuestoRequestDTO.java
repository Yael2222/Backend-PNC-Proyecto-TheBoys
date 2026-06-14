package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class RepuestoRequestDTO {
    @NotNull                    private Integer proveedorId;
    @NotBlank                   private String nombre;
    @NotNull @DecimalMin("0.0") private BigDecimal precioUnitario;
    @NotBlank                   private String categoria;
    private String descripcion;
}
