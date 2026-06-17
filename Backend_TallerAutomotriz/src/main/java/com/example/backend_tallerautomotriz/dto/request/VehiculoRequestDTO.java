package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class VehiculoRequestDTO {
    @NotBlank(message = "La patente no puede estar vacía")
    @Pattern(
            regexp = "^[A-Za-z0-9\\-]{4,10}$",
            message = "La patente debe tener entre 4 y 10 caracteres alfanuméricos"
    )
    private String patente;
    @NotBlank(message = "La marca no puede estar vacía")
    @Size(max = 50, message = "La marca no puede superar 50 caracteres")
    private String marca;
    @NotBlank(message = "El modelo no puede estar vacío")
    @Size(max = 50, message = "El modelo no puede superar 50 caracteres")
    private String modelo;
    @NotNull(message = "El ID del cliente es requerido")
    private Integer clienteId;
}
