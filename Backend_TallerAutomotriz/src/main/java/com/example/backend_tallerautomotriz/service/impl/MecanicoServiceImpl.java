package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.MecanicoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.MecanicoResponseDTO;
import com.example.backend_tallerautomotriz.entity.Mecanico;
import com.example.backend_tallerautomotriz.entity.Sucursal;
import com.example.backend_tallerautomotriz.entity.Usuario;
import com.example.backend_tallerautomotriz.enums.NombreRol;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.*;
import com.example.backend_tallerautomotriz.service.MecanicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.backend_tallerautomotriz.repository.MecanicoRepository;
import com.example.backend_tallerautomotriz.repository.SucursalRepository;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class MecanicoServiceImpl implements MecanicoService {
    private final MecanicoRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final SucursalRepository sucursalRepo;
    private final OrdenTrabajoRepository ordenRepo;
    private final CitaRepository citaRepo;
    private final RegistroHorasRepository registroHorasRepo;

    @Override
    @Transactional
    public MecanicoResponseDTO crear(MecanicoRequestDTO req) {
        Usuario usuario = buscarUsuarioMecanico(req.getUsuarioId());
        if (repo.findByUsuarioId(usuario.getId()).isPresent()) {
            throw new DuplicateResourceException("El usuario ya tiene un perfil de mecanico");
        }
        Sucursal sucursal = buscarSucursal(req.getSucursalId());
        return toDTO(repo.save(new Mecanico(null, usuario, sucursal)));
    }

    @Override
    @Transactional(readOnly = true)
    public MecanicoResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MecanicoResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MecanicoResponseDTO> listarPorSucursal(Integer sucursalId) {
        buscarSucursal(sucursalId);
        return repo.findBySucursalId(sucursalId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public MecanicoResponseDTO actualizar(Integer id, MecanicoRequestDTO req) {
        Mecanico mecanico = buscar(id);
        if (!mecanico.getUsuario().getId().equals(req.getUsuarioId())) {
            throw new BusinessRuleException("No se puede cambiar el usuario asociado al mecanico");
        }
        mecanico.setSucursal(buscarSucursal(req.getSucursalId()));
        return toDTO(repo.save(mecanico));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        buscar(id);
        if (ordenRepo.existsByMecanicoId(id) || citaRepo.existsByMecanicoId(id)) {
            throw new BusinessRuleException("No se puede eliminar un mecanico con asignaciones");
        }
        repo.deleteById(id);
    }

    private Usuario buscarUsuarioMecanico(Integer usuarioId) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + usuarioId));
        if (usuario.getRol().getNombre() != NombreRol.MECANICO) {
            throw new BusinessRuleException("El usuario debe tener rol MECANICO");
        }
        return usuario;
    }

    private Sucursal buscarSucursal(Integer id) {
        return sucursalRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada: " + id));
    }

    private Mecanico buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mecanico no encontrado: " + id));
    }

    private MecanicoResponseDTO toDTO(Mecanico m) {
        BigDecimal horas = registroHorasRepo.sumHorasByMecanicoId(m.getId());
        return new MecanicoResponseDTO(
                m.getId(),
                m.getUsuario().getId(),
                m.getUsuario().getNombre(),
                m.getUsuario().getApellido(),
                m.getUsuario().getEmail(),
                m.getSucursal().getId(),
                m.getSucursal().getNombre(),
                horas != null ? horas : BigDecimal.ZERO);
    }

}
