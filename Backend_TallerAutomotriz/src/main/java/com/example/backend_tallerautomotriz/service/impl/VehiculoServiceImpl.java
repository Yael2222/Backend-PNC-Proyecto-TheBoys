package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.VehiculoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.VehiculoResponseDTO;
import com.example.backend_tallerautomotriz.entity.Cliente;
import com.example.backend_tallerautomotriz.entity.Vehiculo;
import com.example.backend_tallerautomotriz.enums.EstadoOrden;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.ClienteRepository;
import com.example.backend_tallerautomotriz.repository.OrdenTrabajoRepository;
import com.example.backend_tallerautomotriz.repository.VehiculoRepository;
import com.example.backend_tallerautomotriz.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository repo;
    private final ClienteRepository clienteRepo;
    private final OrdenTrabajoRepository ordenRepo;


    @Override
    @Transactional
    public VehiculoResponseDTO crear(VehiculoRequestDTO req) {
        String patente = req.getPatente().toUpperCase().trim();

        if (repo.existsById(patente)) {
            throw new DuplicateResourceException("Patente ya registrada: " + patente);
        }

        Cliente c = clienteRepo.findById(req.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + req.getClienteId()));

        Vehiculo v = new Vehiculo(patente, req.getMarca(), req.getModelo(), c);
        return toDTO(repo.save(v));
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponseDTO obtenerPorPatente(String patente) {
        return toDTO(buscar(patente.toUpperCase().trim()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarPorCliente(Integer clienteId) {
        if (!clienteRepo.existsById(clienteId)) {
            throw new EntityNotFoundException("Cliente no encontrado: " + clienteId);
        }
        return repo.findByClienteId(clienteId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarTodos() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehiculoResponseDTO actualizar(String patente, VehiculoRequestDTO req, String emailSolicitante) {
        Vehiculo v = buscar(patente.toUpperCase().trim());
        v.setMarca(req.getMarca());
        v.setModelo(req.getModelo());
        return toDTO(repo.save(v));
    }

    @Override
    @Transactional
    public void eliminar(String patente) {
        String patenteNorm = patente.toUpperCase().trim();
        buscar(patenteNorm); // valida que exista, lanza 404 si no

        boolean tieneOrdenesActivas = ordenRepo.existsByVehiculoPatenteIgnoreCaseAndEstadoNotIn(
                patenteNorm, List.of(EstadoOrden.COMPLETADA, EstadoOrden.CANCELADA));

        if (tieneOrdenesActivas) {
            throw new BusinessRuleException(
                    "No se puede eliminar el vehículo porque tiene órdenes de trabajo activas");
        }

        repo.deleteById(patenteNorm);
    }

    //Helpers
    private Vehiculo buscar(String patente) {
        return repo.findById(patente)
                .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado: " + patente));
    }
    private VehiculoResponseDTO toDTO(Vehiculo v) {
        return new VehiculoResponseDTO(
                v.getPatente(),
                v.getMarca(),
                v.getModelo(),
                v.getCliente().getUsuario().getNombre() + " " + v.getCliente().getUsuario().getApellido());
    }
}

