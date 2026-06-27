package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.OrdenRepuesto;
import com.example.backend_tallerautomotriz.entity.OrdenRepuestoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenRepuestoRepository extends JpaRepository<OrdenRepuesto, OrdenRepuestoId> {
    boolean existsByRepuestoId(Integer repuestoId);
}

