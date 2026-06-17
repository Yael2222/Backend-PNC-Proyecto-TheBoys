package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.VehiculoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.VehiculoResponseDTO;
import com.example.backend_tallerautomotriz.service.VehiculoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/vehiculos") @RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<VehiculoResponseDTO> crear(@Valid @RequestBody VehiculoRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crear(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public ResponseEntity<List<VehiculoResponseDTO>> listar() {
        return ResponseEntity.ok(vehiculoService.listarTodos());
    }

    @GetMapping("/{patente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO') " +
            "or (hasRole('CLIENTE') and @tallerAuthorization.esVehiculoDelCliente(authentication, #patente))")
    public ResponseEntity<VehiculoResponseDTO> obtener(
            @PathVariable @Pattern(regexp = "^[A-Za-z0-9-]{1,20}$") String patente) {
        return ResponseEntity.ok(vehiculoService.obtenerPorPatente(patente));
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO') " +
            "or (hasRole('CLIENTE') and @tallerAuthorization.esClientePropietario(authentication, #clienteId))")
    public ResponseEntity<List<VehiculoResponseDTO>> listarPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(vehiculoService.listarPorCliente(clienteId));
    }

    @PutMapping("/{patente}")
    @PreAuthorize("hasRole('ADMIN') " +
            "or (hasRole('CLIENTE') and @tallerAuthorization.esVehiculoDelCliente(authentication, #patente))")
    public ResponseEntity<VehiculoResponseDTO> actualizar(
            @PathVariable @Pattern(regexp = "^[A-Za-z0-9-]{1,20}$") String patente,
            @Valid @RequestBody VehiculoRequestDTO req,
            Authentication authentication) {
        return ResponseEntity.ok(vehiculoService.actualizar(patente, req, authentication.getName()));
    }

    @DeleteMapping("/{patente}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Pattern(regexp = "^[A-Za-z0-9-]{1,20}$") String patente) {
        vehiculoService.eliminar(patente);
        return ResponseEntity.noContent().build();
    }
}

