package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenTrabajoRequestDTO {
    @NotBlank private String patente;
    @NotNull  private Integer clienteId;
    private Integer mecanicoId;
    private Integer sucursalId;
    @NotBlank private String tipoOrden;
    private String comentarios;
    @NotEmpty private List<OrdenServicioRequestDTO> servicios;
    private List<OrdenRepuestoRequestDTO> repuestos;
}