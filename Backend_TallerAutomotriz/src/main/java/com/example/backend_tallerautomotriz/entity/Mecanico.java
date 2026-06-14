package com.example.backend_tallerautomotriz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mecanico")
@Data @NoArgsConstructor @AllArgsConstructor
public class Mecanico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;
}
