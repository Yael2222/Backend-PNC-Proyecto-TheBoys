package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.MecanicoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.MecanicoResponseDTO;

import java.util.List;

public interface MecanicoService {
    MecanicoResponseDTO crear(MecanicoRequestDTO request);
    MecanicoResponseDTO obtenerPorId(Integer id);
    MecanicoResponseDTO obtenerPorUsuarioId(Integer usuarioId);
    List<MecanicoResponseDTO> listarTodos();
    List<MecanicoResponseDTO> listarPorSucursal(Integer sucursalId);
    MecanicoResponseDTO actualizar(Integer id, MecanicoRequestDTO request);
    void eliminar(Integer id);
}