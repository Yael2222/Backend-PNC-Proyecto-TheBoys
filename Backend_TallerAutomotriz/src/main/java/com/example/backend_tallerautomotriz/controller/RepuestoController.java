package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.request.RepuestoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.RepuestoResponseDTO;
import com.example.backend_tallerautomotriz.service.RepuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/repuestos")
@RequiredArgsConstructor
public class RepuestoController {

    private final RepuestoService repuestoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RepuestoResponseDTO> crear(@Valid @RequestBody RepuestoRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repuestoService.crear(req));
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<RepuestoResponseDTO>> listar() { return ResponseEntity.ok(repuestoService.listarTodos()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<RepuestoResponseDTO> obtener(@PathVariable Integer id) { return ResponseEntity.ok(repuestoService.obtenerPorId(id)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RepuestoResponseDTO> actualizar(@PathVariable Integer id, @Valid @RequestBody RepuestoRequestDTO req) {
        return ResponseEntity.ok(repuestoService.actualizar(id, req));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        repuestoService.eliminar(id); return ResponseEntity.noContent().build();
    }

    /** GET /api/v1/repuestos/categoria/MOTOR */
    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<RepuestoResponseDTO>> listarPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(repuestoService.listarPorCategoria(categoria));
    }

    /** GET /api/v1/repuestos/buscar?nombre=filtro */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    public ResponseEntity<List<RepuestoResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(repuestoService.buscarPorNombre(nombre));
    }

}