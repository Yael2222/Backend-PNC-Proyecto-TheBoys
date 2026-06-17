package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class ReprogramarCitaRequestDTO {

    @NotNull(message = "La nueva fecha es obligatoria")
    @Future(message = "La nueva fecha debe ser futura")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora es obligatoria")
    private LocalTime nuevaHora;
}
