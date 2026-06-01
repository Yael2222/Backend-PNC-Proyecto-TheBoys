package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.UsuarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {
    List<UsuarioResponseDTO> listarTodos();
    UsuarioResponseDTO obtenerPorId(Integer id);
    UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO request);
    void eliminar(Integer id);
    void desbloquear(Integer id);
}
