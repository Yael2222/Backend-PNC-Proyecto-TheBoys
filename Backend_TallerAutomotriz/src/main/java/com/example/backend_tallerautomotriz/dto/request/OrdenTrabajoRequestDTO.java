package com.example.backend_tallerautomotriz.dto.request;

import com.example.backend_tallerautomotriz.enums.TipoOrden;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenTrabajoRequestDTO {
    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^[A-Za-z0-9-]+$")
    private String patente;

    @NotNull
    @Positive
    private Integer clienteId;

    @Positive
    private Integer mecanicoId;

    @Positive
    private Integer sucursalId;

    @NotNull
    private TipoOrden tipoOrden;

    @Size(max = 1000)
    private String comentarios;

    @NotEmpty
    @Valid
    private List<OrdenServicioRequestDTO> servicios;

    @Valid
    private List<OrdenRepuestoRequestDTO> repuestos;
}