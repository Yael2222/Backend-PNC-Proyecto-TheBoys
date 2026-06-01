package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.RepuestoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.RepuestoResponseDTO;

import java.util.List;

public interface RepuestoService {
    RepuestoResponseDTO crear(RepuestoRequestDTO request);
    RepuestoResponseDTO obtenerPorId(Integer id);
    List<RepuestoResponseDTO> listarTodos();
    RepuestoResponseDTO actualizar(Integer id, RepuestoRequestDTO request);
    void eliminar(Integer id);
}
