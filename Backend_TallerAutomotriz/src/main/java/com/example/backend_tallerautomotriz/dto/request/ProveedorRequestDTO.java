package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ProveedorRequestDTO {
    @NotBlank
    @Size(max = 150)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    private String marca;

    @NotBlank
    @Size(max = 254)
    private String contacto;
}