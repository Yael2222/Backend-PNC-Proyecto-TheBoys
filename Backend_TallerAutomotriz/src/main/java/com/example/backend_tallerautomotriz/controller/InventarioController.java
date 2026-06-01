package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.InventarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.InventarioResponseDTO;
import com.example.backend_tallerautomotriz.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/inventario") @RequiredArgsConstructor
public class InventarioController {
    private final InventarioService inventarioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventarioResponseDTO> crear(@Valid @RequestBody InventarioRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.crear(req));
    }
    @GetMapping("/sucursal/{sucursalId}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<InventarioResponseDTO>> listarPorSucursal(@PathVariable Integer sucursalId) {
        return ResponseEntity.ok(inventarioService.listarPorSucursal(sucursalId));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<InventarioResponseDTO> obtener(@PathVariable Integer id) { return ResponseEntity.ok(inventarioService.obtenerPorId(id)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<InventarioResponseDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody InventarioRequestDTO req) {
        return ResponseEntity.ok(inventarioService.actualizar(id, req));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        inventarioService.eliminar(id); return ResponseEntity.noContent().build();
    }
}

