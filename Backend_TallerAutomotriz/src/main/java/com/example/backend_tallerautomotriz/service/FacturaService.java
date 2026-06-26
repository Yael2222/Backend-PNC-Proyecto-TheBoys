package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.FacturaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.FacturaResponseDTO;

import java.util.List;

public interface FacturaService {
    FacturaResponseDTO obtenerPorOrden(Integer ordenId);
    FacturaResponseDTO obtenerPorId(Integer id);
    List<FacturaResponseDTO> listarTodas();
    List<FacturaResponseDTO> listarPorCliente(Integer clienteId);
    FacturaResponseDTO procesarPago(FacturaRequestDTO request);
    FacturaResponseDTO solicitarPagoEfectivo(Integer facturaId);
    FacturaResponseDTO confirmarPagoEfectivo(Integer facturaId);
}