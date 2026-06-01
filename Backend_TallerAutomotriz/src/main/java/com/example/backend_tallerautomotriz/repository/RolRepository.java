package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Rol;
import com.example.backend_tallerautomotriz.enums.NombreRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(NombreRol nombre);
}
