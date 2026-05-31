package com.example.backend_tallerautomotriz.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/pagos") @RequiredArgsConstructor
public class StripeController {
    private final StripeService stripeService;

    @PostMapping("/stripe")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<StripePaymentResponseDTO> pagar(@Valid @RequestBody StripePaymentRequestDTO req) {
        return ResponseEntity.ok(stripeService.procesarPago(req));
    }
}
