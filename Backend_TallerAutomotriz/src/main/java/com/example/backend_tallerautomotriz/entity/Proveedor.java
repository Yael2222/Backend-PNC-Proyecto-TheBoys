package com.example.backend_tallerautomotriz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedor")
@Data @NoArgsConstructor @AllArgsConstructor
public class Proveedor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    private String marca;
    private String contacto;
}
