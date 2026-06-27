package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);

    @Query("SELECT COUNT(m) > 0 FROM Mecanico m WHERE m.sucursal.id = :sucursalId")
    boolean tieneMecanicos(Integer sucursalId);
}

