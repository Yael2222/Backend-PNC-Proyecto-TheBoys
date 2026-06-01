package com.example.backend_tallerautomotriz.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class OrdenServicioId implements Serializable {
    private Integer ordenId;
    private Integer servicioId;
}
