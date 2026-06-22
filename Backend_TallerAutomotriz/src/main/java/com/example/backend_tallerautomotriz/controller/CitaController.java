package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.CitaRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.ReprogramarCitaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.CitaResponseDTO;
import com.example.backend_tallerautomotriz.service.CitaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
@Validated
public class CitaController {

    private final CitaService citaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @citaAuthorization.esClientePropietario(authentication, #request.clienteId))")
    public ResponseEntity<CitaResponseDTO> crear(@Valid @RequestBody CitaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crear(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @citaAuthorization.esCitaDelMecanico(authentication, #id)) or (hasRole('CLIENTE') and @citaAuthorization.esCitaDelCliente(authentication, #id))")
    public ResponseEntity<CitaResponseDTO> obtener(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(citaService.obtenerPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @citaAuthorization.esClientePropietario(authentication, #clienteId))")
    public ResponseEntity<List<CitaResponseDTO>> listarPorCliente(@PathVariable @Positive Integer clienteId) {
        return ResponseEntity.ok(citaService.listarPorCliente(clienteId));
    }

    @GetMapping("/mecanico/{mecanicoId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @tallerAuthorization.esMecanicoPropietario(authentication, #mecanicoId))")
    public ResponseEntity<List<CitaResponseDTO>> listarPorMecanico(@PathVariable @Positive Integer mecanicoId) {
        return ResponseEntity.ok(citaService.listarPorMecanico(mecanicoId));
    }

    @GetMapping("/sucursal/{sucursalId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @tallerAuthorization.esSucursalDelMecanico(authentication, #sucursalId))")
    public ResponseEntity<List<CitaResponseDTO>> listarPorSucursal(
            @PathVariable @Positive Integer sucursalId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.listarPorSucursalYFecha(sucursalId, fecha));
    }

    /** Citas sin mecánico asignado — el mecánico las ve para aceptarlas */
    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and #sucursalId != null and @tallerAuthorization.esSucursalDelMecanico(authentication, #sucursalId))")
    public ResponseEntity<List<CitaResponseDTO>> listarPendientes(
            @RequestParam(required = false) @Positive Integer sucursalId) {
        return ResponseEntity.ok(citaService.listarPendientes(sucursalId));
    }

    /** Mecánico acepta una cita y queda asignado */
    @PatchMapping("/{id}/aceptar")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @tallerAuthorization.esMecanicoPropietario(authentication, #mecanicoId))")
    public ResponseEntity<CitaResponseDTO> aceptar(
            @PathVariable @Positive Integer id,
            @RequestParam @Positive Integer mecanicoId) {
        return ResponseEntity.ok(citaService.aceptar(id, mecanicoId));
    }

    /** Mecánico propone nueva fecha/hora */
    @PatchMapping("/{id}/reprogramar")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @citaAuthorization.esCitaDelMecanico(authentication, #id))")
    public ResponseEntity<CitaResponseDTO> reprogramar(
            @PathVariable @Positive Integer id,
            @Valid @RequestBody ReprogramarCitaRequestDTO request) {
        return ResponseEntity.ok(citaService.reprogramar(id, request));
    }

    /** Cliente acepta la reprogramación propuesta por el mecánico */
    @PatchMapping("/{id}/aceptar-reprogramacion")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @citaAuthorization.esCitaDelCliente(authentication, #id))")
    public ResponseEntity<CitaResponseDTO> aceptarReprogramacion(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(citaService.aceptarReprogramacion(id));
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @citaAuthorization.esCitaDelMecanico(authentication, #id))")
    public ResponseEntity<CitaResponseDTO> confirmar(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(citaService.confirmar(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @citaAuthorization.esCitaDelCliente(authentication, #id))")
    public ResponseEntity<Void> cancelar(@PathVariable @Positive Integer id) {
        citaService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
