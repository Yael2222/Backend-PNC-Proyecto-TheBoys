package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.FacturaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.FacturaResponseDTO;
import com.example.backend_tallerautomotriz.entity.Factura;
import com.example.backend_tallerautomotriz.entity.OrdenTrabajo;
import com.example.backend_tallerautomotriz.enums.EstadoOrden;
import com.example.backend_tallerautomotriz.enums.EstadoPago;
import com.example.backend_tallerautomotriz.enums.MetodoPago;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.FacturaRepository;
import com.example.backend_tallerautomotriz.repository.OrdenTrabajoRepository;
import com.example.backend_tallerautomotriz.service.FacturaService;
import com.example.backend_tallerautomotriz.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {
    private final FacturaRepository repo;
    private final NotificacionService notificacionService;
    private final OrdenTrabajoRepository ordenRepo;

    @Override
    @Transactional(readOnly = true)
    public FacturaResponseDTO obtenerPorOrden(Integer ordenId) {
        return toDTO(repo.findByOrdenId(ordenId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada para orden: " + ordenId)));
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponseDTO obtenerPorId(Integer id) {
        return toDTO(repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> listarTodas() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> listarPorCliente(Integer clienteId) {
        return repo.findByOrdenClienteIdOrderByIdDesc(clienteId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public FacturaResponseDTO procesarPago(FacturaRequestDTO req) {
        if (req.getMetodoPago() == MetodoPago.STRIPE) {
            throw new BusinessRuleException("Los pagos Stripe deben procesarse en el endpoint de Stripe");
        }
        Factura factura = repo.findByOrdenIdForUpdate(req.getOrdenId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada para orden: " + req.getOrdenId()));
        if (factura.getEstadoPago() == EstadoPago.PAGADO) {
            throw new BusinessRuleException("La factura ya fue pagada");
        }
        factura.setMetodoPago(req.getMetodoPago());
        factura.setEstadoPago(EstadoPago.PAGADO);
        Factura guardada = repo.save(factura);

        OrdenTrabajo orden = factura.getOrden();
        if (orden.getEstado() == EstadoOrden.ESPERANDO_PAGO) {
            orden.setEstado(EstadoOrden.COMPLETADA);
            ordenRepo.save(orden);
        }

        return toDTO(guardada);
    }

    @Override
    @Transactional
    public FacturaResponseDTO solicitarPagoEfectivo(Integer facturaId) {
        Factura factura = repo.findById(facturaId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + facturaId));

        if (factura.getEstadoPago() == EstadoPago.PAGADO) {
            throw new BusinessRuleException("La factura ya está pagada");
        }

        if (factura.getEstadoPago() == EstadoPago.PENDIENTE_CONFIRMACION
                && factura.getMetodoPago() == MetodoPago.EFECTIVO) {
            return toDTO(factura);
        }

        factura.setMetodoPago(MetodoPago.EFECTIVO);
        factura.setEstadoPago(EstadoPago.PENDIENTE_CONFIRMACION);

        Factura guardada = repo.save(factura);

        OrdenTrabajo orden = factura.getOrden();

        if (orden.getMecanico() != null) {
            String clienteNombre =
                    orden.getCliente().getUsuario().getNombre() + " " +
                            orden.getCliente().getUsuario().getApellido();

            notificacionService.crear(
                    orden.getMecanico().getUsuario().getId(),
                    "El cliente " + clienteNombre + " indicó que pagará en efectivo la factura #"
                            + factura.getId()
                            + ". Dirígete a Órdenes Activas, selecciona la orden #"
                            + orden.getId()
                            + " y valida el pago cuando recibas el dinero en el taller.",
                    "PAGO_EFECTIVO_PENDIENTE",
                    factura.getId()
            );
        }

        return toDTO(guardada);
    }

    @Override
    @Transactional
    public FacturaResponseDTO confirmarPagoEfectivo(Integer facturaId) {
        Factura factura = repo.findById(facturaId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + facturaId));

        if (factura.getEstadoPago() == EstadoPago.PAGADO) {
            return toDTO(factura);
        }
        if (factura.getMetodoPago() != MetodoPago.EFECTIVO) {
            throw new BusinessRuleException("Solo se puede confirmar pago en efectivo para facturas marcadas como efectivo");
        }
        if (factura.getEstadoPago() != EstadoPago.PENDIENTE_CONFIRMACION) {
            throw new BusinessRuleException("La factura no está pendiente de confirmación de efectivo");
        }

        factura.setEstadoPago(EstadoPago.PAGADO);
        Factura guardada = repo.save(factura);

        OrdenTrabajo orden = factura.getOrden();
        if (orden.getEstado() == EstadoOrden.ESPERANDO_PAGO) {
            orden.setEstado(EstadoOrden.COMPLETADA);
            ordenRepo.save(orden);
        }

        notificacionService.crear(
                factura.getOrden().getCliente().getUsuario().getId(),
                "Tu pago en efectivo de la factura #" + factura.getId() + " fue confirmado correctamente.",
                "PAGO_CONFIRMADO",
                factura.getId()
        );

        return toDTO(guardada);
    }

    @Override
    @Transactional
    public FacturaResponseDTO confirmarPagoSeguro(Integer facturaId) {
        Factura factura = repo.findByIdForUpdate(facturaId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + facturaId));

        if (factura.getMetodoPago() != MetodoPago.SEGURO) {
            throw new BusinessRuleException("Esta factura no es de tipo seguro");
        }
        if (factura.getEstadoPago() == EstadoPago.PAGADO) {
            return toDTO(factura);
        }
        if (factura.getEstadoPago() != EstadoPago.PENDIENTE_CONFIRMACION) {
            throw new BusinessRuleException("La factura no está pendiente de confirmación");
        }

        factura.setEstadoPago(EstadoPago.PAGADO);
        Factura guardada = repo.save(factura);

        OrdenTrabajo orden = factura.getOrden();
        if (orden.getEstado() == EstadoOrden.ESPERANDO_PAGO) {
            orden.setEstado(EstadoOrden.COMPLETADA);
            ordenRepo.save(orden);
        }

        notificacionService.crear(
                factura.getOrden().getCliente().getUsuario().getId(),
                "El mecánico confirmó el pago del seguro para la factura #" + factura.getId(),
                "PAGO_SEGURO_CONFIRMADO",
                factura.getId()
        );

        return toDTO(guardada);
    }

    private FacturaResponseDTO toDTO(Factura factura) {
        return new FacturaResponseDTO(
                factura.getId(),
                factura.getOrden().getId(),
                factura.getOrden().getVehiculo().getPatente(),
                factura.getOrden().getFechaCreacion(),
                factura.getSubtotal(),
                factura.getImpuestos(),
                factura.getTotal(),
                factura.getEstadoPago().name(),
                factura.getMetodoPago() != null ? factura.getMetodoPago().name() : null);
    }
}