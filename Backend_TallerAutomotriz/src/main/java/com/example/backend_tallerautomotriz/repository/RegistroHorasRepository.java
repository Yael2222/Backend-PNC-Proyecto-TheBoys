package com.example.backend_tallerautomotriz.repository;

import com.example.backend_tallerautomotriz.entity.RegistroHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistroHorasRepository extends JpaRepository<RegistroHoras, Integer> {
    List<RegistroHoras> findByOrdenId(Integer ordenId);
    List<RegistroHoras> findByMecanicoId(Integer mecanicoId);
}