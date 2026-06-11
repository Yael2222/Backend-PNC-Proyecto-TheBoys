package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);

    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(Integer usuarioId);

    long countByUsuarioIdAndLeidaFalse(Integer usuarioId);

    boolean existsByIdAndUsuarioEmailIgnoreCase(Integer id, String email);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario.id = :usuarioId")
    void marcarTodasComoLeidas(Integer usuarioId);
}
