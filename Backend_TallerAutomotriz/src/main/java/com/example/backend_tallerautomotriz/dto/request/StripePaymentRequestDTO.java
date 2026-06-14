package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class StripePaymentRequestDTO {
    @NotNull  private Integer facturaId;
    @NotBlank private String token;
}
