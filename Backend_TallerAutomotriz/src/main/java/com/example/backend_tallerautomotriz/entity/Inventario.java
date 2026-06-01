package com.example.backend_tallerautomotriz.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "inventario")
@Data @NoArgsConstructor @AllArgsConstructor
public class Inventario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @ManyToOne
    @JoinColumn(name = "repuesto_id", nullable = false)
    private Repuesto repuesto;

    @Column(name = "stock_total", nullable = false)
    private Integer stockTotal;

    @Column(name = "fecha_actualizacion")
    private LocalDate fechaActualizacion = LocalDate.now();
}
