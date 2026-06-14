package com.example.backend_tallerautomotriz.entity;

import com.example.backend_tallerautomotriz.enums.CategoriaRepuesto;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "repuesto")
@Data @NoArgsConstructor @AllArgsConstructor
public class Repuesto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaRepuesto categoria;

    private String descripcion;
}
