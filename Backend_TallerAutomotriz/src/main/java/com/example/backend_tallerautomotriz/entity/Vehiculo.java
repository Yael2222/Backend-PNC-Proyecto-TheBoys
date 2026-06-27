package com.example.backend_tallerautomotriz.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehiculo")
@Data @NoArgsConstructor
public class Vehiculo {
    @Id
    private String patente;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "vehiculo", fetch = FetchType.LAZY)
    private List<OrdenTrabajo> ordenesTrabajos = new ArrayList<>();

    public Vehiculo(String patente, String marca, String modelo, Cliente cliente) {
        this.patente = patente;
        this.marca = marca;
        this.modelo = modelo;
        this.cliente = cliente;
    }
}
