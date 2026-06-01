package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class ServicioRequestDTO {
    @NotBlank                    private String nombre;
    private String descripcion;
    @NotNull @Min(1)             private Integer tiempoEstimadoMinutos;
    @NotNull @DecimalMin("0.0")  private BigDecimal precioBase;
}
