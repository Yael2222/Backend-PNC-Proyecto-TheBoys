package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.FacturaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.FacturaResponseDTO;

import java.util.List;

public interface FacturaService {
    FacturaResponseDTO obtenerPorOrden(Integer ordenId);
    FacturaResponseDTO obtenerPorId(Integer id);
    List<FacturaResponseDTO> listarTodas();
    FacturaResponseDTO procesarPago(FacturaRequestDTO request);
}