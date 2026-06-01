package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, String> {
    List<Vehiculo> findByClienteId(Integer clienteId);
}
