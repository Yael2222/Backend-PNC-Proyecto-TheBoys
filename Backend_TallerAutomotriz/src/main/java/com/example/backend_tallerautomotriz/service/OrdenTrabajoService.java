package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.OrdenTrabajoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.OrdenTrabajoResponseDTO;
import com.example.backend_tallerautomotriz.enums.EstadoOrden;

import java.util.List;

public interface OrdenTrabajoService {
    OrdenTrabajoResponseDTO crear(OrdenTrabajoRequestDTO request);
    OrdenTrabajoResponseDTO obtenerPorId(Integer id);
    List<OrdenTrabajoResponseDTO> listarTodos();
    List<OrdenTrabajoResponseDTO> listarPorCliente(Integer clienteId);
    List<OrdenTrabajoResponseDTO> listarPorMecanico(Integer mecanicoId);
    OrdenTrabajoResponseDTO cambiarEstado(Integer id, EstadoOrden estado);
    void cancelar(Integer id);
}
