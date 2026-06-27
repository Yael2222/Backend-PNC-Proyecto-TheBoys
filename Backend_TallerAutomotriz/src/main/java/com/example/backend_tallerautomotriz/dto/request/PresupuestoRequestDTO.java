package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoRequestDTO {

    @NotNull(message = "El presupuesto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El presupuesto debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal presupuestoTotal;

    @FutureOrPresent(message = "La fecha estimada no puede ser anterior a hoy")
    private LocalDate fechaFinalizacionEstimada;

    @Size(max = 1000, message = "Los comentarios no pueden superar 1000 caracteres")
    private String comentarios;

    /*
     * Opcional: servicios adicionales que el mecánico agrega al presupuesto.
     * No deben incluir servicios que ya existen en la orden.
     */
    @Valid
    private List<OrdenServicioRequestDTO> servicios;

    /*
     * Opcional: repuestos adicionales que el mecánico usará.
     * Se validan contra inventario de la sucursal en OrdenTrabajoServiceImpl.
     */
    @Valid
    private List<OrdenRepuestoRequestDTO> repuestos;
}

