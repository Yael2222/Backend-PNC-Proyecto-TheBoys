package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.OrdenTrabajoRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.PresupuestoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.OrdenTrabajoResponseDTO;
import com.example.backend_tallerautomotriz.enums.EstadoOrden;
import com.example.backend_tallerautomotriz.service.OrdenTrabajoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
public class OrdenTrabajoController {

    private final OrdenTrabajoService ordenService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<OrdenTrabajoResponseDTO> crear(@Valid @RequestBody OrdenTrabajoRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.crear(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> listar() {
        return ResponseEntity.ok(ordenService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<OrdenTrabajoResponseDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(ordenService.obtenerPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> listarPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(ordenService.listarPorCliente(clienteId));
    }

    @GetMapping("/mecanico/{mecanicoId}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> listarPorMecanico(@PathVariable Integer mecanicoId) {
        return ResponseEntity.ok(ordenService.listarPorMecanico(mecanicoId));
    }

    @GetMapping("/vehiculo/{patente}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> listarPorVehiculo(@PathVariable String patente) {
        return ResponseEntity.ok(ordenService.listarPorVehiculo(patente));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<OrdenTrabajoResponseDTO> cambiarEstado(
            @PathVariable Integer id, @RequestParam EstadoOrden estado) {
        return ResponseEntity.ok(ordenService.cambiarEstado(id, estado));
    }

    /** Mecánico envía presupuesto → notifica al cliente, orden pasa a PENDIENTE_APROBACION */
    @PatchMapping("/{id}/presupuesto")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<OrdenTrabajoResponseDTO> enviarPresupuesto(
            @PathVariable Integer id, @Valid @RequestBody PresupuestoRequestDTO req) {
        return ResponseEntity.ok(ordenService.enviarPresupuesto(id, req));
    }

    /** Cliente aprueba el presupuesto → orden pasa a EN_PROGRESO */
    @PatchMapping("/{id}/aprobar-presupuesto")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<OrdenTrabajoResponseDTO> aprobarPresupuesto(@PathVariable Integer id) {
        return ResponseEntity.ok(ordenService.aprobarPresupuesto(id));
    }

    /** Cliente rechaza el presupuesto → orden vuelve a PENDIENTE */
    @PatchMapping("/{id}/rechazar-presupuesto")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<OrdenTrabajoResponseDTO> rechazarPresupuesto(@PathVariable Integer id) {
        return ResponseEntity.ok(ordenService.rechazarPresupuesto(id));
    }

    /** Mecánico marca la orden como completada → notifica al cliente y genera factura */
    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<OrdenTrabajoResponseDTO> marcarCompletada(@PathVariable Integer id) {
        return ResponseEntity.ok(ordenService.marcarCompletada(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<Void> cancelar(@PathVariable Integer id) {
        ordenService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
