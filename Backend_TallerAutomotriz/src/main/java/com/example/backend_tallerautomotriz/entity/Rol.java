package com.example.backend_tallerautomotriz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol")
@Data @NoArgsConstructor @AllArgsConstructor
public class Rol {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private NombreRol nombre;
}