package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class MecanicoRequestDTO {
    @NotNull private Integer usuarioId;
    @NotNull private Integer sucursalId;
}
