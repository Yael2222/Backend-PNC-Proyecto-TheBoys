package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.RegistroHorasRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.RegistroHorasResponseDTO;

import java.util.List;

public interface RegistroHorasService {
    RegistroHorasResponseDTO registrar(RegistroHorasRequestDTO request);
    List<RegistroHorasResponseDTO> listarPorOrden(Integer ordenId);
    List<RegistroHorasResponseDTO> listarPorMecanico(Integer mecanicoId);
    void eliminar(Integer id);
}
