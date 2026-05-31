package com.example.backend_tallerautomotriz.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sucursales")
@RequiredArgsConstructor
public class SucursalController {
    private final SucursalService sucursalService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SucursalResponseDTO> crear(@Valid @RequestBody SucursalRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sucursalService.crear(req));
    }

    @GetMapping
    public ResponseEntity<List<SucursalResponseDTO>> listar() {
        return ResponseEntity.ok(sucursalService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalResponseDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(sucursalService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SucursalResponseDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody SucursalRequestDTO req) {
        return ResponseEntity.ok(sucursalService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        sucursalService.eliminar(id); return ResponseEntity.noContent().build();
    }
}

