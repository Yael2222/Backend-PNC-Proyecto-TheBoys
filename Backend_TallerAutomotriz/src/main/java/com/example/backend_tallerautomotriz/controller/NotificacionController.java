package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.response.NotificacionResponseDTO;
import com.example.backend_tallerautomotriz.service.NotificacionService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Validated
public class NotificacionController {

    private final NotificacionService notificacionService;

    /** Todas las notificaciones del usuario */
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN') or @tallerAuthorization.esUsuarioPropietario(authentication, #usuarioId)")
    public ResponseEntity<List<NotificacionResponseDTO>> listar(@PathVariable @Positive Integer usuarioId) {
        return ResponseEntity.ok(notificacionService.listarPorUsuario(usuarioId));
    }

    /** Solo las no leídas */
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    @PreAuthorize("hasRole('ADMIN') or @tallerAuthorization.esUsuarioPropietario(authentication, #usuarioId)")
    public ResponseEntity<List<NotificacionResponseDTO>> listarNoLeidas(@PathVariable @Positive Integer usuarioId) {
        return ResponseEntity.ok(notificacionService.listarNoLeidas(usuarioId));
    }

    /** Contador de no leídas (para el badge del frontend) */
    @GetMapping("/usuario/{usuarioId}/contador")
    @PreAuthorize("hasRole('ADMIN') or @tallerAuthorization.esUsuarioPropietario(authentication, #usuarioId)")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(@PathVariable @Positive Integer usuarioId) {
        return ResponseEntity.ok(Map.of("noLeidas", notificacionService.contarNoLeidas(usuarioId)));
    }

    /** Marcar una notificación como leída */
    @PatchMapping("/{id}/leer")
    @PreAuthorize("hasRole('ADMIN') or @tallerAuthorization.esNotificacionDelUsuario(authentication, #id)")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable @Positive Integer id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.noContent().build();
    }

    /** Marcar todas como leídas */
    @PatchMapping("/usuario/{usuarioId}/leer-todas")
    @PreAuthorize("hasRole('ADMIN') or @tallerAuthorization.esUsuarioPropietario(authentication, #usuarioId)")
    public ResponseEntity<Void> marcarTodasComoLeidas(@PathVariable @Positive Integer usuarioId) {
        notificacionService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
