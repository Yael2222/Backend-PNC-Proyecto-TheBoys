package com.example.backend_tallerautomotriz.controller;


import com.example.backend_tallerautomotriz.dto.request.FacturaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.FacturaResponseDTO;
import com.example.backend_tallerautomotriz.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/facturas") @RequiredArgsConstructor
public class FacturaController {
    private final FacturaService facturaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FacturaResponseDTO>> listar() { return ResponseEntity.ok(facturaService.listarTodas()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<FacturaResponseDTO> obtener(@PathVariable Integer id) { return ResponseEntity.ok(facturaService.obtenerPorId(id)); }

    @GetMapping("/orden/{ordenId}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO','CLIENTE')")
    public ResponseEntity<FacturaResponseDTO> obtenerPorOrden(@PathVariable Integer ordenId) {
        return ResponseEntity.ok(facturaService.obtenerPorOrden(ordenId));
    }
    @PostMapping("/pagar")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<FacturaResponseDTO> procesarPago(@Valid @RequestBody FacturaRequestDTO req) {
        return ResponseEntity.ok(facturaService.procesarPago(req));
    }
}
