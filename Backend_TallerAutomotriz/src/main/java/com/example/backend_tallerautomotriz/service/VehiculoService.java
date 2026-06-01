package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.VehiculoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.VehiculoResponseDTO;

import java.util.List;

public interface VehiculoService {
    VehiculoResponseDTO crear(VehiculoRequestDTO request);
    VehiculoResponseDTO obtenerPorPatente(String patente);
    List<VehiculoResponseDTO> listarPorCliente(Integer clienteId);
    List<VehiculoResponseDTO> listarTodos();
    VehiculoResponseDTO actualizar(String patente, VehiculoRequestDTO request);
    void eliminar(String patente);
}

