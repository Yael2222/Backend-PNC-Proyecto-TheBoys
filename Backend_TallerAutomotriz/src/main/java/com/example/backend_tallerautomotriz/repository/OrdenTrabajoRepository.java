package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.OrdenTrabajo;
import com.example.backend_tallerautomotriz.enums.EstadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Integer> {

    List<OrdenTrabajo> findByClienteId(Integer clienteId);

    List<OrdenTrabajo> findByMecanicoId(Integer mecanicoId);

    List<OrdenTrabajo> findByEstado(EstadoOrden estado);

    List<OrdenTrabajo> findByVehiculoPatente(String patente);

    List<OrdenTrabajo> findBySucursalId(Integer sucursalId);

    List<OrdenTrabajo> findByClienteIdAndEstado(Integer clienteId, EstadoOrden estado);

    List<OrdenTrabajo> findByMecanicoIdAndEstado(Integer mecanicoId, EstadoOrden estado);

    @Query("SELECT o FROM OrdenTrabajo o WHERE o.mecanico.id = :mecanicoId " +
            "AND o.estado NOT IN (com.example.backend_tallerautomotriz.enums.EstadoOrden.COMPLETADA, " +
            "com.example.backend_tallerautomotriz.enums.EstadoOrden.CANCELADA)")
    List<OrdenTrabajo> findActivasByMecanicoId(Integer mecanicoId);

    @Query("SELECT o FROM OrdenTrabajo o WHERE o.fechaCreacion BETWEEN :desde AND :hasta")
    List<OrdenTrabajo> findByFechaCreacionBetween(LocalDate desde, LocalDate hasta);

    @Query("SELECT o FROM OrdenTrabajo o WHERE o.sucursal.id = :sucursalId " +
            "AND o.fechaCreacion BETWEEN :desde AND :hasta")
    List<OrdenTrabajo> findBySucursalIdAndFechaCreacionBetween(Integer sucursalId, LocalDate desde, LocalDate hasta);
}
