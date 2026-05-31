package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class InventarioResponseDTO {
    private Integer id;
    private String sucursal;
    private String repuesto;
    private Integer stockTotal;
    private LocalDate fechaActualizacion;
}