package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
    Optional<Inventario> findBySucursalIdAndRepuestoId(Integer sucursalId, Integer repuestoId);
    List<Inventario> findBySucursalId(Integer sucursalId);
}