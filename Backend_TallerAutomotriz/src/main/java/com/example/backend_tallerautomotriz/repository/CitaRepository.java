package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Cita;
import com.example.backend_tallerautomotriz.enums.EstadoCita;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    List<Cita> findByClienteIdOrderByFechaDescHoraDesc(Integer clienteId);

    List<Cita> findBySucursalIdAndFechaOrderByHoraAsc(Integer sucursalId, LocalDate fecha);

    List<Cita> findByMecanicoIdOrderByFechaDescHoraDesc(Integer mecanicoId);
    boolean existsBySucursalId(Integer sucursalId);

    List<Cita> findByMecanicoIsNullAndEstadoOrderByFechaAscHoraAsc(EstadoCita estado);

    List<Cita> findBySucursalIdAndMecanicoIsNullAndEstadoOrderByFechaAscHoraAsc(
            Integer sucursalId,
            EstadoCita estado);

    boolean existsByIdAndClienteUsuarioEmailIgnoreCase(Integer id, String email);

    boolean existsByIdAndMecanicoUsuarioEmailIgnoreCase(Integer id, String email);

    boolean existsByMecanicoId(Integer mecanicoId);

    boolean existsByMecanicoIdAndFechaAndHoraAndEstadoNot(
            Integer mecanicoId,
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado);

    boolean existsByMecanicoIdAndFechaAndHoraAndIdNotAndEstadoNot(
            Integer mecanicoId,
            LocalDate fecha,
            LocalTime hora,
            Integer id,
            EstadoCita estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cita c where c.id = :id")
    java.util.Optional<Cita> findByIdForUpdate(@Param("id") Integer id);

    boolean existsByMecanicoIdAndFechaAndHora(Integer mecanicoId, LocalDate fecha, LocalTime hora);
    boolean existsByMecanicoIdAndFechaAndHoraAndIdNot(Integer mecanicoId, LocalDate fecha, LocalTime hora, Integer id);
}
