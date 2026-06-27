package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Inventario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.backend_tallerautomotriz.enums.CategoriaRepuesto;
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

    @Query("SELECT i FROM Inventario i WHERE i.sucursal.id = :sucursalId AND i.repuesto.categoria = :categoria")
    List<Inventario> findBySucursalIdAndCategoria(Integer sucursalId, CategoriaRepuesto categoria);

    @Query("SELECT i FROM Inventario i WHERE i.sucursal.id = :sucursalId " +
            "AND LOWER(i.repuesto.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Inventario> findBySucursalIdAndNombre(Integer sucursalId, String nombre);

    @Query("SELECT i FROM Inventario i WHERE i.sucursal.id = :sucursalId " +
            "AND i.repuesto.categoria = :categoria " +
            "AND LOWER(i.repuesto.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Inventario> findBySucursalIdAndCategoriaAndNombre(Integer sucursalId, CategoriaRepuesto categoria, String nombre);

}
