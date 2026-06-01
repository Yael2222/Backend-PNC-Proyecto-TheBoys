package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {
    List<Cita> findByClienteId(Integer clienteId);
    List<Cita> findBySucursalIdAndFecha(Integer sucursalId, LocalDate fecha);
    boolean existsByMecanicoIdAndFechaAndHora(Integer mecanicoId, LocalDate fecha, LocalTime hora);
}
