package com.example.backend_tallerautomotriz.security;

import com.example.backend_tallerautomotriz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("tallerAuthorization")
@RequiredArgsConstructor
public class TallerAuthorization {

    private final ClienteRepository clienteRepository;
    private final FacturaRepository facturaRepository;
    private final InventarioRepository inventarioRepository;
    private final MecanicoRepository mecanicoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;
    private final VehiculoRepository vehiculoRepository;

    public boolean esClientePropietario(Authentication authentication, Integer clienteId) {
        return autenticacionValida(authentication)
                && clienteId != null
                && clienteRepository.existsByIdAndUsuarioEmailIgnoreCase(clienteId, authentication.getName());
    }

    public boolean esFacturaDelCliente(Authentication authentication, Integer facturaId) {
        return autenticacionValida(authentication)
                && facturaId != null
                && facturaRepository.existsByIdAndOrdenClienteUsuarioEmailIgnoreCase(
                facturaId,
                authentication.getName());
    }

    public boolean esFacturaDeOrdenDelCliente(Authentication authentication, Integer ordenId) {
        return autenticacionValida(authentication)
                && ordenId != null
                && facturaRepository.existsByOrdenIdAndOrdenClienteUsuarioEmailIgnoreCase(
                ordenId,
                authentication.getName());
    }

    public boolean esOrdenDelCliente(Authentication authentication, Integer ordenId) {
        return autenticacionValida(authentication)
                && ordenId != null
                && ordenTrabajoRepository.existsByIdAndClienteUsuarioEmailIgnoreCase(
                ordenId,
                authentication.getName());
    }

    public boolean esOrdenAsignadaAlMecanico(Authentication authentication, Integer ordenId) {
        return autenticacionValida(authentication)
                && ordenId != null
                && ordenTrabajoRepository.existsByIdAndMecanicoUsuarioEmailIgnoreCase(
                ordenId,
                authentication.getName());
    }

    public boolean esVehiculoDelCliente(Authentication authentication, String patente) {
        return autenticacionValida(authentication)
                && patente != null
                && vehiculoRepository.existsByPatenteIgnoreCaseAndClienteUsuarioEmailIgnoreCase(
                patente,
                authentication.getName());
    }

    public boolean esVehiculoDeOrdenAsignadaAlMecanico(Authentication authentication, String patente) {
        return autenticacionValida(authentication)
                && patente != null
                && ordenTrabajoRepository.existsByVehiculoPatenteIgnoreCaseAndMecanicoUsuarioEmailIgnoreCase(
                patente,
                authentication.getName());
    }

    public boolean esMecanicoPropietario(Authentication authentication, Integer mecanicoId) {
        return autenticacionValida(authentication)
                && mecanicoId != null
                && mecanicoRepository.existsByIdAndUsuarioEmailIgnoreCase(mecanicoId, authentication.getName());
    }

    public boolean esSucursalDelMecanico(Authentication authentication, Integer sucursalId) {
        return autenticacionValida(authentication)
                && sucursalId != null
                && mecanicoRepository.existsBySucursalIdAndUsuarioEmailIgnoreCase(
                sucursalId,
                authentication.getName());
    }

    public boolean esInventarioDeSucursalDelMecanico(Authentication authentication, Integer inventarioId) {
        if (!autenticacionValida(authentication) || inventarioId == null) {
            return false;
        }
        return inventarioRepository.findById(inventarioId)
                .map(inventario -> esSucursalDelMecanico(authentication, inventario.getSucursal().getId()))
                .orElse(false);
    }

    public boolean esUsuarioPropietario(Authentication authentication, Integer usuarioId) {
        return autenticacionValida(authentication)
                && usuarioId != null
                && usuarioRepository.existsByIdAndEmailIgnoreCase(usuarioId, authentication.getName());
    }

    public boolean esNotificacionDelUsuario(Authentication authentication, Integer notificacionId) {
        return autenticacionValida(authentication)
                && notificacionId != null
                && notificacionRepository.existsByIdAndUsuarioEmailIgnoreCase(
                notificacionId,
                authentication.getName());
    }

    private boolean autenticacionValida(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }
}
