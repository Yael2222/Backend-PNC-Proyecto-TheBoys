package com.example.backend_tallerautomotriz.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orden_repuesto")
@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenRepuesto {
    @EmbeddedId
    private OrdenRepuestoId id;

    @ManyToOne @MapsId("ordenId")
    @JoinColumn(name = "orden_id")
    private OrdenTrabajo orden;

    @ManyToOne @MapsId("repuestoId")
    @JoinColumn(name = "repuesto_id")
    private Repuesto repuesto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_aplicado", nullable = false)
    private BigDecimal precioAplicado;
}

