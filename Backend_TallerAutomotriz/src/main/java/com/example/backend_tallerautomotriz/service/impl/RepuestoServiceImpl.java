package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.RepuestoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.RepuestoResponseDTO;
import com.example.backend_tallerautomotriz.entity.Proveedor;
import com.example.backend_tallerautomotriz.entity.Repuesto;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.InventarioRepository;
import com.example.backend_tallerautomotriz.repository.OrdenRepuestoRepository;
import com.example.backend_tallerautomotriz.repository.ProveedorRepository;
import com.example.backend_tallerautomotriz.repository.RepuestoRepository;
import com.example.backend_tallerautomotriz.service.RepuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class RepuestoServiceImpl implements RepuestoService {

    private final RepuestoRepository repo;
    private final ProveedorRepository proveedorRepo;
    private final OrdenRepuestoRepository ordenRepuestoRepo;
    private final InventarioRepository inventarioRepo;

    @Override
    @Transactional
    public RepuestoResponseDTO crear(RepuestoRequestDTO req) {
        Proveedor p = proveedorRepo.findById(req.getProveedorId())
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado: " + req.getProveedorId()));

        if (repo.existsByNombreIgnoreCaseAndProveedorId(req.getNombre().trim(), req.getProveedorId())) {
            throw new DuplicateResourceException(
                    "Este proveedor ya tiene registrado un repuesto con el nombre: " + req.getNombre());
        }

        Repuesto r = new Repuesto(null, p, req.getNombre().trim(), req.getPrecioUnitario());
        return toDTO(repo.save(r));
    }

    @Override
    @Transactional(readOnly = true)
    public RepuestoResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepuestoResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RepuestoResponseDTO actualizar(Integer id, RepuestoRequestDTO req) {
        Repuesto r = buscar(id);
        if (repo.existsByNombreIgnoreCaseAndProveedorIdAndIdNot(req.getNombre().trim(), req.getProveedorId(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe otro repuesto con ese nombre para este proveedor");
        }
        if (!req.getProveedorId().equals(r.getProveedor().getId())) {
            Proveedor nuevoProveedor = proveedorRepo.findById(req.getProveedorId())
                    .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado: " + req.getProveedorId()));
            r.setProveedor(nuevoProveedor);
        }
        r.setNombre(req.getNombre().trim());
        r.setPrecioUnitario(req.getPrecioUnitario());
        return toDTO(repo.save(r));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        buscar(id);
        if (ordenRepuestoRepo.existsByRepuestoId(id)) {
            throw new BusinessRuleException(
                    "No se puede eliminar el repuesto porque ya fue usado en órdenes de trabajo");
        }
        if (inventarioRepo.existsByRepuestoId(id)) {
            throw new BusinessRuleException(
                    "No se puede eliminar el repuesto porque tiene inventario registrado en alguna sucursal. " +
                            "Elimina primero esos registros de inventario.");
        }
        repo.deleteById(id);
    }

    //Helpers
    private Repuesto buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Repuesto no encontrado: " + id));
    }

    private RepuestoResponseDTO toDTO(Repuesto r) {
        return new RepuestoResponseDTO(r.getId(), r.getNombre(), r.getPrecioUnitario(), r.getProveedor().getNombre());
    }
}

