package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.ProveedorRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ProveedorResponseDTO;

import java.util.List;

public interface ProveedorService {
    ProveedorResponseDTO crear(ProveedorRequestDTO request);
    ProveedorResponseDTO obtenerPorId(Integer id);
    List<ProveedorResponseDTO> listarTodos();
    ProveedorResponseDTO actualizar(Integer id, ProveedorRequestDTO request);
    void eliminar(Integer id);
}
