package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ClienteRequestDTO {
    @NotNull  private Integer usuarioId;
    @NotBlank private String telefono;
}
