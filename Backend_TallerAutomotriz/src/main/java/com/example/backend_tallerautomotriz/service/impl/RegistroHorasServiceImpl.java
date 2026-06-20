package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.RegistroHorasRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.RegistroHorasResponseDTO;
import com.example.backend_tallerautomotriz.entity.Mecanico;
import com.example.backend_tallerautomotriz.entity.OrdenTrabajo;
import com.example.backend_tallerautomotriz.entity.RegistroHoras;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.FacturaRepository;
import com.example.backend_tallerautomotriz.repository.MecanicoRepository;
import com.example.backend_tallerautomotriz.repository.OrdenTrabajoRepository;
import com.example.backend_tallerautomotriz.repository.RegistroHorasRepository;
import com.example.backend_tallerautomotriz.service.RegistroHorasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistroHorasServiceImpl implements RegistroHorasService {
    private static final BigDecimal MAX_HORAS_POR_DIA = new BigDecimal("24.0");
    private final RegistroHorasRepository repo;
    private final MecanicoRepository mecanicoRepo;
    private final OrdenTrabajoRepository ordenRepo;
    private final FacturaRepository facturaRepo;

    @Override
    @Transactional
    public RegistroHorasResponseDTO registrar(RegistroHorasRequestDTO req) {
        Mecanico m = mecanicoRepo.findById(req.getMecanicoId())
                .orElseThrow(() -> new EntityNotFoundException("Mecánico no encontrado: " + req.getMecanicoId()));
        OrdenTrabajo o = ordenRepo.findById(req.getOrdenId())
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada: " + req.getOrdenId()));

        if (o.getMecanico() == null || !o.getMecanico().getId().equals(m.getId())) {
            throw new BusinessRuleException(
                    "El mecánico indicado no es el asignado a esta orden de trabajo");
        }

        LocalDate hoy = LocalDate.now();
        BigDecimal horasYaRegistradasHoy = repo.sumHorasByMecanicoIdAndFecha(m.getId(), hoy);
        BigDecimal totalConEsteRegistro = horasYaRegistradasHoy.add(req.getHorasInvertidas());

        if (totalConEsteRegistro.compareTo(MAX_HORAS_POR_DIA) > 0) {
            throw new BusinessRuleException(
                    "El mecánico ya tiene " + horasYaRegistradasHoy + " horas registradas hoy. " +
                            "No se pueden superar las 24 horas diarias entre todas las órdenes.");
        }

        RegistroHoras rh = new RegistroHoras(null, m, o, req.getHorasInvertidas(), hoy);
        return toDTO(repo.save(rh));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroHorasResponseDTO> listarPorOrden(Integer ordenId) {
        return repo.findByOrdenId(ordenId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroHorasResponseDTO> listarPorMecanico(Integer mecanicoId) {
        return repo.findByMecanicoId(mecanicoId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        RegistroHoras rh = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro no encontrado: " + id));

        boolean ordenYaFacturada = facturaRepo.findByOrdenId(rh.getOrden().getId()).isPresent();
        if (ordenYaFacturada) {
            throw new BusinessRuleException(
                    "No se puede eliminar este registro porque la orden ya fue facturada");
        }

        repo.deleteById(id);
    }

    private RegistroHorasResponseDTO toDTO(RegistroHoras r) {
        return new RegistroHorasResponseDTO(r.getId(),
                r.getMecanico().getUsuario().getNombre() + " " + r.getMecanico().getUsuario().getApellido(),
                r.getOrden().getId(), r.getHorasInvertidas(), r.getFechaRegistro());
    }
}