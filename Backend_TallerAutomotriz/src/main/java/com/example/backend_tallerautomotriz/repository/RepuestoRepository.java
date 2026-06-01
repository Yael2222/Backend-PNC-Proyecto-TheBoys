package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Repuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepuestoRepository extends JpaRepository<Repuesto, Integer> {
}

