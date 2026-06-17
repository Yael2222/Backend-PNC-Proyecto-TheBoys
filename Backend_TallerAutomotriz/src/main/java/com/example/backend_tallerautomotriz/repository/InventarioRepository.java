package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Inventario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
    Optional<Inventario> findBySucursalIdAndRepuestoId(Integer sucursalId, Integer repuestoId);
    List<Inventario> findBySucursalId(Integer sucursalId);
    boolean existsBySucursalId(Integer sucursalId);
    boolean existsByRepuestoId(Integer repuestoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventario i where i.id = :id")
    Optional<Inventario> findByIdForUpdate(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventario i where i.sucursal.id = :sucursalId and i.repuesto.id = :repuestoId")
    Optional<Inventario> findBySucursalIdAndRepuestoIdForUpdate(
            @Param("sucursalId") Integer sucursalId,
            @Param("repuestoId") Integer repuestoId);
}