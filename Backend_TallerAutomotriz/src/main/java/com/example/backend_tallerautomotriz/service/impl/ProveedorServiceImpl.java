package com.example.backend_tallerautomotriz.service.impl;


import com.example.backend_tallerautomotriz.dto.request.ProveedorRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ProveedorResponseDTO;
import com.example.backend_tallerautomotriz.entity.Proveedor;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.ProveedorRepository;
import com.example.backend_tallerautomotriz.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {
    private final ProveedorRepository repo;

    @Override public ProveedorResponseDTO crear(ProveedorRequestDTO req) {
        return toDTO(repo.save(new Proveedor(null, req.getNombre(), req.getMarca(), req.getContacto())));
    }
    @Override public ProveedorResponseDTO obtenerPorId(Integer id) { return toDTO(buscar(id)); }
    @Override public List<ProveedorResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public ProveedorResponseDTO actualizar(Integer id, ProveedorRequestDTO req) {
        Proveedor p = buscar(id);
        p.setNombre(req.getNombre()); p.setMarca(req.getMarca()); p.setContacto(req.getContacto());
        return toDTO(repo.save(p));
    }
    @Override public void eliminar(Integer id) { buscar(id); repo.deleteById(id); }

    private Proveedor buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado: " + id));
    }
    private ProveedorResponseDTO toDTO(Proveedor p) {
        return new ProveedorResponseDTO(p.getId(), p.getNombre(), p.getMarca(), p.getContacto());
    }
}

