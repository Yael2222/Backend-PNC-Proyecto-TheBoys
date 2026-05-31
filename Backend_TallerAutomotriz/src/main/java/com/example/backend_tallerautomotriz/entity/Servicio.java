package com.example.backend_tallerautomotriz.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "servicio")
@Data @NoArgsConstructor @AllArgsConstructor
public class Servicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "tiempo_estimado_minutos")
    private Integer tiempoEstimadoMinutos;

    @Column(name = "precio_base", nullable = false)
    private BigDecimal precioBase;

    @Enumerated(EnumType.STRING)
    private EstadoServicio estado = EstadoServicio.ACTIVO;
}
