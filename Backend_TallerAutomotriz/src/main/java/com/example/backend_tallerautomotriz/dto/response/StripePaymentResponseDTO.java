package com.example.backend_tallerautomotriz.dto.response;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class StripePaymentResponseDTO {
    private String chargeId;
    private String estado;
    private String mensaje;
}
