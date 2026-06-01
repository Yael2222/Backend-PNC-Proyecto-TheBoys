package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.ServicioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ServicioResponseDTO;
import com.example.backend_tallerautomotriz.entity.Servicio;
import com.example.backend_tallerautomotriz.enums.EstadoServicio;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.ServicioRepository;
import com.example.backend_tallerautomotriz.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ServicioServiceImpl implements ServicioService {
    private final ServicioRepository repo;

    @Override public ServicioResponseDTO crear(ServicioRequestDTO req) {
        Servicio s = new Servicio(null, req.getNombre(), req.getDescripcion(),
                req.getTiempoEstimadoMinutos(), req.getPrecioBase(), EstadoServicio.ACTIVO);
        return toDTO(repo.save(s));
    }
    @Override public ServicioResponseDTO obtenerPorId(Integer id) { return toDTO(buscar(id)); }
    @Override public List<ServicioResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public List<ServicioResponseDTO> listarActivos() {
        return repo.findAll().stream()
                .filter(s -> s.getEstado() == EstadoServicio.ACTIVO)
                .map(this::toDTO).collect(Collectors.toList());
    }
    @Override public ServicioResponseDTO actualizar(Integer id, ServicioRequestDTO req) {
        Servicio s = buscar(id);
        s.setNombre(req.getNombre()); s.setDescripcion(req.getDescripcion());
        s.setTiempoEstimadoMinutos(req.getTiempoEstimadoMinutos()); s.setPrecioBase(req.getPrecioBase());
        return toDTO(repo.save(s));
    }
    @Override public void desactivar(Integer id) {
        Servicio s = buscar(id); s.setEstado(EstadoServicio.INACTIVO); repo.save(s);
    }

    private Servicio buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado: " + id));
    }
    private ServicioResponseDTO toDTO(Servicio s) {
        return new ServicioResponseDTO(s.getId(), s.getNombre(), s.getDescripcion(),
                s.getTiempoEstimadoMinutos(), s.getPrecioBase(), s.getEstado().name());
    }
}

