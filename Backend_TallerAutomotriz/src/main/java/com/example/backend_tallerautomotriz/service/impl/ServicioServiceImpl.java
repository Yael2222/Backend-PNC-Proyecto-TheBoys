package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.ServicioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ServicioResponseDTO;
import com.example.backend_tallerautomotriz.entity.Servicio;
import com.example.backend_tallerautomotriz.enums.EstadoServicio;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.ServicioRepository;
import com.example.backend_tallerautomotriz.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository repo;

    @Override
    @Transactional
    public ServicioResponseDTO crear(ServicioRequestDTO req) {
        if (repo.existsByNombreIgnoreCase(req.getNombre().trim())) {
            throw new DuplicateResourceException(
                    "Ya existe un servicio con el nombre: " + req.getNombre());
        }
        Servicio s = new Servicio(null, req.getNombre().trim(), req.getDescripcion(),
                req.getTiempoEstimadoMinutos(), req.getPrecioBase(), EstadoServicio.ACTIVO);
        return toDTO(repo.save(s));
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponseDTO> listarActivos() {
        return repo.findAll().stream()
                .filter(s -> s.getEstado() == EstadoServicio.ACTIVO)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ServicioResponseDTO actualizar(Integer id, ServicioRequestDTO req) {
        Servicio s = buscar(id);

        if (s.getEstado() == EstadoServicio.INACTIVO) {
            throw new BusinessRuleException(
                    "No se puede editar un servicio inactivo. Reactívalo primero.");
        }

        if (repo.existsByNombreIgnoreCaseAndIdNot(req.getNombre().trim(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe otro servicio con el nombre: " + req.getNombre());
        }

        s.setNombre(req.getNombre().trim());
        s.setDescripcion(req.getDescripcion());
        s.setTiempoEstimadoMinutos(req.getTiempoEstimadoMinutos());
        s.setPrecioBase(req.getPrecioBase());
        return toDTO(repo.save(s));
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        Servicio s = buscar(id);
        if (s.getEstado() == EstadoServicio.INACTIVO) {
            throw new BusinessRuleException("El servicio ya está inactivo");
        }
        s.setEstado(EstadoServicio.INACTIVO);
        repo.save(s);
    }

    @Override
    @Transactional
    public void reactivar(Integer id) {
        Servicio s = buscar(id);
        if (s.getEstado() == EstadoServicio.ACTIVO) {
            throw new BusinessRuleException("El servicio ya está activo");
        }
        s.setEstado(EstadoServicio.ACTIVO);
        repo.save(s);
    }

    //Helpers
    private Servicio buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado: " + id));
    }

    private ServicioResponseDTO toDTO(Servicio s) {
        return new ServicioResponseDTO(s.getId(), s.getNombre(), s.getDescripcion(),
                s.getTiempoEstimadoMinutos(), s.getPrecioBase(), s.getEstado().name());
    }
}

