package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.SucursalRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.SucursalResponseDTO;
import com.example.backend_tallerautomotriz.entity.Sucursal;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.CitaRepository;
import com.example.backend_tallerautomotriz.repository.InventarioRepository;
import com.example.backend_tallerautomotriz.repository.MecanicoRepository;
import com.example.backend_tallerautomotriz.repository.SucursalRepository;
import com.example.backend_tallerautomotriz.service.SucursalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class SucursalServiceImpl implements SucursalService {
    private final SucursalRepository repo;
    private final MecanicoRepository mecanicoRepo;
    private final InventarioRepository inventarioRepo;
    private final CitaRepository citaRepo;

    @Override
    @Transactional
    public SucursalResponseDTO crear(SucursalRequestDTO req) {
        if (repo.existsByNombreIgnoreCase(req.getNombre().trim())) {
            throw new DuplicateResourceException("Ya existe una sucursal con el nombre: " + req.getNombre());
        }
        Sucursal s = new Sucursal(null, req.getNombre().trim(), req.getDireccion(), req.getDepartamento());
        return toDTO(repo.save(s));
    }

    @Override
    @Transactional(readOnly = true)
    public SucursalResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SucursalResponseDTO actualizar(Integer id, SucursalRequestDTO req) {
        Sucursal s = buscar(id);

        if (repo.existsByNombreIgnoreCaseAndIdNot(req.getNombre().trim(), id)) {
            throw new DuplicateResourceException("Ya existe otra sucursal con el nombre: " + req.getNombre());
        }

        s.setNombre(req.getNombre().trim());
        s.setDireccion(req.getDireccion());
        s.setDepartamento(req.getDepartamento());
        return toDTO(repo.save(s));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        buscar(id);

        if (mecanicoRepo.existsBySucursalId(id)) {
            throw new BusinessRuleException(
                    "No se puede eliminar la sucursal porque tiene mecánicos asignados");
        }
        if (inventarioRepo.existsBySucursalId(id)) {
            throw new BusinessRuleException(
                    "No se puede eliminar la sucursal porque tiene inventario registrado");
        }
        if (citaRepo.existsBySucursalId(id)) {
            throw new BusinessRuleException(
                    "No se puede eliminar la sucursal porque tiene citas agendadas");
        }

        repo.deleteById(id);
    }

    //Helpers
    private Sucursal buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada: " + id));
    }

    private SucursalResponseDTO toDTO(Sucursal s) {
        return new SucursalResponseDTO(s.getId(), s.getNombre(), s.getDireccion(), s.getDepartamento());
    }
}

