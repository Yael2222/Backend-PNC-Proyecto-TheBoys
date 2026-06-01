package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.LoginRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.RegisterRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.AuthResponseDTO;
import com.example.backend_tallerautomotriz.entity.Rol;
import com.example.backend_tallerautomotriz.entity.Usuario;
import com.example.backend_tallerautomotriz.enums.NombreRol;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.RolRepository;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;
import com.example.backend_tallerautomotriz.security.JwtTokenProvider;
import com.example.backend_tallerautomotriz.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    @Override
    public AuthResponseDTO login(LoginRequestDTO req) {
        Usuario usuario = usuarioRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (usuario.isBloqueado())
            throw new BusinessRuleException("Cuenta bloqueada por múltiples intentos fallidos");

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
            usuario.setIntentosFallidos(0);
            usuarioRepo.save(usuario);
        } catch (BadCredentialsException e) {
            int intentos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(intentos);
            if (intentos >= 5) usuario.setBloqueado(true);
            usuarioRepo.save(usuario);
            throw new BusinessRuleException("Credenciales incorrectas. Intentos: " + intentos + "/5");
        }

        String token = jwtProvider.generateToken(usuario.getEmail(), usuario.getRol().getNombre().name());
        return new AuthResponseDTO(token, usuario.getEmail(),
                usuario.getRol().getNombre().name(), usuario.getNombre());
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO req) {
        if (usuarioRepo.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("El email ya está registrado");

        NombreRol nombreRol = NombreRol.valueOf(req.getRol().toUpperCase());
        Rol rol = rolRepo.findByNombre(nombreRol)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + req.getRol()));

        Usuario usuario = new Usuario();
        usuario.setEmail(req.getEmail());
        usuario.setPassword(passwordEncoder.encode(req.getPassword()));
        usuario.setNombre(req.getNombre());
        usuario.setApellido(req.getApellido());
        usuario.setRol(rol);
        usuarioRepo.save(usuario);

        String token = jwtProvider.generateToken(usuario.getEmail(), rol.getNombre().name());
        return new AuthResponseDTO(token, usuario.getEmail(), rol.getNombre().name(), usuario.getNombre());
    }
}
