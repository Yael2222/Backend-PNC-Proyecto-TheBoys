package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.OrdenTrabajo;
import com.example.backend_tallerautomotriz.enums.EstadoOrden;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Integer> {

    List<OrdenTrabajo> findByClienteIdOrderByFechaCreacionDesc(Integer clienteId);

    List<OrdenTrabajo> findByMecanicoIdOrderByFechaCreacionDesc(Integer mecanicoId);

    List<OrdenTrabajo> findByVehiculoPatenteOrderByFechaCreacionDesc(String patente);

    List<OrdenTrabajo> findByEstado(EstadoOrden estado);

    boolean existsByMecanicoId(Integer mecanicoId);

    boolean existsByIdAndClienteUsuarioEmailIgnoreCase(Integer id, String email);

    boolean existsByIdAndMecanicoUsuarioEmailIgnoreCase(Integer id, String email);

    boolean existsByVehiculoPatenteIgnoreCaseAndMecanicoUsuarioEmailIgnoreCase(String patente, String email);

    boolean existsByVehiculoPatenteIgnoreCaseAndEstadoNotIn(String patente, List<EstadoOrden> estados);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrdenTrabajo o where o.id = :id")
    Optional<OrdenTrabajo> findByIdForUpdate(@Param("id") Integer id);

    List<OrdenTrabajo> findByClienteId(Integer clienteId);

    List<OrdenTrabajo> findByMecanicoId(Integer mecanicoId);
    
    List<OrdenTrabajo> findByVehiculoPatente(String patente);

    List<OrdenTrabajo> findBySucursalId(Integer sucursalId);

    List<OrdenTrabajo> findByClienteIdAndEstado(Integer clienteId, EstadoOrden estado);

    List<OrdenTrabajo> findByMecanicoIdAndEstado(Integer mecanicoId, EstadoOrden estado);

    @Query("SELECT o FROM OrdenTrabajo o WHERE o.mecanico.id = :mecanicoId " +
            "AND o.estado NOT IN (com.example.backend_tallerautomotriz.enums.EstadoOrden.COMPLETADA, " +
            "com.example.backend_tallerautomotriz.enums.EstadoOrden.CANCELADA)")
    List<OrdenTrabajo> findActivasByMecanicoId(Integer mecanicoId);

    // Reportes por rango de fechas
    @Query("SELECT o FROM OrdenTrabajo o WHERE o.fechaCreacion BETWEEN :desde AND :hasta")
    List<OrdenTrabajo> findByFechaCreacionBetween(LocalDate desde, LocalDate hasta);

    @Query("SELECT o FROM OrdenTrabajo o WHERE o.sucursal.id = :sucursalId " +
            "AND o.fechaCreacion BETWEEN :desde AND :hasta")
    List<OrdenTrabajo> findBySucursalIdAndFechaCreacionBetween(Integer sucursalId, LocalDate desde, LocalDate hasta);

    // Reporte: cantidad de órdenes por sucursal
    @Query("SELECT o.sucursal.id, o.sucursal.nombre, COUNT(o) FROM OrdenTrabajo o " +
            "WHERE o.sucursal IS NOT NULL GROUP BY o.sucursal.id, o.sucursal.nombre ORDER BY COUNT(o) DESC")
    List<Object[]> countOrdenesPorSucursal();

    // Reporte: cantidad de órdenes por estado
    @Query("SELECT o.estado, COUNT(o) FROM OrdenTrabajo o GROUP BY o.estado")
    List<Object[]> countByEstado();

}
