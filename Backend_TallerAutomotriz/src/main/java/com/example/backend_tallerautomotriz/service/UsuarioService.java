package com.example.backend_tallerautomotriz.service;

import com.example.backend_tallerautomotriz.dto.request.CambiarRolRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.UsuarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.UsuarioResponseDTO;
import java.util.List;

public interface UsuarioService {

    List<UsuarioResponseDTO> listarTodos();

    UsuarioResponseDTO obtenerPorId(Integer id);

    UsuarioResponseDTO buscarPorEmail(String email);

    UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO req);

    void eliminar(Integer id);

    void desbloquear(Integer id);

    UsuarioResponseDTO cambiarRol(Integer usuarioId, CambiarRolRequestDTO req);
}
