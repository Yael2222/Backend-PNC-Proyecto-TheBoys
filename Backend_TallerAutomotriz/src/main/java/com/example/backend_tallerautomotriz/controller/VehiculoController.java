package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.VehiculoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.VehiculoResponseDTO;
import com.example.backend_tallerautomotriz.service.VehiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/vehiculos") @RequiredArgsConstructor
public class VehiculoController {
    private final VehiculoService vehiculoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<VehiculoResponseDTO> crear(@Valid @RequestBody VehiculoRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crear(req));
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<VehiculoResponseDTO>> listar() { return ResponseEntity.ok(vehiculoService.listarTodos()); }

    @GetMapping("/{patente}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<VehiculoResponseDTO> obtener(@PathVariable String patente) {
        return ResponseEntity.ok(vehiculoService.obtenerPorPatente(patente));
    }
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<List<VehiculoResponseDTO>> listarPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(vehiculoService.listarPorCliente(clienteId));
    }
    @PutMapping("/{patente}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<VehiculoResponseDTO> actualizar(@PathVariable String patente, @Valid @RequestBody VehiculoRequestDTO req) {
        return ResponseEntity.ok(vehiculoService.actualizar(patente, req));
    }
    @DeleteMapping("/{patente}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable String patente) {
        vehiculoService.eliminar(patente); return ResponseEntity.noContent().build();
    }
}

