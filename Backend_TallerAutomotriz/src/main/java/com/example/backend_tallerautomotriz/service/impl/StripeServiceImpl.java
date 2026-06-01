package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.StripePaymentRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.StripePaymentResponseDTO;
import com.example.backend_tallerautomotriz.entity.Factura;
import com.example.backend_tallerautomotriz.enums.EstadoPago;
import com.example.backend_tallerautomotriz.enums.MetodoPago;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.FacturaRepository;
import com.example.backend_tallerautomotriz.service.StripeService;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {
    private final FacturaRepository facturaRepo;

    @Value("${stripe.api-key}")
    private String stripeKey;

    @PostConstruct
    public void init() { Stripe.apiKey = stripeKey; }

    @Override
    public StripePaymentResponseDTO procesarPago(StripePaymentRequestDTO req) {
        Factura f = facturaRepo.findById(req.getFacturaId())
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + req.getFacturaId()));
        if (f.getEstadoPago() == EstadoPago.PAGADO)
            throw new BusinessRuleException("La factura ya fue pagada");
        try {
            ChargeCreateParams params = ChargeCreateParams.builder()
                    .setAmount(f.getTotal().multiply(java.math.BigDecimal.valueOf(100)).longValue())
                    .setCurrency("usd")
                    .setSource(req.getToken())
                    .setDescription("Pago factura #" + f.getId() + " - Taller Automotriz")
                    .build();
            Charge charge = Charge.create(params);
            f.setEstadoPago(EstadoPago.PAGADO);
            f.setMetodoPago(MetodoPago.STRIPE);
            facturaRepo.save(f);
            return new StripePaymentResponseDTO(charge.getId(), "EXITOSO", "Pago procesado correctamente");
        } catch (Exception e) {
            throw new BusinessRuleException("Error procesando pago Stripe: " + e.getMessage());
        }
    }
}
