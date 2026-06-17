package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class CitaRequestDTO {

    @NotNull
    @Positive
    private Integer clienteId;

    @NotNull
    @Positive
    private Integer sucursalId;

    @Positive
    private Integer mecanicoId;

    @NotNull
    @FutureOrPresent
    private LocalDate fecha;

    @NotNull
    private LocalTime hora;

    @Size(max = 50)
    private List<@Positive Integer> servicioIds;
}
