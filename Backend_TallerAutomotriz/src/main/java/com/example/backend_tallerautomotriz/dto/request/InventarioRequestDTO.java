package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class InventarioRequestDTO {

    @NotNull
    @Positive
    private Integer sucursalId;

    @NotNull
    @Positive
    private Integer repuestoId;

    @NotNull
    @PositiveOrZero
    private Integer stockTotal;
}