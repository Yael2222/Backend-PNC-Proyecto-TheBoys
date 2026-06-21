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
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    private final FacturaRepository facturaRepo;

    @Value("${stripe.api-key}")
    private String stripeKey;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeKey;
    }

    @Override
    public String getPublishableKey() {
        return publishableKey;
    }

    @Override
    @Transactional
    public StripePaymentResponseDTO procesarPago(StripePaymentRequestDTO req) {
        Factura f = facturaRepo.findByIdForUpdate(req.getFacturaId())
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + req.getFacturaId()));

        if (f.getEstadoPago() == EstadoPago.PAGADO) {
            throw new BusinessRuleException("La factura ya fue pagada");
        }

        long montoEnCentavos = f.getTotal()
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        Charge charge;
        try {
            ChargeCreateParams params = ChargeCreateParams.builder()
                    .setAmount(montoEnCentavos)
                    .setCurrency("usd")
                    .setSource(req.getToken())
                    .setDescription("Pago factura #" + f.getId() + " - Taller Automotriz")
                    .build();
            charge = Charge.create(params);
        } catch (StripeException e) {
            // Error real de Stripe (tarjeta rechazada, token inválido, etc.)
            throw new BusinessRuleException("El pago fue rechazado por Stripe: " + e.getMessage());
        }

        f.setEstadoPago(EstadoPago.PAGADO);
        f.setMetodoPago(MetodoPago.STRIPE);
        facturaRepo.save(f);

        return new StripePaymentResponseDTO(charge.getId(), "EXITOSO", "Pago procesado correctamente");
    }
}