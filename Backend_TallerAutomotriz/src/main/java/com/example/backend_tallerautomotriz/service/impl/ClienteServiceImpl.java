package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.ClienteRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ClienteResponseDTO;
import com.example.backend_tallerautomotriz.entity.Cliente;
import com.example.backend_tallerautomotriz.entity.Usuario;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.ClienteRepository;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;
import com.example.backend_tallerautomotriz.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository repo;
    private final UsuarioRepository usuarioRepo;

    @Override public ClienteResponseDTO crear(ClienteRequestDTO req) {
        Usuario u = usuarioRepo.findById(req.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + req.getUsuarioId()));
        Cliente c = new Cliente(null, u, req.getTelefono());
        return toDTO(repo.save(c));
    }
    @Override public ClienteResponseDTO obtenerPorId(Integer id) { return toDTO(buscar(id)); }
    @Override public List<ClienteResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public ClienteResponseDTO actualizar(Integer id, ClienteRequestDTO req) {
        Cliente c = buscar(id); c.setTelefono(req.getTelefono()); return toDTO(repo.save(c));
    }
    @Override public void eliminar(Integer id) { buscar(id); repo.deleteById(id); }

    private Cliente buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + id));
    }
    private ClienteResponseDTO toDTO(Cliente c) {
        return new ClienteResponseDTO(c.getId(), c.getUsuario().getNombre(),
                c.getUsuario().getApellido(), c.getUsuario().getEmail(), c.getTelefono());
    }
}
