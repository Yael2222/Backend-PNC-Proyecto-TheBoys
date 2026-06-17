package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.InventarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.InventarioResponseDTO;
import com.example.backend_tallerautomotriz.service.InventarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
@Validated
public class InventarioController {
    private final InventarioService inventarioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventarioResponseDTO> crear(@Valid @RequestBody InventarioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.crear(request));
    }
    @GetMapping("/sucursal/{sucursalId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @tallerAuthorization.esSucursalDelMecanico(authentication, #sucursalId))")
    public ResponseEntity<List<InventarioResponseDTO>> listarPorSucursal(@PathVariable @Positive Integer sucursalId) {
        return ResponseEntity.ok(inventarioService.listarPorSucursal(sucursalId));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @tallerAuthorization.esInventarioDeSucursalDelMecanico(authentication, #id))")
    public ResponseEntity<InventarioResponseDTO> obtener(@PathVariable @Positive Integer id) { return ResponseEntity.ok(inventarioService.obtenerPorId(id)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @tallerAuthorization.esInventarioDeSucursalDelMecanico(authentication, #id))")
    public ResponseEntity<InventarioResponseDTO> actualizar(@PathVariable @Positive Integer id, @Valid @RequestBody InventarioRequestDTO request) {
        return ResponseEntity.ok(inventarioService.actualizar(id, request));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Integer id) {
        inventarioService.eliminar(id); return ResponseEntity.noContent().build();
    }
}

