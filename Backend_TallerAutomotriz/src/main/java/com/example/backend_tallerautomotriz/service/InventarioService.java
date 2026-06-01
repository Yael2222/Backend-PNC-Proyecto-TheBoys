package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.InventarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.InventarioResponseDTO;

import java.util.List;

public interface InventarioService {
    InventarioResponseDTO crear(InventarioRequestDTO request);
    InventarioResponseDTO obtenerPorId(Integer id);
    List<InventarioResponseDTO> listarPorSucursal(Integer sucursalId);
    InventarioResponseDTO actualizar(Integer id, InventarioRequestDTO request);
    void eliminar(Integer id);
}
