package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class CitaRequestDTO {
    @NotNull private Integer clienteId;
    @NotNull private Integer sucursalId;
    private Integer mecanicoId;
    @NotNull private LocalDate fecha;
    @NotNull private LocalTime hora;
}

