package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.CitaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.CitaResponseDTO;

import java.util.List;

public interface CitaService {
    CitaResponseDTO crear(CitaRequestDTO request);
    CitaResponseDTO obtenerPorId(Integer id);
    List<CitaResponseDTO> listarPorCliente(Integer clienteId);
    List<CitaResponseDTO> listarPorSucursalYFecha(Integer sucursalId, String fecha);
    CitaResponseDTO confirmar(Integer id);
    void cancelar(Integer id);
}
