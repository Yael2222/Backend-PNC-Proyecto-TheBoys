package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class InventarioRequestDTO {
    @NotNull         private Integer sucursalId;
    @NotNull         private Integer repuestoId;
    @NotNull @Min(0) private Integer stockTotal;
}