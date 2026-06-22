package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.SucursalRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.SucursalResponseDTO;
import com.example.backend_tallerautomotriz.service.SucursalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sucursales")
@RequiredArgsConstructor
@Validated
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
    public ResponseEntity<SucursalResponseDTO> obtener(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(sucursalService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SucursalResponseDTO> actualizar(@PathVariable @Positive Integer id, @Valid @RequestBody SucursalRequestDTO req) {
        return ResponseEntity.ok(sucursalService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Integer id) {
        sucursalService.eliminar(id); return ResponseEntity.noContent().build();
    }
}

