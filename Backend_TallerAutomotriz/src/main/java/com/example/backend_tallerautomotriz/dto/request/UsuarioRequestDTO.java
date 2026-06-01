package com.example.backend_tallerautomotriz.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UsuarioRequestDTO {
    @NotBlank @Email  private String email;
    @NotBlank         private String password;
    @NotBlank         private String nombre;
    @NotBlank         private String apellido;
    @NotBlank         private String rol;
}