package com.example.backend_tallerautomotriz.repository;


import com.example.backend_tallerautomotriz.entity.OrdenServicio;
import com.example.backend_tallerautomotriz.entity.OrdenServicioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenServicioRepository extends JpaRepository<OrdenServicio, OrdenServicioId> {
}
