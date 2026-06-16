package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.UsuarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.UsuarioUpdatePasswordDTO;
import com.example.backend_tallerautomotriz.dto.response.UsuarioResponseDTO;
import com.example.backend_tallerautomotriz.entity.Usuario;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;
import com.example.backend_tallerautomotriz.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;


    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscarPorId(id));
    }

    @Override
    public UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO req) {
        Usuario u = buscarPorId(id);
        u.setNombre(req.getNombre());
        u.setApellido(req.getApellido());
        return toDTO(usuarioRepo.save(u));
    }

    @Override
    public void eliminar(Integer id, String emailSolicitante) {
        Usuario aEliminar = buscarPorId(id);

        if(aEliminar.getEmail().equalsIgnoreCase(emailSolicitante)){
            throw new BusinessRuleException("Un administrador no puede eliminarse a si mismo");
        }

        usuarioRepo.deleteById(id);
    }

    @Override
    public void desbloquear(Integer id) {
        Usuario u = buscarPorId(id);
        if (!u.isBloqueado()) {
            throw new BusinessRuleException("El usuario no está bloqueado");
        }
        u.setBloqueado(false);
        u.setIntentosFallidos(0);
        usuarioRepo.save(u);
    }

    @Override
    public UsuarioResponseDTO obtenerPerfil(String email) {
        return toDTO(buscarPorEmail(email));
    }

    @Override
    public UsuarioResponseDTO actualizarPerfil(String email, UsuarioRequestDTO req) {
        Usuario u = buscarPorEmail(email);
        u.setNombre(req.getNombre());
        u.setApellido(req.getApellido());
        return toDTO(usuarioRepo.save(u));
    }

    @Override
    public void cambiarPassword(String email, UsuarioUpdatePasswordDTO req) {
        Usuario u = buscarPorEmail(email);

        if (!passwordEncoder.matches(req.getPasswordActual(), u.getPassword())) {
            throw new BusinessRuleException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(req.getPasswordNueva(), u.getPassword())) {
            throw new BusinessRuleException("La nueva contraseña no puede ser igual a la actual");
        }

        u.setPassword(passwordEncoder.encode(req.getPasswordNueva()));
        usuarioRepo.save(u);
    }

    private Usuario buscarPorId(Integer id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
    }

    private Usuario buscarPorEmail(String email) {
        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));
    }


    private UsuarioResponseDTO toDTO(Usuario u) {
        return new UsuarioResponseDTO(u.getId(), u.getEmail(), u.getNombre(),
                u.getApellido(), u.getRol().getNombre().name(), u.isBloqueado());
    }
}
