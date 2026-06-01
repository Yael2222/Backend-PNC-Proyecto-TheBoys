package com.example.backend_tallerautomotriz.entity;

import com.example.backend_tallerautomotriz.enums.EstadoPago;
import com.example.backend_tallerautomotriz.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "factura")
@Data @NoArgsConstructor @AllArgsConstructor
public class Factura {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "orden_id", nullable = false, unique = true)
    private OrdenTrabajo orden;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal impuestos;

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago")
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago;
}

