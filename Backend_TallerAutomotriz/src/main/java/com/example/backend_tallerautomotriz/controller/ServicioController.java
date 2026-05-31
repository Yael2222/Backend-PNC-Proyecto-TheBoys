package com.example.backend_tallerautomotriz.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/servicios") @RequiredArgsConstructor
public class ServicioController {
    private final ServicioService servicioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioResponseDTO> crear(@Valid @RequestBody ServicioRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioService.crear(req));
    }
    @GetMapping
    public ResponseEntity<List<ServicioResponseDTO>> listar() { return ResponseEntity.ok(servicioService.listarActivos()); }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServicioResponseDTO>> listarTodos() { return ResponseEntity.ok(servicioService.listarTodos()); }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponseDTO> obtener(@PathVariable Integer id) { return ResponseEntity.ok(servicioService.obtenerPorId(id)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioResponseDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody ServicioRequestDTO req) {
        return ResponseEntity.ok(servicioService.actualizar(id, req));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        servicioService.desactivar(id); return ResponseEntity.noContent().build();
    }
}

