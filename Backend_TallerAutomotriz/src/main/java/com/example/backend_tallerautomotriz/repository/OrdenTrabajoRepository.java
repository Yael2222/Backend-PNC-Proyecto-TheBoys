package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.OrdenTrabajo;
import com.example.backend_tallerautomotriz.enums.EstadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Integer> {
    List<OrdenTrabajo> findByClienteId(Integer clienteId);
    List<OrdenTrabajo> findByMecanicoId(Integer mecanicoId);
    List<OrdenTrabajo> findByEstado(EstadoOrden estado);
}
