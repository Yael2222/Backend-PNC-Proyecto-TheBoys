package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.LoginRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.RegisterRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.AuthResponseDTO;
import com.example.backend_tallerautomotriz.entity.Cliente;
import com.example.backend_tallerautomotriz.entity.Rol;
import com.example.backend_tallerautomotriz.entity.Usuario;
import com.example.backend_tallerautomotriz.enums.NombreRol;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.exception.UnauthorizedException;
import com.example.backend_tallerautomotriz.repository.ClienteRepository;
import com.example.backend_tallerautomotriz.repository.RolRepository;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;
import com.example.backend_tallerautomotriz.security.JwtTokenProvider;
import com.example.backend_tallerautomotriz.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final ClienteRepository clienteRepo;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        String email = normalizarEmail(request.getEmail());
        Usuario usuario = usuarioRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Credenciales incorrectas"));

        if (usuario.isBloqueado()) {
            throw new UnauthorizedException("Cuenta bloqueada por multiples intentos fallidos");
        }

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
            usuario.setIntentosFallidos(0);
            usuarioRepo.save(usuario);
        } catch (AuthenticationException exception) {
            int intentos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(intentos);
            if (intentos >= 5) {
                usuario.setBloqueado(true);
            }
            usuarioRepo.save(usuario);
            throw new UnauthorizedException("Credenciales incorrectas. Intentos: " + intentos + "/5");
        }

        String token = jwtProvider.generateToken(usuario.getEmail(), usuario.getRol().getNombre().name());
        return new AuthResponseDTO(
                token,
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol().getNombre().name(),
                usuario.getNombre(),
                usuario.getApellido());
    }

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String email = normalizarEmail(request.getEmail());
        if (usuarioRepo.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("El email ya esta registrado");
        }

        Rol rol = rolRepo.findByNombre(request.getRol())
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + request.getRol()));

        if (rol.getNombre() == NombreRol.CLIENTE
                && (request.getTelefono() == null || request.getTelefono().isBlank())) {
            throw new BusinessRuleException("El teléfono es obligatorio para registrarse como cliente");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setRol(rol);
        usuarioRepo.save(usuario);

        if (rol.getNombre() == NombreRol.CLIENTE) {
            Cliente cliente = new Cliente(null, usuario, request.getTelefono().trim());
            clienteRepo.save(cliente);
        }

        String token = jwtProvider.generateToken(usuario.getEmail(), rol.getNombre().name());
        return new AuthResponseDTO(
                token,
                usuario.getId(),
                usuario.getEmail(),
                rol.getNombre().name(),
                usuario.getNombre(),
                usuario.getApellido());
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}