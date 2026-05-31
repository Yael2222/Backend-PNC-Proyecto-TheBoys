package com.example.backend_tallerautomotriz.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orden_servicio")
@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenServicio {
    @EmbeddedId
    private OrdenServicioId id;

    @ManyToOne @MapsId("ordenId")
    @JoinColumn(name = "orden_id")
    private OrdenTrabajo orden;

    @ManyToOne @MapsId("servicioId")
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @Column(name = "precio_aplicado", nullable = false)
    private BigDecimal precioAplicado;
}
