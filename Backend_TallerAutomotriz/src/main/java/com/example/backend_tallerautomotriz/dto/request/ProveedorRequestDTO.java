package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ProveedorRequestDTO {
    @NotBlank private String nombre;
    @NotBlank private String marca;
    @NotBlank private String contacto;
}