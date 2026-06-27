package com.example.backend_tallerautomotriz.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class NotificacionResponseDTO {
    private Integer id;
    private String mensaje;
    private boolean leida;
    private LocalDateTime fechaCreacion;
    private String tipo;
    private Integer referenciaId;
}
