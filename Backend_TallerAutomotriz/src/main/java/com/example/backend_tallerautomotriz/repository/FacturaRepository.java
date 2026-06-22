package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Factura;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {
    Optional<Factura> findByOrdenId(Integer ordenId);

    java.util.List<Factura> findByOrdenClienteIdOrderByIdDesc(Integer clienteId);

    boolean existsByIdAndOrdenClienteUsuarioEmailIgnoreCase(Integer id, String email);

    boolean existsByOrdenIdAndOrdenClienteUsuarioEmailIgnoreCase(Integer ordenId, String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Factura f where f.orden.id = :ordenId")
    Optional<Factura> findByOrdenIdForUpdate(@Param("ordenId") Integer ordenId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Factura f where f.id = :id")
    Optional<Factura> findByIdForUpdate(@Param("id") Integer id);
}