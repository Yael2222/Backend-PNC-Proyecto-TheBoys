package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class RegistroHorasRequestDTO {
    @NotNull              private Integer mecanicoId;
    @NotNull              private Integer ordenId;
    @NotNull @DecimalMin("0.1") private BigDecimal horasInvertidas;
}

