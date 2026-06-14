package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.ClienteRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ClienteResponseDTO;
import com.example.backend_tallerautomotriz.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<ClienteResponseDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<ClienteResponseDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody ClienteRequestDTO req) {
        return ResponseEntity.ok(clienteService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        clienteService.eliminar(id); return ResponseEntity.noContent().build();
    }
}

