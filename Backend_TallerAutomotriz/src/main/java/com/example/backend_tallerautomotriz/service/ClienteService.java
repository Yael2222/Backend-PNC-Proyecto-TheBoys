package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.ClienteRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.ClienteResponseDTO;

import java.util.List;

public interface ClienteService {
    ClienteResponseDTO crear(ClienteRequestDTO request);
    ClienteResponseDTO obtenerPorId(Integer id);
    ClienteResponseDTO obtenerPorUsuarioId(Integer usuarioId);
    List<ClienteResponseDTO> listarTodos();
    ClienteResponseDTO actualizar(Integer id, ClienteRequestDTO request);
    void eliminar(Integer id);
}