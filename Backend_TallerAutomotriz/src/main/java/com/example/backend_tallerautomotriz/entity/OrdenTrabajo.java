package com.example.backend_tallerautomotriz.entity;

import com.example.backend_tallerautomotriz.enums.EstadoOrden;
import com.example.backend_tallerautomotriz.enums.TipoOrden;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orden_trabajo")
@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenTrabajo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patente", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "mecanico_id")
    private Mecanico mecanico;

    @ManyToOne
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_orden")
    private TipoOrden tipoOrden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado = EstadoOrden.PENDIENTE;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion = LocalDate.now();

    @Column(name = "fecha_finalizacion_estimada")
    private LocalDate fechaFinalizacionEstimada;

    private String comentarios;

    @Column(name = "presupuesto_total", precision = 12, scale = 2)
    private BigDecimal presupuestoTotal;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenServicio> servicios = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenRepuesto> repuestos = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL)
    private List<RegistroHoras> registroHoras = new ArrayList<>();

    @OneToOne(mappedBy = "orden", cascade = CascadeType.ALL)
    private Factura factura;
}
