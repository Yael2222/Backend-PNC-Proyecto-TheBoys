package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenTrabajoResponseDTO {
    private Integer id;
    private String patente;
    private String clienteNombre;
    private String mecanicoNombre;
    private String tipoOrden;
    private String estado;
    private LocalDate fechaCreacion;
    private String comentarios;
    private List<OrdenServicioResponseDTO> servicios;
    private List<OrdenRepuestoResponseDTO> repuestos;
}