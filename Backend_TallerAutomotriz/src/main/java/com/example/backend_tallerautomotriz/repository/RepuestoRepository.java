package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Repuesto;
import com.example.backend_tallerautomotriz.enums.CategoriaRepuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepuestoRepository extends JpaRepository<Repuesto, Integer> {

    List<Repuesto> findByCategoria(CategoriaRepuesto categoria);

    List<Repuesto> findByNombreContainingIgnoreCase(String nombre);

    List<Repuesto> findByCategoriaAndNombreContainingIgnoreCase(CategoriaRepuesto categoria, String nombre);

    // Repuestos más usados en órdenes (para reporte admin)
    @Query("SELECT r.repuesto.id, r.repuesto.nombre, SUM(r.cantidad) as total " +
            "FROM OrdenRepuesto r GROUP BY r.repuesto.id, r.repuesto.nombre ORDER BY total DESC")
    List<Object[]> findRepuestosMasUsados();
}
