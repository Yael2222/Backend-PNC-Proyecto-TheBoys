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
}
