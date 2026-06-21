package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.RegistroHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistroHorasRepository extends JpaRepository<RegistroHoras, Integer> {

    List<RegistroHoras> findByOrdenId(Integer ordenId);

    List<RegistroHoras> findByMecanicoId(Integer mecanicoId);

    @Query("SELECT r.mecanico.id, r.mecanico.usuario.nombre, r.mecanico.usuario.apellido, " +
            "SUM(r.horasInvertidas) as totalHoras FROM RegistroHoras r " +
            "GROUP BY r.mecanico.id, r.mecanico.usuario.nombre, r.mecanico.usuario.apellido " +
            "ORDER BY totalHoras DESC")
    List<Object[]> findHorasTotalesPorMecanico();

    @Query("SELECT COALESCE(SUM(r.horasInvertidas), 0) FROM RegistroHoras r WHERE r.mecanico.id = :mecanicoId")
    java.math.BigDecimal sumHorasByMecanicoId(Integer mecanicoId);

    @Query("SELECT COALESCE(SUM(r.horasInvertidas), 0) FROM RegistroHoras r " +
            "WHERE r.mecanico.id = :mecanicoId AND r.fechaRegistro = :fecha")
    java.math.BigDecimal sumHorasByMecanicoIdAndFecha(
            @Param("mecanicoId") Integer mecanicoId,
            @Param("fecha") java.time.LocalDate fecha);
}