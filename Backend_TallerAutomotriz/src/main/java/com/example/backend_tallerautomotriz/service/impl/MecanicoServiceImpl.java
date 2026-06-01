package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.MecanicoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.MecanicoResponseDTO;
import com.example.backend_tallerautomotriz.entity.Mecanico;
import com.example.backend_tallerautomotriz.entity.Sucursal;
import com.example.backend_tallerautomotriz.entity.Usuario;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.MecanicoRepository;
import com.example.backend_tallerautomotriz.repository.SucursalRepository;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;
import com.example.backend_tallerautomotriz.service.MecanicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class MecanicoServiceImpl implements MecanicoService {
    private final MecanicoRepository repo;
    private final UsuarioRepository usuarioRepo;
    private final SucursalRepository sucursalRepo;

    @Override public MecanicoResponseDTO crear(MecanicoRequestDTO req) {
        Usuario u = usuarioRepo.findById(req.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        Sucursal s = sucursalRepo.findById(req.getSucursalId())
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
        return toDTO(repo.save(new Mecanico(null, u, s)));
    }
    @Override public MecanicoResponseDTO obtenerPorId(Integer id) { return toDTO(buscar(id)); }
    @Override public List<MecanicoResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public List<MecanicoResponseDTO> listarPorSucursal(Integer sucursalId) {
        return repo.findBySucursalId(sucursalId).stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public MecanicoResponseDTO actualizar(Integer id, MecanicoRequestDTO req) {
        Mecanico m = buscar(id);
        Sucursal s = sucursalRepo.findById(req.getSucursalId())
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
        m.setSucursal(s);
        return toDTO(repo.save(m));
    }
    @Override public void eliminar(Integer id) { buscar(id); repo.deleteById(id); }

    private Mecanico buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Mecánico no encontrado: " + id));
    }
    private MecanicoResponseDTO toDTO(Mecanico m) {
        return new MecanicoResponseDTO(m.getId(), m.getUsuario().getNombre(),
                m.getUsuario().getApellido(), m.getUsuario().getEmail(), m.getSucursal().getNombre());
    }
}

