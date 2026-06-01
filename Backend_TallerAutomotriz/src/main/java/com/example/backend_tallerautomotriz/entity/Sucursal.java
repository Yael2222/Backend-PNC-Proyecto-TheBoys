package com.example.backend_tallerautomotriz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sucursal")
@Data @NoArgsConstructor @AllArgsConstructor
public class Sucursal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    private String direccion;
    private String departamento;
}
