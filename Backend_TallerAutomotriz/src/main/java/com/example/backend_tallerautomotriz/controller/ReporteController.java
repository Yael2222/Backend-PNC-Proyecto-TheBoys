package com.example.backend_tallerautomotriz.controller;

import com.example.backend_tallerautomotriz.dto.response.MecanicoResponseDTO;
import com.example.backend_tallerautomotriz.dto.response.OrdenTrabajoResponseDTO;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.repository.OrdenTrabajoRepository;
import com.example.backend_tallerautomotriz.repository.RegistroHorasRepository;
import com.example.backend_tallerautomotriz.repository.RepuestoRepository;
import com.example.backend_tallerautomotriz.service.MecanicoService;
import com.example.backend_tallerautomotriz.service.OrdenTrabajoService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class ReporteController {

    private final OrdenTrabajoRepository ordenRepo;
    private final RegistroHorasRepository registroHorasRepo;
    private final RepuestoRepository repuestoRepo;
    private final OrdenTrabajoService ordenService;
    private final MecanicoService mecanicoService;

    @GetMapping("/ordenes")
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> ordenesPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) @Positive Integer sucursalId) {

        if (desde.isAfter(hasta)) {
            throw new BusinessRuleException("La fecha desde no puede ser posterior a la fecha hasta");
        }

        var ordenes = sucursalId != null
                ? ordenRepo.findBySucursalIdAndFechaCreacionBetween(sucursalId, desde, hasta)
                : ordenRepo.findByFechaCreacionBetween(desde, hasta);

        return ResponseEntity.ok(
                ordenes.stream().map(o -> ordenService.obtenerPorId(o.getId())).collect(Collectors.toList()));
    }


    @GetMapping("/mecanicos/horas")
    public ResponseEntity<List<Map<String, Object>>> mecanicosPorHoras() {
        List<Object[]> rows = registroHorasRepo.findHorasTotalesPorMecanico();
        List<Map<String, Object>> resultado = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("mecanicoId", r[0]);
            m.put("nombre",     r[1] + " " + r[2]);
            m.put("horasTotales", r[3]);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    /**
     * Repuestos más usados en órdenes.
     * GET /api/v1/reportes/repuestos/mas-usados
     */
    @GetMapping("/repuestos/mas-usados")
    public ResponseEntity<List<Map<String, Object>>> repuestosMasUsados() {
        List<Object[]> rows = repuestoRepo.findRepuestosMasUsados();
        List<Map<String, Object>> resultado = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("repuestoId", r[0]);
            m.put("nombre",     r[1]);
            m.put("cantidadTotal", r[2]);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    /**
     * Cantidad de órdenes por sucursal.
     * GET /api/v1/reportes/ordenes/por-sucursal
     */
    @GetMapping("/ordenes/por-sucursal")
    public ResponseEntity<List<Map<String, Object>>> ordenesPorSucursal() {
        List<Object[]> rows = ordenRepo.countOrdenesPorSucursal();
        List<Map<String, Object>> resultado = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sucursalId",    r[0]);
            m.put("sucursal",      r[1]);
            m.put("totalOrdenes",  r[2]);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    /**
     * Resumen global: total ordenes, completadas, en progreso, canceladas.
     * GET /api/v1/reportes/resumen
     */
    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> resumenGlobal() {
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalOrdenes",      ordenRepo.count());
        resumen.put("totalMecanicos",    mecanicoService.listarTodos().size());
        resumen.put("ordenesPorEstado",  ordenRepo.countByEstado());
        return ResponseEntity.ok(resumen);
    }
}
