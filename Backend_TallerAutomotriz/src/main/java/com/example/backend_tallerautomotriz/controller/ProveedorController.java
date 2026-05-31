package com.example.backend_tallerautomotriz.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/proveedores") @RequiredArgsConstructor
public class ProveedorController {
    private final ProveedorService proveedorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProveedorResponseDTO> crear(@Valid @RequestBody ProveedorRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crear(req));
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<ProveedorResponseDTO>> listar() { return ResponseEntity.ok(proveedorService.listarTodos()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<ProveedorResponseDTO> obtener(@PathVariable Integer id) { return ResponseEntity.ok(proveedorService.obtenerPorId(id)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProveedorResponseDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody ProveedorRequestDTO req) {
        return ResponseEntity.ok(proveedorService.actualizar(id, req));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        proveedorService.eliminar(id); return ResponseEntity.noContent().build();
    }
}

