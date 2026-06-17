package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.CambiarRolRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.UsuarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.UsuarioResponseDTO;
import com.example.backend_tallerautomotriz.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable Integer id, @Valid @RequestBody UsuarioRequestDTO req) {
        return ResponseEntity.ok(usuarioService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id); return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desbloquear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desbloquear(@PathVariable Integer id) {
        usuarioService.desbloquear(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin cambia el rol de un usuario.
     * Para promover a MECANICO se requiere sucursalId en el body.
     * Ejemplo: PATCH /api/v1/usuarios/5/cambiar-rol
     * Body: { "nuevoRol": "MECANICO", "sucursalId": 2 }
     */
    @PatchMapping("/{id}/cambiar-rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> cambiarRol(
            @PathVariable Integer id, @Valid @RequestBody CambiarRolRequestDTO req) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, req));
    }
}

