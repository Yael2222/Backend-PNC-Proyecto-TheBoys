package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class VehiculoRequestDTO {
    @NotBlank private String patente;
    @NotBlank private String marca;
    @NotBlank private String modelo;
    @NotNull  private Integer clienteId;
}
