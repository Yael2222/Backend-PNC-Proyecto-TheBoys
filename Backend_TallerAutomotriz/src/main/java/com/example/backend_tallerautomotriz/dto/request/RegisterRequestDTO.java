package com.example.backend_tallerautomotriz.dto.request;

import com.example.backend_tallerautomotriz.enums.NombreRol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class RegisterRequestDTO {
    @NotBlank
    @Email
    @Size(max = 254)
    private String email;

    @NotBlank
    @Size(min = 6, max = 72)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    private String apellido;

    @NotNull
    private NombreRol rol; // ADMIN | MECANICO | CLIENTE

    @Size(max = 20)
    private String telefono;
}