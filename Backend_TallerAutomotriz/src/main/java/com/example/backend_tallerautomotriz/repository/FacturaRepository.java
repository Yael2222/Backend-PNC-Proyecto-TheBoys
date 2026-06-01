package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {
    Optional<Factura> findByOrdenId(Integer ordenId);
}
