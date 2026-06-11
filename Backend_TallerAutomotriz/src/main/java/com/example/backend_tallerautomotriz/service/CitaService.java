package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.CitaRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.ReprogramarCitaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.CitaResponseDTO;
import java.util.List;

public interface CitaService {

    CitaResponseDTO crear(CitaRequestDTO req);

    CitaResponseDTO obtenerPorId(Integer id);

    List<CitaResponseDTO> listarPorCliente(Integer clienteId);

    List<CitaResponseDTO> listarPorMecanico(Integer mecanicoId);

    List<CitaResponseDTO> listarPorSucursalYFecha(Integer sucursalId, String fecha);

    List<CitaResponseDTO> listarPendientes();

    CitaResponseDTO aceptar(Integer citaId, Integer mecanicoId);

    CitaResponseDTO reprogramar(Integer citaId, ReprogramarCitaRequestDTO req);

    CitaResponseDTO aceptarReprogramacion(Integer citaId);

    CitaResponseDTO confirmar(Integer id);

    void cancelar(Integer id);
}
