package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.UsuarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.UsuarioUpdatePasswordDTO;
import com.example.backend_tallerautomotriz.dto.response.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {
    //Admin
    List<UsuarioResponseDTO> listarTodos();
    UsuarioResponseDTO obtenerPorId(Integer id);
    UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO request);
    void eliminar(Integer id, String emailSolicitante);
    void desbloquear(Integer id);

    //Otro rol
    UsuarioResponseDTO obtenerPerfil(String email);
    UsuarioResponseDTO actualizarPerfil(String email, UsuarioRequestDTO request);
    void cambiarPassword(String email, UsuarioUpdatePasswordDTO request);
}
