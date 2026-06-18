package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.FacturaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.FacturaResponseDTO;
import com.example.backend_tallerautomotriz.entity.Factura;
import com.example.backend_tallerautomotriz.enums.EstadoPago;
import com.example.backend_tallerautomotriz.enums.MetodoPago;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.FacturaRepository;
import com.example.backend_tallerautomotriz.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {
    private final FacturaRepository repo;

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
        return toDTO(repo.save(factura));
    }

    private FacturaResponseDTO toDTO(Factura factura) {
        return new FacturaResponseDTO(
                factura.getId(),
                factura.getOrden().getId(),
                factura.getSubtotal(),
                factura.getImpuestos(),
                factura.getTotal(),
                factura.getEstadoPago().name(),
                factura.getMetodoPago() != null ? factura.getMetodoPago().name() : null);
    }
}
