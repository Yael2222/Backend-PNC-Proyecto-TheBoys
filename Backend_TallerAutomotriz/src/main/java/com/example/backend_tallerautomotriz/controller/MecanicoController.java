package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.MecanicoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.MecanicoResponseDTO;
import com.example.backend_tallerautomotriz.service.MecanicoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mecanicos")
@RequiredArgsConstructor
@Validated
public class MecanicoController {
    private final MecanicoService mecanicoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MecanicoResponseDTO> crear(@Valid @RequestBody MecanicoRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mecanicoService.crear(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<MecanicoResponseDTO>> listar() {
        return ResponseEntity.ok(mecanicoService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<MecanicoResponseDTO> obtener(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok(mecanicoService.obtenerPorId(id));
    }

    @GetMapping("/sucursal/{sucursalId}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<MecanicoResponseDTO>> listarPorSucursal(@PathVariable @Positive Integer sucursalId) {
        return ResponseEntity.ok(mecanicoService.listarPorSucursal(sucursalId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MecanicoResponseDTO> actualizar(@PathVariable @Positive Integer id, @Valid @RequestBody MecanicoRequestDTO req) {
        return ResponseEntity.ok(mecanicoService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Integer id) {
        mecanicoService.eliminar(id); return ResponseEntity.noContent().build();
    }
}
