package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.UsuarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.UsuarioResponseDTO;
import com.example.backend_tallerautomotriz.entity.Usuario;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;
import com.example.backend_tallerautomotriz.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepo;

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    public UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO req) {
        Usuario u = buscar(id);
        u.setNombre(req.getNombre());
        u.setApellido(req.getApellido());
        return toDTO(usuarioRepo.save(u));
    }

    @Override
    public void eliminar(Integer id) {
        buscar(id);
        usuarioRepo.deleteById(id);
    }

    @Override
    public void desbloquear(Integer id) {
        Usuario u = buscar(id);
        u.setBloqueado(false);
        u.setIntentosFallidos(0);
        usuarioRepo.save(u);
    }

    private Usuario buscar(Integer id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
    }

    private UsuarioResponseDTO toDTO(Usuario u) {
        return new UsuarioResponseDTO(u.getId(), u.getEmail(), u.getNombre(),
                u.getApellido(), u.getRol().getNombre().name(), u.isBloqueado());
    }
}
