package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.CambiarRolRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.UsuarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.UsuarioResponseDTO;
import com.example.backend_tallerautomotriz.entity.Mecanico;
import com.example.backend_tallerautomotriz.entity.Rol;
import com.example.backend_tallerautomotriz.entity.Sucursal;
import com.example.backend_tallerautomotriz.entity.Usuario;
import com.example.backend_tallerautomotriz.enums.NombreRol;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.*;
import com.example.backend_tallerautomotriz.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final MecanicoRepository mecanicoRepo;
    private final ClienteRepository clienteRepo;
    private final SucursalRepository sucursalRepo;
    private final OrdenTrabajoRepository ordenRepo;
    private final CitaRepository citaRepo;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email) {
        return toDTO(usuarioRepo.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con email: " + email)));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO request) {
        Usuario usuario = buscar(id);
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        return toDTO(usuarioRepo.save(usuario));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        usuarioRepo.delete(buscar(id));
    }

    @Override
    @Transactional
    public void desbloquear(Integer id) {
        Usuario usuario = buscar(id);
        usuario.setBloqueado(false);
        usuario.setIntentosFallidos(0);
        usuarioRepo.save(usuario);
    }

    private void prepararCambioAMecanico(
            Usuario usuario,
            NombreRol actual,
            NombreRol nuevo,
            Integer sucursalId) {
        if (nuevo != NombreRol.MECANICO || actual == NombreRol.MECANICO) {
            return;
        }
        if (sucursalId == null) {
            throw new BusinessRuleException("Se requiere sucursalId para asignar el rol MECANICO");
        }
        Sucursal sucursal = buscarSucursal(sucursalId);
        if (mecanicoRepo.findByUsuarioId(usuario.getId()).isEmpty()) {
            mecanicoRepo.save(new Mecanico(null, usuario, sucursal));
        }
    }

    private void actualizarSucursalMecanicoSiAplica(Integer usuarioId, Integer sucursalId, NombreRol rol) {
        if (rol != NombreRol.MECANICO || sucursalId == null) {
            return;
        }
        Mecanico mecanico = mecanicoRepo.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil de mecanico no encontrado"));
        mecanico.setSucursal(buscarSucursal(sucursalId));
        mecanicoRepo.save(mecanico);
    }

    private Sucursal buscarSucursal(Integer sucursalId) {
        return sucursalRepo.findById(sucursalId)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada: " + sucursalId));
    }

    private void prepararCambioDesdeMecanico(Integer usuarioId, NombreRol actual, NombreRol nuevo) {
        if (actual != NombreRol.MECANICO || nuevo == NombreRol.MECANICO) {
            return;
        }
        mecanicoRepo.findByUsuarioId(usuarioId).ifPresent(mecanico -> {
            if (ordenRepo.existsByMecanicoId(mecanico.getId()) || citaRepo.existsByMecanicoId(mecanico.getId())) {
                throw new BusinessRuleException("No se puede cambiar el rol de un mecanico con asignaciones");
            }
            mecanicoRepo.delete(mecanico);
        });
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarRol(Integer usuarioId, CambiarRolRequestDTO request) {
        Usuario usuario = buscar(usuarioId);
        NombreRol rolActual = usuario.getRol().getNombre();
        NombreRol nuevoRol = request.getNuevoRol();
        if (rolActual == nuevoRol) {
            actualizarSucursalMecanicoSiAplica(usuarioId, request.getSucursalId(), nuevoRol);
            return toDTO(usuario);
        }

        validarCambioDesdeCliente(usuarioId, rolActual, nuevoRol);
        prepararCambioDesdeMecanico(usuarioId, rolActual, nuevoRol);
        prepararCambioAMecanico(usuario, rolActual, nuevoRol, request.getSucursalId());
        Rol rol = rolRepo.findByNombre(nuevoRol)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + nuevoRol));
        usuario.setRol(rol);
        return toDTO(usuarioRepo.save(usuario));
    }

    private void validarCambioDesdeCliente(Integer usuarioId, NombreRol actual, NombreRol nuevo) {
        if (actual == NombreRol.CLIENTE
                && nuevo != NombreRol.CLIENTE
                && clienteRepo.findByUsuarioId(usuarioId).isPresent()) {
            throw new BusinessRuleException("No se puede cambiar el rol mientras exista un perfil de cliente");
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Usuario buscar(Integer id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
    }

    private UsuarioResponseDTO toDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol().getNombre().name(),
                usuario.isBloqueado());
    }
}
