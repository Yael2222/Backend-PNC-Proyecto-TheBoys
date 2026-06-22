package com.example.backend_tallerautomotriz.dto.request;

import com.example.backend_tallerautomotriz.enums.MetodoPago;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class FacturaRequestDTO {

    @NotNull
    @Positive
    private Integer ordenId;

    @NotNull
    private MetodoPago metodoPago;
}
