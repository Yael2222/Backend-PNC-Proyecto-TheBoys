package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class RegistroHorasResponseDTO {
    private Integer id;
    private String mecanicoNombre;
    private Integer ordenId;
    private BigDecimal horasInvertidas;
    private LocalDate fechaRegistro;
}