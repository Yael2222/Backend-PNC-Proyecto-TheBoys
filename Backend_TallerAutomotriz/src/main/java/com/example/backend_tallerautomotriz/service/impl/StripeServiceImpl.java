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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeServiceImpl.class);

    private final FacturaRepository facturaRepo;

    @Value("${stripe.api-key}")
    private String stripeKey;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @PostConstruct
    public void init() {
        stripeKey = sanitize(stripeKey);
        publishableKey = sanitize(publishableKey);

        if (stripeKey.isEmpty() || publishableKey.isEmpty()) {
            log.warn("[Stripe] Claves NO configuradas. Define STRIPE_SECRET_KEY (sk_...) y " +
                    "STRIPE_PUBLISHABLE_KEY (pk_...). El pago en línea estará deshabilitado.");
        } else {
            if (!stripeKey.startsWith("sk_")) {
                log.error("[Stripe] STRIPE_SECRET_KEY no parece una clave secreta (debe empezar con 'sk_'). " +
                        "¿Invertiste las claves pública y secreta?");
            }
            if (!publishableKey.startsWith("pk_")) {
                log.error("[Stripe] STRIPE_PUBLISHABLE_KEY no parece una clave pública (debe empezar con 'pk_'). " +
                        "¿Invertiste las claves pública y secreta? El formulario de tarjeta no cargará en el frontend.");
            }
            if (stripeKey.startsWith("sk_") && publishableKey.startsWith("pk_")) {
                String modo = stripeKey.startsWith("sk_test_") ? "TEST" : "LIVE";
                log.info("[Stripe] Configuración cargada correctamente en modo {}.", modo);
            }
        }

        Stripe.apiKey = stripeKey;
    }

    private static String sanitize(String value) {
        if (value == null) return "";
        return value.trim().replaceAll("^['\"]|['\"]$", "");
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