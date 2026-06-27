package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuarioId(Integer usuarioId);
    boolean existsByIdAndUsuarioEmailIgnoreCase(Integer id, String email);
}

