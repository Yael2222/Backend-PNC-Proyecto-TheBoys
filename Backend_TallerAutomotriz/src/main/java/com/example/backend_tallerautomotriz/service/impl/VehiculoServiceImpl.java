package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.VehiculoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.VehiculoResponseDTO;
import com.example.backend_tallerautomotriz.entity.Cliente;
import com.example.backend_tallerautomotriz.entity.Vehiculo;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.ClienteRepository;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepo;


    @Override public VehiculoResponseDTO crear(VehiculoRequestDTO req) {
        String patente = req.getPatente().toUpperCase().trim();
        if (repo.existsById(patente))
            throw new DuplicateResourceException("Patente ya registrada: " + patente);
        Cliente c = clienteRepo.findById(req.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        return toDTO(repo.save(new Vehiculo(req.getPatente(), req.getMarca(), req.getModelo(), c)));
    }
    @Override public VehiculoResponseDTO obtenerPorPatente(String patente) { return toDTO(buscar(patente.toUpperCase().trim()));}
    @Override public List<VehiculoResponseDTO> listarPorCliente(Integer clienteId) {
        if (!clienteRepo.existsById(clienteId)) {
            throw new EntityNotFoundException("Cliente no encontrado: " + clienteId);
        }
        return repo.findByClienteId(clienteId).stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public List<VehiculoResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public VehiculoResponseDTO actualizar(String patente, VehiculoRequestDTO req, String emailSolicitante) {
        Vehiculo v = buscar(patente.toUpperCase().trim());
        String emailDueño = v.getCliente().getUsuario().getEmail();
        String rolSolicitante = v.getCliente().getUsuario().getRol().getNombre().name();
        boolean esAdmin = "ADMIN".equals(obtenerRolPorEmail(emailSolicitante));
        if (!esAdmin && !emailDueño.equalsIgnoreCase(emailSolicitante)) {
            throw new BusinessRuleException("No tienes permiso para modificar este vehículo");
        }
        v.setMarca(req.getMarca());
        v.setModelo(req.getModelo());
        return toDTO(repo.save(v));
    }
    @Override public void eliminar(String patente) {
        Vehiculo v = buscar(patente.toUpperCase().trim());
        boolean tieneOrdenesActivas = v.getOrdenesTrabajos() != null &&
                v.getOrdenesTrabajos().stream()
                        .anyMatch(o -> !"COMPLETADA".equals(o.getEstado().name()) &&
                                !"CANCELADA".equals(o.getEstado().name()));
        if (tieneOrdenesActivas) {
            throw new BusinessRuleException(
                    "No se puede eliminar el vehículo porque tiene órdenes de trabajo activas");
        }
        repo.deleteById(patente.toUpperCase().trim());
    }

    private Vehiculo buscar(String patente) {
        return repo.findById(patente).orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado: " + patente));
    }
    private String obtenerRolPorEmail(String email) {
        return usuarioRepo.findByEmail(email)
                .map(u -> u.getRol().getNombre().name())
                .orElse("CLIENTE"); // default seguro
    }
    private VehiculoResponseDTO toDTO(Vehiculo v) {
        return new VehiculoResponseDTO(v.getPatente(), v.getMarca(), v.getModelo(),
                v.getCliente().getUsuario().getNombre() + " " + v.getCliente().getUsuario().getApellido());
    }
}

