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
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {
    private final FacturaRepository repo;

    @Override public FacturaResponseDTO obtenerPorOrden(Integer ordenId) {
        return toDTO(repo.findByOrdenId(ordenId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada para orden: " + ordenId)));
    }
    @Override public FacturaResponseDTO obtenerPorId(Integer id) {
        return toDTO(repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + id)));
    }
    @Override public List<FacturaResponseDTO> listarTodas() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public FacturaResponseDTO procesarPago(FacturaRequestDTO req) {
        Factura f = repo.findByOrdenId(req.getOrdenId())
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada para orden: " + req.getOrdenId()));
        if (f.getEstadoPago() == EstadoPago.PAGADO)
            throw new BusinessRuleException("La factura ya fue pagada");
        f.setMetodoPago(MetodoPago.valueOf(req.getMetodoPago().toUpperCase()));
        f.setEstadoPago(EstadoPago.PAGADO);
        return toDTO(repo.save(f));
    }
    private FacturaResponseDTO toDTO(Factura f) {
        return new FacturaResponseDTO(f.getId(), f.getOrden().getId(), f.getSubtotal(),
                f.getImpuestos(), f.getTotal(), f.getEstadoPago().name(),
                f.getMetodoPago() != null ? f.getMetodoPago().name() : null);
    }
}
