package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.StripePaymentRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.StripePaymentResponseDTO;

public interface StripeService {
    StripePaymentResponseDTO procesarPago(StripePaymentRequestDTO request);
    String getPublishableKey();
}