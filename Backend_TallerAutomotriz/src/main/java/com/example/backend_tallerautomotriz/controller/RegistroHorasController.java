package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.RegistroHorasRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.RegistroHorasResponseDTO;
import com.example.backend_tallerautomotriz.service.RegistroHorasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/horas") @RequiredArgsConstructor
public class RegistroHorasController {
    private final RegistroHorasService registroHorasService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<RegistroHorasResponseDTO> registrar(@Valid @RequestBody RegistroHorasRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroHorasService.registrar(req));
    }
    @GetMapping("/orden/{ordenId}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<RegistroHorasResponseDTO>> listarPorOrden(@PathVariable Integer ordenId) {
        return ResponseEntity.ok(registroHorasService.listarPorOrden(ordenId));
    }
    @GetMapping("/mecanico/{mecanicoId}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<RegistroHorasResponseDTO>> listarPorMecanico(@PathVariable Integer mecanicoId) {
        return ResponseEntity.ok(registroHorasService.listarPorMecanico(mecanicoId));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        registroHorasService.eliminar(id); return ResponseEntity.noContent().build();
    }
}
