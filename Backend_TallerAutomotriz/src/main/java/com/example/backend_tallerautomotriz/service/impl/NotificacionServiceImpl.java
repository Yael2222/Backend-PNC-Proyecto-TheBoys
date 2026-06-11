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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepo;
    private final UsuarioRepository usuarioRepo;

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarPorUsuario(Integer usuarioId) {
        validarUsuarioExiste(usuarioId);
        return notificacionRepo.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarNoLeidas(Integer usuarioId) {
        validarUsuarioExiste(usuarioId);
        return notificacionRepo.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidas(Integer usuarioId) {
        validarUsuarioExiste(usuarioId);
        return notificacionRepo.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Override
    @Transactional
    public void marcarComoLeida(Integer notificacionId) {
        Notificacion notificacion = notificacionRepo.findById(notificacionId)
                .orElseThrow(() -> new EntityNotFoundException("Notificacion no encontrada: " + notificacionId));
        if (!notificacion.isLeida()) {
            notificacion.setLeida(true);
            notificacionRepo.save(notificacion);
        }
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidas(Integer usuarioId) {
        validarUsuarioExiste(usuarioId);
        notificacionRepo.marcarTodasComoLeidas(usuarioId);
    }

    @Override
    @Transactional
    public void crear(Integer usuarioId, String mensaje, String tipo, Integer referenciaId) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + usuarioId));
        Notificacion notificacion = new Notificacion(
                null,
                usuario,
                mensaje,
                false,
                LocalDateTime.now(),
                tipo,
                referenciaId);
        notificacionRepo.save(notificacion);
    }

    private void validarUsuarioExiste(Integer usuarioId) {
        if (!usuarioRepo.existsById(usuarioId)) {
            throw new EntityNotFoundException("Usuario no encontrado: " + usuarioId);
        }
    }

    private NotificacionResponseDTO toDTO(Notificacion notificacion) {
        return new NotificacionResponseDTO(
                notificacion.getId(),
                notificacion.getMensaje(),
                notificacion.isLeida(),
                notificacion.getFechaCreacion(),
                notificacion.getTipo(),
                notificacion.getReferenciaId());
    }
}
