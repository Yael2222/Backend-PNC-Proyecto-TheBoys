package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class PresupuestoRequestDTO {

    @NotNull(message = "El presupuesto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El presupuesto debe ser mayor a cero")
    private BigDecimal presupuestoTotal;

    private LocalDate fechaFinalizacionEstimada;

    private String comentarios;
}
