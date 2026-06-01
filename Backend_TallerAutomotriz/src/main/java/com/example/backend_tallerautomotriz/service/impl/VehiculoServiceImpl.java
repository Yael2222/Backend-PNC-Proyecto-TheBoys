package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.VehiculoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.VehiculoResponseDTO;
import com.example.backend_tallerautomotriz.entity.Cliente;
import com.example.backend_tallerautomotriz.entity.Vehiculo;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.ClienteRepository;
import com.example.backend_tallerautomotriz.repository.VehiculoRepository;
import com.example.backend_tallerautomotriz.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class VehiculoServiceImpl implements VehiculoService {
    private final VehiculoRepository repo;
    private final ClienteRepository clienteRepo;

    @Override public VehiculoResponseDTO crear(VehiculoRequestDTO req) {
        if (repo.existsById(req.getPatente()))
            throw new DuplicateResourceException("Patente ya registrada: " + req.getPatente());
        Cliente c = clienteRepo.findById(req.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        return toDTO(repo.save(new Vehiculo(req.getPatente(), req.getMarca(), req.getModelo(), c)));
    }
    @Override public VehiculoResponseDTO obtenerPorPatente(String patente) { return toDTO(buscar(patente)); }
    @Override public List<VehiculoResponseDTO> listarPorCliente(Integer clienteId) {
        return repo.findByClienteId(clienteId).stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public List<VehiculoResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public VehiculoResponseDTO actualizar(String patente, VehiculoRequestDTO req) {
        Vehiculo v = buscar(patente); v.setMarca(req.getMarca()); v.setModelo(req.getModelo());
        return toDTO(repo.save(v));
    }
    @Override public void eliminar(String patente) { buscar(patente); repo.deleteById(patente); }

    private Vehiculo buscar(String patente) {
        return repo.findById(patente).orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado: " + patente));
    }
    private VehiculoResponseDTO toDTO(Vehiculo v) {
        return new VehiculoResponseDTO(v.getPatente(), v.getMarca(), v.getModelo(),
                v.getCliente().getUsuario().getNombre() + " " + v.getCliente().getUsuario().getApellido());
    }
}

