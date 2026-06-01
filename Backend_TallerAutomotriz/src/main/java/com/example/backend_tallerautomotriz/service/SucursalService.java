package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.SucursalRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.SucursalResponseDTO;

import java.util.List;

public interface SucursalService {
    SucursalResponseDTO crear(SucursalRequestDTO request);
    SucursalResponseDTO obtenerPorId(Integer id);
    List<SucursalResponseDTO> listarTodos();
    SucursalResponseDTO actualizar(Integer id, SucursalRequestDTO request);
    void eliminar(Integer id);
}

