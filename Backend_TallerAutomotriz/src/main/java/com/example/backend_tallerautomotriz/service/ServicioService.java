package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.ServicioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ServicioResponseDTO;

import java.util.List;

public interface ServicioService {
    ServicioResponseDTO crear(ServicioRequestDTO request);
    ServicioResponseDTO obtenerPorId(Integer id);
    List<ServicioResponseDTO> listarTodos();
    List<ServicioResponseDTO> listarActivos();
    ServicioResponseDTO actualizar(Integer id, ServicioRequestDTO request);
    void desactivar(Integer id);
    void reactivar(Integer id);
}

