package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Mecanico;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MecanicoRepository extends JpaRepository<Mecanico, Integer> {
    Optional<Mecanico> findByUsuarioId(Integer usuarioId);

    List<Mecanico> findBySucursalId(Integer sucursalId);

    boolean existsByIdAndUsuarioEmailIgnoreCase(Integer id, String email);

    boolean existsBySucursalIdAndUsuarioEmailIgnoreCase(Integer sucursalId, String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mecanico m where m.id = :id")
    Optional<Mecanico> findByIdForUpdate(@Param("id") Integer id);
}
