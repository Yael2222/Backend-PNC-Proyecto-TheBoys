package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.CitaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.CitaResponseDTO;
import com.example.backend_tallerautomotriz.entity.Cita;
import com.example.backend_tallerautomotriz.entity.Cliente;
import com.example.backend_tallerautomotriz.entity.Mecanico;
import com.example.backend_tallerautomotriz.entity.Sucursal;
import com.example.backend_tallerautomotriz.enums.EstadoCita;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.CitaRepository;
import com.example.backend_tallerautomotriz.repository.ClienteRepository;
import com.example.backend_tallerautomotriz.repository.MecanicoRepository;
import com.example.backend_tallerautomotriz.repository.SucursalRepository;
import com.example.backend_tallerautomotriz.service.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {
    private final CitaRepository repo;
    private final ClienteRepository clienteRepo;
    private final SucursalRepository sucursalRepo;
    private final MecanicoRepository mecanicoRepo;

    @Override public CitaResponseDTO crear(CitaRequestDTO req) {
        Cliente c = clienteRepo.findById(req.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        Sucursal s = sucursalRepo.findById(req.getSucursalId())
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
        Mecanico m = null;
        if (req.getMecanicoId() != null) {
            m = mecanicoRepo.findById(req.getMecanicoId())
                    .orElseThrow(() -> new EntityNotFoundException("Mecánico no encontrado"));
            if (repo.existsByMecanicoIdAndFechaAndHora(m.getId(), req.getFecha(), req.getHora()))
                throw new BusinessRuleException("El mecánico ya tiene una cita a esa hora");
        }
        Cita cita = new Cita(null, c, s, m, req.getFecha(), req.getHora(), EstadoCita.PROGRAMADA);
        return toDTO(repo.save(cita));
    }
    @Override public CitaResponseDTO obtenerPorId(Integer id) { return toDTO(buscar(id)); }
    @Override public List<CitaResponseDTO> listarPorCliente(Integer clienteId) {
        return repo.findByClienteId(clienteId).stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public List<CitaResponseDTO> listarPorSucursalYFecha(Integer sucursalId, String fecha) {
        return repo.findBySucursalIdAndFecha(sucursalId, LocalDate.parse(fecha))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override public CitaResponseDTO confirmar(Integer id) {
        Cita c = buscar(id); c.setEstado(EstadoCita.CONFIRMADA); return toDTO(repo.save(c));
    }
    @Override public void cancelar(Integer id) {
        Cita c = buscar(id); c.setEstado(EstadoCita.CANCELADA); repo.save(c);
    }
    private Cita buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Cita no encontrada: " + id));
    }
    private CitaResponseDTO toDTO(Cita c) {
        String mecanico = c.getMecanico() != null ?
                c.getMecanico().getUsuario().getNombre() + " " + c.getMecanico().getUsuario().getApellido() : null;
        return new CitaResponseDTO(c.getId(),
                c.getCliente().getUsuario().getNombre() + " " + c.getCliente().getUsuario().getApellido(),
                c.getSucursal().getNombre(), mecanico, c.getFecha(), c.getHora(), c.getEstado().name());
    }
}

