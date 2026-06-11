package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.CitaRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.ReprogramarCitaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.CitaResponseDTO;
import com.example.backend_tallerautomotriz.entity.*;
import com.example.backend_tallerautomotriz.enums.EstadoCita;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.*;
import com.example.backend_tallerautomotriz.service.CitaService;
import com.example.backend_tallerautomotriz.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository repo;
    private final ClienteRepository clienteRepo;
    private final SucursalRepository sucursalRepo;
    private final MecanicoRepository mecanicoRepo;
    private final ServicioRepository servicioRepo;
    private final NotificacionService notificacionService;

    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN    = LocalTime.of(19, 0);

    @Override
    @Transactional
    public CitaResponseDTO crear(CitaRequestDTO req) {
        validarHorarioLaboral(req.getHora());

        Cliente c = clienteRepo.findById(req.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        Sucursal s = sucursalRepo.findById(req.getSucursalId())
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));

        Mecanico m = null;
        if (req.getMecanicoId() != null) {
            m = mecanicoRepo.findById(req.getMecanicoId())
                    .orElseThrow(() -> new EntityNotFoundException("Mecánico no encontrado"));
            validarDisponibilidadMecanico(m.getId(), req.getFecha(), req.getHora(), null);
        }

        List<Servicio> servicios = new ArrayList<>();
        if (req.getServicioIds() != null) {
            for (Integer sid : req.getServicioIds()) {
                servicios.add(servicioRepo.findById(sid)
                        .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado: " + sid)));
            }
        }

        Cita cita = new Cita(null, c, s, m, req.getFecha(), req.getHora(),
                EstadoCita.PROGRAMADA, servicios, null, null);
        return toDTO(repo.save(cita));
    }

    @Override
    public CitaResponseDTO obtenerPorId(Integer id) { return toDTO(buscar(id)); }

    @Override
    public List<CitaResponseDTO> listarPorCliente(Integer clienteId) {
        return repo.findByClienteId(clienteId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CitaResponseDTO> listarPorMecanico(Integer mecanicoId) {
        return repo.findByMecanicoId(mecanicoId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CitaResponseDTO> listarPorSucursalYFecha(Integer sucursalId, String fecha) {
        return repo.findBySucursalIdAndFecha(sucursalId, LocalDate.parse(fecha))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CitaResponseDTO> listarPendientes() {
        return repo.findByMecanicoIsNullAndEstado(EstadoCita.PROGRAMADA)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CitaResponseDTO aceptar(Integer citaId, Integer mecanicoId) {
        Cita cita = buscar(citaId);
        if (cita.getEstado() != EstadoCita.PROGRAMADA)
            throw new BusinessRuleException("Solo se pueden aceptar citas en estado PROGRAMADA");

        Mecanico mecanico = mecanicoRepo.findById(mecanicoId)
                .orElseThrow(() -> new EntityNotFoundException("Mecánico no encontrado"));
        validarDisponibilidadMecanico(mecanicoId, cita.getFecha(), cita.getHora(), citaId);

        cita.setMecanico(mecanico);
        cita.setEstado(EstadoCita.CONFIRMADA);
        repo.save(cita);

        // Notificar al cliente
        notificacionService.crear(
                cita.getCliente().getUsuario().getId(),
                "Tu cita del " + cita.getFecha() + " a las " + cita.getHora() +
                        " fue aceptada por el mecánico " +
                        mecanico.getUsuario().getNombre() + " " + mecanico.getUsuario().getApellido() + ".",
                "CITA_CONFIRMADA",
                citaId
        );

        return toDTO(cita);
    }

    @Override
    @Transactional
    public CitaResponseDTO reprogramar(Integer citaId, ReprogramarCitaRequestDTO req) {
        validarHorarioLaboral(req.getNuevaHora());
        Cita cita = buscar(citaId);

        if (cita.getMecanico() == null)
            throw new BusinessRuleException("La cita no tiene mecánico asignado para reprogramar");
        if (cita.getEstado() == EstadoCita.CANCELADA || cita.getEstado() == EstadoCita.COMPLETADA)
            throw new BusinessRuleException("No se puede reprogramar una cita " + cita.getEstado());

        validarDisponibilidadMecanico(cita.getMecanico().getId(), req.getNuevaFecha(), req.getNuevaHora(), citaId);

        cita.setNuevaFechaPropuesta(req.getNuevaFecha());
        cita.setNuevaHoraPropuesta(req.getNuevaHora());
        cita.setEstado(EstadoCita.REPROGRAMADA);
        repo.save(cita);

        // Notificar al cliente
        notificacionService.crear(
                cita.getCliente().getUsuario().getId(),
                "Tu mecánico propuso reprogramar tu cita para el " + req.getNuevaFecha() +
                        " a las " + req.getNuevaHora() + ". Por favor, confirma o cancela.",
                "CITA_REPROGRAMADA",
                citaId
        );

        return toDTO(cita);
    }

    @Override
    @Transactional
    public CitaResponseDTO aceptarReprogramacion(Integer citaId) {
        Cita cita = buscar(citaId);
        if (cita.getEstado() != EstadoCita.REPROGRAMADA)
            throw new BusinessRuleException("La cita no está en estado REPROGRAMADA");

        cita.setFecha(cita.getNuevaFechaPropuesta());
        cita.setHora(cita.getNuevaHoraPropuesta());
        cita.setNuevaFechaPropuesta(null);
        cita.setNuevaHoraPropuesta(null);
        cita.setEstado(EstadoCita.CONFIRMADA);
        repo.save(cita);

        // Notificar al mecánico
        if (cita.getMecanico() != null) {
            notificacionService.crear(
                    cita.getMecanico().getUsuario().getId(),
                    "El cliente aceptó la reprogramación de la cita #" + citaId + " para el " + cita.getFecha() + ".",
                    "REPROGRAMACION_ACEPTADA",
                    citaId
            );
        }

        return toDTO(cita);
    }

    @Override
    @Transactional
    public CitaResponseDTO confirmar(Integer id) {
        Cita c = buscar(id);
        c.setEstado(EstadoCita.CONFIRMADA);
        return toDTO(repo.save(c));
    }

    @Override
    @Transactional
    public void cancelar(Integer id) {
        Cita c = buscar(id);
        if (c.getEstado() == EstadoCita.COMPLETADA)
            throw new BusinessRuleException("No se puede cancelar una cita ya completada");
        c.setEstado(EstadoCita.CANCELADA);
        repo.save(c);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void validarHorarioLaboral(LocalTime hora) {
        if (hora.isBefore(HORA_INICIO) || hora.isAfter(HORA_FIN.minusMinutes(1)))
            throw new BusinessRuleException("La hora debe estar entre las 09:00 y las 19:00");
    }

    private void validarDisponibilidadMecanico(Integer mecanicoId, LocalDate fecha, LocalTime hora, Integer citaIdExcluir) {
        boolean ocupado = citaIdExcluir != null
                ? repo.existsByMecanicoIdAndFechaAndHoraAndIdNot(mecanicoId, fecha, hora, citaIdExcluir)
                : repo.existsByMecanicoIdAndFechaAndHora(mecanicoId, fecha, hora);
        if (ocupado)
            throw new BusinessRuleException("El mecánico ya tiene una cita a esa hora y fecha");
    }

    private Cita buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Cita no encontrada: " + id));
    }

    private CitaResponseDTO toDTO(Cita c) {
        String mecNombre = c.getMecanico() != null
                ? c.getMecanico().getUsuario().getNombre() + " " + c.getMecanico().getUsuario().getApellido()
                : null;
        Integer mecId = c.getMecanico() != null ? c.getMecanico().getId() : null;

        List<String> nombresServicios = c.getServicios().stream()
                .map(Servicio::getNombre).collect(Collectors.toList());

        return new CitaResponseDTO(
                c.getId(),
                c.getCliente().getUsuario().getNombre() + " " + c.getCliente().getUsuario().getApellido(),
                c.getCliente().getId(),
                c.getSucursal().getNombre(),
                c.getSucursal().getId(),
                mecNombre, mecId,
                c.getFecha(), c.getHora(),
                c.getEstado().name(),
                nombresServicios,
                c.getNuevaFechaPropuesta(),
                c.getNuevaHoraPropuesta()
        );
    }
}
