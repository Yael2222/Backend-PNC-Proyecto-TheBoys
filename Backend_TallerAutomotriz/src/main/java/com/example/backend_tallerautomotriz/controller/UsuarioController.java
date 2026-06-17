package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.CambiarRolRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.UsuarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.UsuarioUpdatePasswordDTO;
import com.example.backend_tallerautomotriz.dto.response.UsuarioResponseDTO;
import com.example.backend_tallerautomotriz.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioRequestDTO req) {
        return ResponseEntity.ok(usuarioService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, Authentication authentication) {
        usuarioService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desbloquear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> desbloquear(@PathVariable Integer id) {
        usuarioService.desbloquear(id);
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/cambiar-rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> cambiarRol(
            @PathVariable Integer id, @Valid @RequestBody CambiarRolRequestDTO req) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, req));
    }

    //Permite modificar el propio perfil para cualquier rol autenticado
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerPerfil(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> actualizarPerfil(
            Authentication authentication,
            @Valid @RequestBody UsuarioRequestDTO req) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(authentication.getName(), req));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> cambiarPassword(
            Authentication authentication,
            @Valid @RequestBody UsuarioUpdatePasswordDTO req) {
        usuarioService.cambiarPassword(authentication.getName(), req);
        return ResponseEntity.noContent().build();
    }
}

