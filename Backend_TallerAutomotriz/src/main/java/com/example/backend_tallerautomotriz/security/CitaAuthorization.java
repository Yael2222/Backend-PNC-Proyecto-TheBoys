package com.example.backend_tallerautomotriz.security;

import com.example.backend_tallerautomotriz.repository.CitaRepository;
import com.example.backend_tallerautomotriz.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("citaAuthorization")
@RequiredArgsConstructor
public class CitaAuthorization {

    private final ClienteRepository clienteRepository;
    private final CitaRepository citaRepository;

    public boolean esClientePropietario(Authentication authentication, Integer clienteId) {
        return autenticacionValida(authentication)
                && clienteId != null
                && clienteRepository.existsByIdAndUsuarioEmailIgnoreCase(clienteId, authentication.getName());
    }

    public boolean esCitaDelCliente(Authentication authentication, Integer citaId) {
        return autenticacionValida(authentication)
                && citaId != null
                && citaRepository.existsByIdAndClienteUsuarioEmailIgnoreCase(citaId, authentication.getName());
    }

    public boolean esCitaDelMecanico(Authentication authentication, Integer citaId) {
        return autenticacionValida(authentication)
                && citaId != null
                && citaRepository.existsByIdAndMecanicoUsuarioEmailIgnoreCase(citaId, authentication.getName());
    }

    private boolean autenticacionValida(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }
}
