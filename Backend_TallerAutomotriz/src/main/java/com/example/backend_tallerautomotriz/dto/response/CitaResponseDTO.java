package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class CitaResponseDTO {
    private Integer id;
    private String clienteNombre;
    private Integer clienteId;
    private String sucursalNombre;
    private Integer sucursalId;
    private String mecanicoNombre;
    private Integer mecanicoId;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
    private List<String> servicios;
    private LocalDate nuevaFechaPropuesta;
    private LocalTime nuevaHoraPropuesta;
}
