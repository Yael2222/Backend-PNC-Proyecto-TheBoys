package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenRepuestoRequestDTO {
    @NotNull
    @Positive
    private Integer repuestoId;

    @NotNull
    @Positive
    private Integer cantidad;

    @NotNull
    @DecimalMin("0.0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal precioAplicado;
}

