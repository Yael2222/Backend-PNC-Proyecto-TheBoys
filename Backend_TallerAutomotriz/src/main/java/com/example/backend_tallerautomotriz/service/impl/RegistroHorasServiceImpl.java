package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.RegistroHorasRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.RegistroHorasResponseDTO;
import com.example.backend_tallerautomotriz.entity.Mecanico;
import com.example.backend_tallerautomotriz.entity.OrdenTrabajo;
import com.example.backend_tallerautomotriz.entity.RegistroHoras;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.MecanicoRepository;
import com.example.backend_tallerautomotriz.repository.OrdenTrabajoRepository;
import com.example.backend_tallerautomotriz.repository.RegistroHorasRepository;
import com.example.backend_tallerautomotriz.service.RegistroHorasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class RegistroHorasServiceImpl implements RegistroHorasService {
    private final RegistroHorasRepository repo;
    private final MecanicoRepository mecanicoRepo;
    private final OrdenTrabajoRepository ordenRepo;

    @Override public RegistroHorasResponseDTO registrar(RegistroHorasRequestDTO req) {
        Mecanico m = mecanicoRepo.findById(req.getMecanicoId())
                .orElseThrow(() -> new EntityNotFoundException("Mecánico no encontrado"));
        OrdenTrabajo o = ordenRepo.findById(req.getOrdenId())
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada"));
        RegistroHoras rh = new RegistroHoras(null, m, o, req.getHorasInvertidas(), LocalDate.now());
        return toDTO(repo.save(rh));
    }
    @Override public List<RegistroHorasResponseDTO> listarPorOrden(Integer ordenId) {
        return repo.findByOrdenId(ordenId).stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public List<RegistroHorasResponseDTO> listarPorMecanico(Integer mecanicoId) {
        return repo.findByMecanicoId(mecanicoId).stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public void eliminar(Integer id) {
        repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Registro no encontrado: " + id));
        repo.deleteById(id);
    }
    private RegistroHorasResponseDTO toDTO(RegistroHoras r) {
        return new RegistroHorasResponseDTO(r.getId(),
                r.getMecanico().getUsuario().getNombre() + " " + r.getMecanico().getUsuario().getApellido(),
                r.getOrden().getId(), r.getHorasInvertidas(), r.getFechaRegistro());
    }
}
