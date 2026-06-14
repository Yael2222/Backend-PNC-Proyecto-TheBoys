package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Mecanico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MecanicoRepository extends JpaRepository<Mecanico, Integer> {
    Optional<Mecanico> findByUsuarioId(Integer usuarioId);
    List<Mecanico> findBySucursalId(Integer sucursalId);
}
