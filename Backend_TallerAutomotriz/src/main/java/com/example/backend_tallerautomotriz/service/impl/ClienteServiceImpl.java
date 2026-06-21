package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.ClienteRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ClienteResponseDTO;
import com.example.backend_tallerautomotriz.entity.Cliente;
import com.example.backend_tallerautomotriz.entity.Usuario;
import com.example.backend_tallerautomotriz.enums.NombreRol;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.ClienteRepository;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;
import com.example.backend_tallerautomotriz.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository repo;
    private final UsuarioRepository usuarioRepo;

    @Override
    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO req) {
        Usuario usuario = buscarUsuarioCliente(req.getUsuarioId());
        if (repo.findByUsuarioId(usuario.getId()).isPresent()) {
            throw new DuplicateResourceException("El usuario ya tiene un perfil de cliente");
        }
        Cliente cliente = new Cliente(null, usuario, req.getTelefono().trim());
        return toDTO(repo.save(cliente));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorUsuarioId(Integer usuarioId) {
        Cliente cliente = repo.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe perfil de cliente para el usuario: " + usuarioId));
        return toDTO(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizar(Integer id, ClienteRequestDTO req) {
        Cliente cliente = buscar(id);
        if (!cliente.getUsuario().getId().equals(req.getUsuarioId())) {
            throw new BusinessRuleException("No se puede cambiar el usuario asociado al cliente");
        }
        cliente.setTelefono(req.getTelefono().trim());
        return toDTO(repo.save(cliente));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        repo.delete(buscar(id));
    }

    private Usuario buscarUsuarioCliente(Integer usuarioId) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + usuarioId));
        if (usuario.getRol().getNombre() != NombreRol.CLIENTE) {
            throw new BusinessRuleException("El usuario debe tener rol CLIENTE");
        }
        return usuario;
    }

    private Cliente buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + id));
    }

    private ClienteResponseDTO toDTO(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getUsuario().getNombre(),
                cliente.getUsuario().getApellido(),
                cliente.getUsuario().getEmail(),
                cliente.getTelefono());
    }
}