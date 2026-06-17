package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.LoginRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.RegisterRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.AuthResponseDTO;
import com.example.backend_tallerautomotriz.entity.Rol;
import com.example.backend_tallerautomotriz.entity.Usuario;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.exception.UnauthorizedException;
import com.example.backend_tallerautomotriz.repository.RolRepository;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;
import com.example.backend_tallerautomotriz.security.JwtTokenProvider;
import com.example.backend_tallerautomotriz.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
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
                usuario.getEmail(),
                usuario.getRol().getNombre().name(),
                usuario.getNombre());
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String email = normalizarEmail(request.getEmail());
        if (usuarioRepo.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("El email ya esta registrado");
        }

        Rol rol = rolRepo.findByNombre(request.getRol())
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + request.getRol()));

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setRol(rol);
        usuarioRepo.save(usuario);

        String token = jwtProvider.generateToken(usuario.getEmail(), rol.getNombre().name());
        return new AuthResponseDTO(token, usuario.getEmail(), rol.getNombre().name(), usuario.getNombre());
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
