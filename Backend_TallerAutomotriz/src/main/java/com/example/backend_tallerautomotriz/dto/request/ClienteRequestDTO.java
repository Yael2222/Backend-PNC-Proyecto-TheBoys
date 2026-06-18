package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ClienteRequestDTO {

    @NotNull
    @Positive
    private Integer usuarioId;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^[0-9+() -]{7,20}$")
    private String telefono;
}
