package com.example.backend_tallerautomotriz.dto.request;

import com.example.backend_tallerautomotriz.enums.NombreRol;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class CambiarRolRequestDTO {

    @NotNull(message = "El rol es obligatorio")
    private NombreRol nuevoRol; // "CLIENTE", "MECANICO", "ADMIN"

    private Integer sucursalId;
}

