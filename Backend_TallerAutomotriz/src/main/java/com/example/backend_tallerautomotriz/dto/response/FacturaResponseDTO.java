package com.example.backend_tallerautomotriz.dto.response;


import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class FacturaResponseDTO {
    private Integer id;
    private Integer ordenId;
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal total;
    private String estadoPago;
    private String metodoPago;
}
