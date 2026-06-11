package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenTrabajoResponseDTO {
    private Integer id;
    private String patente;
    private String clienteNombre;
    private String mecanicoNombre;
    private Integer mecanicoId;
    private String sucursalNombre;
    private Integer sucursalId;
    private String tipoOrden;
    private String estado;
    private LocalDate fechaCreacion;
    private LocalDate fechaFinalizacionEstimada;
    private String comentarios;
    private BigDecimal presupuestoTotal;
    private List<OrdenServicioResponseDTO> servicios;
    private List<OrdenRepuestoResponseDTO> repuestos;
}
