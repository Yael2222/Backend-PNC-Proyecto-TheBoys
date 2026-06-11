package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.OrdenTrabajoRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.PresupuestoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.OrdenTrabajoResponseDTO;
import com.example.backend_tallerautomotriz.enums.EstadoOrden;
import java.util.List;

public interface OrdenTrabajoService {

    OrdenTrabajoResponseDTO crear(OrdenTrabajoRequestDTO req);

    OrdenTrabajoResponseDTO obtenerPorId(Integer id);

    List<OrdenTrabajoResponseDTO> listarTodos();

    List<OrdenTrabajoResponseDTO> listarPorCliente(Integer clienteId);

    List<OrdenTrabajoResponseDTO> listarPorMecanico(Integer mecanicoId);

    List<OrdenTrabajoResponseDTO> listarPorVehiculo(String patente);

    OrdenTrabajoResponseDTO cambiarEstado(Integer id, EstadoOrden estado);

    OrdenTrabajoResponseDTO enviarPresupuesto(Integer ordenId, PresupuestoRequestDTO req);

    OrdenTrabajoResponseDTO aprobarPresupuesto(Integer ordenId);

    OrdenTrabajoResponseDTO rechazarPresupuesto(Integer ordenId);

    OrdenTrabajoResponseDTO marcarCompletada(Integer ordenId);

    void cancelar(Integer id);
}
