package com.example.backend_tallerautomotriz.entity;

import com.example.backend_tallerautomotriz.enums.EstadoCita;
import com.example.backend_tallerautomotriz.enums.TipoOrden;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cita")
@Data @NoArgsConstructor @AllArgsConstructor
public class Cita {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @ManyToOne
    @JoinColumn(name = "mecanico_id")
    private Mecanico mecanico;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado = EstadoCita.PROGRAMADA;

    @ManyToMany
    @JoinTable(
            name = "cita_servicio",
            joinColumns = @JoinColumn(name = "cita_id"),
            inverseJoinColumns = @JoinColumn(name = "servicio_id")
    )
    private List<Servicio> servicios = new ArrayList<>();

    @Column(name = "nueva_fecha_propuesta")
    private LocalDate nuevaFechaPropuesta;

    @Column(name = "nueva_hora_propuesta")
    private LocalTime nuevaHoraPropuesta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_orden")
    private TipoOrden tipoOrden = TipoOrden.ESTANDAR;

    @ManyToOne
    @JoinColumn(name = "factura_garantia_id")
    private Factura facturaGarantia;
}
