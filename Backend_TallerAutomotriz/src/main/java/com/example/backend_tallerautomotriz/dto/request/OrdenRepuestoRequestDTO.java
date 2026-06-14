package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenRepuestoRequestDTO {
    @NotNull              private Integer repuestoId;
    @NotNull @Min(1)      private Integer cantidad;
    @NotNull              private BigDecimal precioAplicado;
}

