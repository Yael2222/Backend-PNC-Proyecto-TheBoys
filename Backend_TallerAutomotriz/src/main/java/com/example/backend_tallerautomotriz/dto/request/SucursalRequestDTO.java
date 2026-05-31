package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class SucursalRequestDTO {
    @NotBlank private String nombre;
    @NotBlank private String direccion;
    @NotBlank private String departamento;
}
