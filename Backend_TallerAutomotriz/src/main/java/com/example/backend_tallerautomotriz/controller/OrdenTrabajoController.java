package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.OrdenTrabajoRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.PresupuestoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.OrdenTrabajoResponseDTO;
import com.example.backend_tallerautomotriz.enums.EstadoOrden;
import com.example.backend_tallerautomotriz.service.OrdenTrabajoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
@Validated
public class OrdenTrabajoController {

    private final OrdenTrabajoService ordenService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') " +
            "or (hasRole('MECANICO') and @tallerAuthorization.esMecanicoPropietario(authentication, #request.mecanicoId)) " +
            "or (hasRole('CLIENTE') and @tallerAuthorization.esClientePropietario(authentication, #request.clienteId))")
    public ResponseEntity<OrdenTrabajoResponseDTO> crear(@Valid @RequestBody OrdenTrabajoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.crear(request));
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MECANICO')")
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> listarPendientes(
            @RequestParam(required = false) @Positive Integer sucursalId) {
        return ResponseEntity.ok(ordenService.listarPendientes(sucursalId));
    }

    @PatchMapping("/{id}/asignar-mecanico")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') " +
            "and @tallerAuthorization.esMecanicoPropietario(authentication, #mecanicoId))")
    public ResponseEntity<OrdenTrabajoResponseDTO> asignarMecanico(
            @PathVariable @Positive Integer id,
            @RequestParam @Positive Integer mecanicoId) {
        return ResponseEntity.ok(ordenService.asignarMecanico(id, mecanicoId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> listar() {
        return ResponseEntity.ok(ordenService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @tallerAuthorization.esOrdenDelCliente(authentication, #id)) or (hasRole('MECANICO') and @tallerAuthorization.esOrdenAsignadaAlMecanico(authentication, #id))")
    public ResponseEntity<OrdenTrabajoResponseDTO> obtener(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(ordenService.obtenerPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @tallerAuthorization.esClientePropietario(authentication, #clienteId))")
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> listarPorCliente(@PathVariable @Positive Integer clienteId) {
        return ResponseEntity.ok(ordenService.listarPorCliente(clienteId));
    }

    @GetMapping("/mecanico/{mecanicoId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @tallerAuthorization.esMecanicoPropietario(authentication, #mecanicoId))")
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> listarPorMecanico(@PathVariable @Positive Integer mecanicoId) {
        return ResponseEntity.ok(ordenService.listarPorMecanico(mecanicoId));
    }

    @GetMapping("/vehiculo/{patente}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @tallerAuthorization.esVehiculoDelCliente(authentication, #patente)) or (hasRole('MECANICO') and @tallerAuthorization.esVehiculoDeOrdenAsignadaAlMecanico(authentication, #patente))")
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> listarPorVehiculo(
            @PathVariable @Pattern(regexp = "^[A-Za-z0-9-]{1,20}$") String patente) {
        return ResponseEntity.ok(ordenService.listarPorVehiculo(patente));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @tallerAuthorization.esOrdenAsignadaAlMecanico(authentication, #id))")
    public ResponseEntity<OrdenTrabajoResponseDTO> cambiarEstado(
            @PathVariable @Positive Integer id,
            @RequestParam @NotNull EstadoOrden estado) {
        return ResponseEntity.ok(ordenService.cambiarEstado(id, estado));
    }

    @PatchMapping("/{id}/presupuesto")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @tallerAuthorization.esOrdenAsignadaAlMecanico(authentication, #id))")
    public ResponseEntity<OrdenTrabajoResponseDTO> enviarPresupuesto(
            @PathVariable @Positive Integer id,
            @Valid @RequestBody PresupuestoRequestDTO request) {
        return ResponseEntity.ok(ordenService.enviarPresupuesto(id, request));
    }

    @PatchMapping("/{id}/aprobar-presupuesto")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @tallerAuthorization.esOrdenDelCliente(authentication, #id))")
    public ResponseEntity<OrdenTrabajoResponseDTO> aprobarPresupuesto(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(ordenService.aprobarPresupuesto(id));
    }

    @PatchMapping("/{id}/rechazar-presupuesto")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @tallerAuthorization.esOrdenDelCliente(authentication, #id))")
    public ResponseEntity<OrdenTrabajoResponseDTO> rechazarPresupuesto(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(ordenService.rechazarPresupuesto(id));
    }

    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @tallerAuthorization.esOrdenAsignadaAlMecanico(authentication, #id))")
    public ResponseEntity<OrdenTrabajoResponseDTO> marcarCompletada(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(ordenService.marcarCompletada(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MECANICO') and @tallerAuthorization.esOrdenAsignadaAlMecanico(authentication, #id))")
    public ResponseEntity<Void> cancelar(@PathVariable @Positive Integer id) {
        ordenService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}