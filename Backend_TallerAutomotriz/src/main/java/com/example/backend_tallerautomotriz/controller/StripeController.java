package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.StripePaymentRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.StripeConfigResponseDTO;
import com.example.backend_tallerautomotriz.dto.response.StripePaymentResponseDTO;
import com.example.backend_tallerautomotriz.service.StripeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class StripeController {

    private final StripeService stripeService;

    @GetMapping("/stripe/config")
    public ResponseEntity<StripeConfigResponseDTO> obtenerConfig() {
        return ResponseEntity.ok(new StripeConfigResponseDTO(stripeService.getPublishableKey()));
    }

    @PostMapping("/stripe")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') " +
            "and @tallerAuthorization.esFacturaDelCliente(authentication, #req.facturaId))")
    public ResponseEntity<StripePaymentResponseDTO> pagar(@Valid @RequestBody StripePaymentRequestDTO req) {
        return ResponseEntity.ok(stripeService.procesarPago(req));
    }
}