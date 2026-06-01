package com.example.backend_tallerautomotriz.controller;


import com.example.backend_tallerautomotriz.dto.request.CitaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.CitaResponseDTO;
import com.example.backend_tallerautomotriz.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/citas") @RequiredArgsConstructor
public class CitaController {
    private final CitaService citaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<CitaResponseDTO> crear(@Valid @RequestBody CitaRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crear(req));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<CitaResponseDTO> obtener(@PathVariable Integer id) { return ResponseEntity.ok(citaService.obtenerPorId(id)); }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<List<CitaResponseDTO>> listarPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(citaService.listarPorCliente(clienteId));
    }
    @GetMapping("/sucursal/{sucursalId}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<CitaResponseDTO>> listarPorSucursal(@PathVariable Integer sucursalId, @RequestParam String fecha) {
        return ResponseEntity.ok(citaService.listarPorSucursalYFecha(sucursalId, fecha));
    }
    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<CitaResponseDTO> confirmar(@PathVariable Integer id) { return ResponseEntity.ok(citaService.confirmar(id)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<Void> cancelar(@PathVariable Integer id) {
        citaService.cancelar(id); return ResponseEntity.noContent().build();
    }
}
