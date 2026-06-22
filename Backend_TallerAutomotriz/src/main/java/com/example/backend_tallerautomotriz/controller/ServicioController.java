package com.example.backend_tallerautomotriz.controller;


import com.example.backend_tallerautomotriz.dto.request.ServicioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ServicioResponseDTO;
import com.example.backend_tallerautomotriz.service.ServicioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/servicios") @RequiredArgsConstructor @Validated
public class ServicioController {

    private final ServicioService servicioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioResponseDTO> crear(@Valid @RequestBody ServicioRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioService.crear(req));
    }

    @GetMapping
    public ResponseEntity<List<ServicioResponseDTO>> listar() {
        return ResponseEntity.ok(servicioService.listarActivos());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServicioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(servicioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponseDTO> obtener(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(servicioService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioResponseDTO> actualizar(
            @PathVariable @Positive Integer id, @Valid @RequestBody ServicioRequestDTO req) {
        return ResponseEntity.ok(servicioService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable @Positive Integer id) {
        servicioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioResponseDTO> reactivar(@PathVariable @Positive Integer id) {
        servicioService.reactivar(id);
        return ResponseEntity.ok(servicioService.obtenerPorId(id));
    }
}

