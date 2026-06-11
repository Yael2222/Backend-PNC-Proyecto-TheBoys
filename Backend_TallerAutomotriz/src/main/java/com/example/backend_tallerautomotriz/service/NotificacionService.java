package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.response.NotificacionResponseDTO;
import java.util.List;

public interface NotificacionService {

    List<NotificacionResponseDTO> listarPorUsuario(Integer usuarioId);

    List<NotificacionResponseDTO> listarNoLeidas(Integer usuarioId);

    long contarNoLeidas(Integer usuarioId);

    void marcarComoLeida(Integer notificacionId);

    void marcarTodasComoLeidas(Integer usuarioId);

    void crear(Integer usuarioId, String mensaje, String tipo, Integer referenciaId);
}
