package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class MecanicoResponseDTO {
    private Integer id;
    private Integer usuarioId;
    private String nombre;
    private String apellido;
    private String email;
    private Integer sucursalId;
    private String sucursal;
    private BigDecimal horasTrabajadas;
}
