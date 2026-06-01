package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenServicioRequestDTO {
    @NotNull                    private Integer servicioId;
    @NotNull @DecimalMin("0.0") private BigDecimal precioAplicado;
}