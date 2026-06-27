package com.example.backend_tallerautomotriz.service.impl;


import com.example.backend_tallerautomotriz.dto.request.ProveedorRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ProveedorResponseDTO;
import com.example.backend_tallerautomotriz.entity.Proveedor;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.ProveedorRepository;
import com.example.backend_tallerautomotriz.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {
    private final ProveedorRepository repo;

    @Override
    @Transactional
    public ProveedorResponseDTO crear(ProveedorRequestDTO req) {
        String nombre = req.getNombre().trim();
        String marca = req.getMarca().trim();
        if (repo.existsByNombreIgnoreCaseAndMarcaIgnoreCase(nombre, marca)) {
            throw new DuplicateResourceException("Ya existe un proveedor con el mismo nombre y marca");
        }
        Proveedor proveedor = new Proveedor(null, nombre, marca, req.getContacto().trim());
        return toDTO(repo.save(proveedor));
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public ProveedorResponseDTO actualizar(Integer id, ProveedorRequestDTO req) {
        Proveedor proveedor = buscar(id);
        String nombre = req.getNombre().trim();
        String marca = req.getMarca().trim();
        if (repo.existsByNombreIgnoreCaseAndMarcaIgnoreCaseAndIdNot(nombre, marca, id)) {
            throw new DuplicateResourceException("Ya existe un proveedor con el mismo nombre y marca");
        }
        proveedor.setNombre(nombre);
        proveedor.setMarca(marca);
        proveedor.setContacto(req.getContacto().trim());
        return toDTO(repo.save(proveedor));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        repo.delete(buscar(id));
    }

    private Proveedor buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado: " + id));
    }

    private ProveedorResponseDTO toDTO(Proveedor proveedor) {
        return new ProveedorResponseDTO(
                proveedor.getId(),
                proveedor.getNombre(),
                proveedor.getMarca(),
                proveedor.getContacto());
    }
}

