package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.response.NotificacionResponseDTO;
import com.example.backend_tallerautomotriz.entity.Notificacion;
import com.example.backend_tallerautomotriz.entity.Usuario;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.NotificacionRepository;
import com.example.backend_tallerautomotriz.repository.UsuarioRepository;
import com.example.backend_tallerautomotriz.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository repo;
    private final UsuarioRepository usuarioRepo;

    @Override
    public List<NotificacionResponseDTO> listarPorUsuario(Integer usuarioId) {
        return repo.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<NotificacionResponseDTO> listarNoLeidas(Integer usuarioId) {
        return repo.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public long contarNoLeidas(Integer usuarioId) {
        return repo.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Override
    @Transactional
    public void marcarComoLeida(Integer notificacionId) {
        Notificacion n = repo.findById(notificacionId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada: " + notificacionId));
        n.setLeida(true);
        repo.save(n);
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidas(Integer usuarioId) {
        repo.marcarTodasComoLeidas(usuarioId);
    }

    @Override
    @Transactional
    public void crear(Integer usuarioId, String mensaje, String tipo, Integer referenciaId) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + usuarioId));
        Notificacion n = new Notificacion(null, usuario, mensaje, false, null, tipo, referenciaId);
        repo.save(n);
    }

    private NotificacionResponseDTO toDTO(Notificacion n) {
        return new NotificacionResponseDTO(
                n.getId(), n.getMensaje(), n.isLeida(),
                n.getFechaCreacion(), n.getTipo(), n.getReferenciaId());
    }
}
