package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class CambiarRolRequestDTO {

    @NotBlank(message = "El rol es obligatorio")
    private String nuevoRol; // "CLIENTE", "MECANICO", "ADMIN"

    private Integer sucursalId;
}

