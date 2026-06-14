package com.example.backend_tallerautomotriz.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "registro_horas")
@Data @NoArgsConstructor @AllArgsConstructor
public class RegistroHoras {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "mecanico_id", nullable = false)
    private Mecanico mecanico;

    @ManyToOne
    @JoinColumn(name = "orden_id", nullable = false)
    private OrdenTrabajo orden;

    @Column(name = "horas_invertidas", nullable = false)
    private BigDecimal horasInvertidas;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro = LocalDate.now();
}
