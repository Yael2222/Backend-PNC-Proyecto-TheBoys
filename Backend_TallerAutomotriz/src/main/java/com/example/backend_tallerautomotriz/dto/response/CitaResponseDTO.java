package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class CitaResponseDTO {
    private Integer id;
    private String clienteNombre;
    private String sucursalNombre;
    private String mecanicoNombre;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
}