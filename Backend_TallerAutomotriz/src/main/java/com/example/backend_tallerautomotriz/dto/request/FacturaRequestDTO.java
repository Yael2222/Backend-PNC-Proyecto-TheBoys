package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class FacturaRequestDTO {
    @NotNull  private Integer ordenId;
    @NotBlank private String metodoPago;
}
