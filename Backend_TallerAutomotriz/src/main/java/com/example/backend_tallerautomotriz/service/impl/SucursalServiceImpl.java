package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.SucursalRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.SucursalResponseDTO;
import com.example.backend_tallerautomotriz.entity.Sucursal;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.SucursalRepository;
import com.example.backend_tallerautomotriz.service.SucursalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository repo;

    @Override
    public SucursalResponseDTO crear(SucursalRequestDTO req) {
        Sucursal s = new Sucursal(null, req.getNombre(), req.getDireccion(), req.getDepartamento());
        return toDTO(repo.save(s));
    }

    @Override
    public SucursalResponseDTO obtenerPorId(Integer id) { return toDTO(buscar(id)); }

    @Override
    public List<SucursalResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public SucursalResponseDTO actualizar(Integer id, SucursalRequestDTO req) {
        Sucursal s = buscar(id);
        s.setNombre(req.getNombre());
        s.setDireccion(req.getDireccion());
        s.setDepartamento(req.getDepartamento());
        return toDTO(repo.save(s));
    }

    @Override
    public void eliminar(Integer id) {
        buscar(id);
        if (repo.tieneMecanicos(id))
            throw new BusinessRuleException(
                    "No se puede eliminar la sucursal porque tiene mecánicos activos asignados. " +
                            "Reasigna o elimina los mecánicos primero.");
        repo.deleteById(id);
    }

    private Sucursal buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada: " + id));
    }

    private SucursalResponseDTO toDTO(Sucursal s) {
        return new SucursalResponseDTO(s.getId(), s.getNombre(), s.getDireccion(), s.getDepartamento());
    }
}
