package com.example.backend_tallerautomotriz.controller;


import com.example.backend_tallerautomotriz.dto.request.FacturaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.FacturaResponseDTO;
import com.example.backend_tallerautomotriz.service.FacturaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/facturas")
@RequiredArgsConstructor
@Validated
public class FacturaController {
    private final FacturaService facturaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FacturaResponseDTO>> listar() { return ResponseEntity.ok(facturaService.listarTodas()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @tallerAuthorization.esFacturaDelCliente(authentication, #id))")
    public ResponseEntity<FacturaResponseDTO> obtener(@PathVariable @Positive Integer id) { return ResponseEntity.ok(facturaService.obtenerPorId(id)); }

    @GetMapping("/orden/{ordenId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @tallerAuthorization.esFacturaDeOrdenDelCliente(authentication, #ordenId)) or (hasRole('MECANICO') and @tallerAuthorization.esOrdenAsignadaAlMecanico(authentication, #ordenId))")
    public ResponseEntity<FacturaResponseDTO> obtenerPorOrden(@PathVariable @Positive Integer ordenId) {
        return ResponseEntity.ok(facturaService.obtenerPorOrden(ordenId));
    }
    @PostMapping("/pagar")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and @tallerAuthorization.esFacturaDeOrdenDelCliente(authentication, #request.ordenId))")
    public ResponseEntity<FacturaResponseDTO> procesarPago(@Valid @RequestBody FacturaRequestDTO request) {
        return ResponseEntity.ok(facturaService.procesarPago(request));
    }
}
