package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.ClienteRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ClienteResponseDTO;
import com.example.backend_tallerautomotriz.service.ClienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Validated
public class ClienteController {
    private final ClienteService clienteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @tallerAuthorization.esClientePropietario(authentication, #id))")
    public ResponseEntity<ClienteResponseDTO> obtener(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @tallerAuthorization.esUsuarioPropietario(authentication, #usuarioId))")
    public ResponseEntity<ClienteResponseDTO> obtenerPorUsuarioId(@PathVariable @Positive Integer usuarioId) {
        return ResponseEntity.ok(clienteService.obtenerPorUsuarioId(usuarioId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @tallerAuthorization.esClientePropietario(authentication, #id))")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @PathVariable @Positive Integer id,
            @Valid @RequestBody ClienteRequestDTO request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Integer id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}