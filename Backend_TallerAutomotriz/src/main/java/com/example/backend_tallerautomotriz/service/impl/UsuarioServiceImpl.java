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
import com.example.backend_tallerautomotriz.repository.MecanicoRepository;
import com.example.backend_tallerautomotriz.repository.RolRepository;
import com.example.backend_tallerautomotriz.repository.SucursalRepository;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;
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
    private final SucursalRepository sucursalRepo;

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    public UsuarioResponseDTO buscarPorEmail(String email) {
        Usuario u = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con email: " + email));
        return toDTO(u);
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

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarRol(Integer usuarioId, CambiarRolRequestDTO req) {
        Usuario usuario = buscar(usuarioId);

        NombreRol nuevoNombreRol;
        try {
            nuevoNombreRol = NombreRol.valueOf(req.getNuevoRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Rol inválido: " + req.getNuevoRol() + ". Use: CLIENTE, MECANICO, ADMIN");
        }

        Rol nuevoRol = rolRepo.findByNombre(nuevoNombreRol)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + nuevoNombreRol));

        NombreRol rolActual = usuario.getRol().getNombre();

        // Si se promueve a MECANICO, crear el registro de mecánico
        if (nuevoNombreRol == NombreRol.MECANICO && rolActual != NombreRol.MECANICO) {
            if (req.getSucursalId() == null)
                throw new BusinessRuleException("Se requiere sucursalId para asignar el rol MECANICO");

            Sucursal sucursal = sucursalRepo.findById(req.getSucursalId())
                    .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada: " + req.getSucursalId()));

            // Verificar que no tenga ya un registro de mecánico (edge case)
            if (mecanicoRepo.findByUsuarioId(usuarioId).isEmpty()) {
                Mecanico mecanico = new Mecanico(null, usuario, sucursal);
                mecanicoRepo.save(mecanico);
            }
        }

        // Si se degrada desde MECANICO, eliminar el registro de mecánico
        if (rolActual == NombreRol.MECANICO && nuevoNombreRol != NombreRol.MECANICO) {
            mecanicoRepo.findByUsuarioId(usuarioId).ifPresent(mecanicoRepo::delete);
        }

        usuario.setRol(nuevoRol);
        return toDTO(usuarioRepo.save(usuario));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Usuario buscar(Integer id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
    }

    private UsuarioResponseDTO toDTO(Usuario u) {
        return new UsuarioResponseDTO(
                u.getId(), u.getEmail(), u.getNombre(),
                u.getApellido(), u.getRol().getNombre().name(), u.isBloqueado());
    }
}
