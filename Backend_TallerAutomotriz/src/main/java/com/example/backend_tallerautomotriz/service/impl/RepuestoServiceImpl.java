package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.RepuestoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.RepuestoResponseDTO;
import com.example.backend_tallerautomotriz.entity.Proveedor;
import com.example.backend_tallerautomotriz.entity.Repuesto;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.ProveedorRepository;
import com.example.backend_tallerautomotriz.repository.RepuestoRepository;
import com.example.backend_tallerautomotriz.service.RepuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class RepuestoServiceImpl implements RepuestoService {
    private final RepuestoRepository repo;
    private final ProveedorRepository proveedorRepo;

    @Override public RepuestoResponseDTO crear(RepuestoRequestDTO req) {
        Proveedor p = proveedorRepo.findById(req.getProveedorId())
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
        return toDTO(repo.save(new Repuesto(null, p, req.getNombre(), req.getPrecioUnitario())));
    }
    @Override public RepuestoResponseDTO obtenerPorId(Integer id) { return toDTO(buscar(id)); }
    @Override public List<RepuestoResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public RepuestoResponseDTO actualizar(Integer id, RepuestoRequestDTO req) {
        Repuesto r = buscar(id); r.setNombre(req.getNombre()); r.setPrecioUnitario(req.getPrecioUnitario());
        return toDTO(repo.save(r));
    }
    @Override public void eliminar(Integer id) { buscar(id); repo.deleteById(id); }

    private Repuesto buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Repuesto no encontrado: " + id));
    }
    private RepuestoResponseDTO toDTO(Repuesto r) {
        return new RepuestoResponseDTO(r.getId(), r.getNombre(), r.getPrecioUnitario(), r.getProveedor().getNombre());
    }
}

