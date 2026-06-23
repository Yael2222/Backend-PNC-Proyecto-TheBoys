package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.CitaRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.ReprogramarCitaRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.CitaResponseDTO;
import com.example.backend_tallerautomotriz.entity.*;
import com.example.backend_tallerautomotriz.enums.EstadoCita;
import com.example.backend_tallerautomotriz.enums.EstadoServicio;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.*;
import com.example.backend_tallerautomotriz.service.CitaService;
import com.example.backend_tallerautomotriz.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepo;
    private final ClienteRepository clienteRepo;
    private final SucursalRepository sucursalRepo;
    private final MecanicoRepository mecanicoRepo;
    private final ServicioRepository servicioRepo;
    private final NotificacionService notificacionService;

    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN    = LocalTime.of(19, 0);

    @Override
    @Transactional
    public CitaResponseDTO crear(CitaRequestDTO request) {
        validarTurno(request.getFecha(), request.getHora());
        Cliente cliente = clienteRepo.findById(request.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        Sucursal sucursal = sucursalRepo.findById(request.getSucursalId())
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
        Mecanico mecanico = null;

        if (request.getMecanicoId() != null) {
            mecanico = mecanicoRepo.findByIdForUpdate(request.getMecanicoId())
                    .orElseThrow(() -> new EntityNotFoundException("Mecanico no encontrado"));
            validarMecanicoEnSucursal(mecanico, sucursal);
            validarDisponibilidad(mecanico.getId(), request.getFecha(), request.getHora(), null);
        }

        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setSucursal(sucursal);
        cita.setMecanico(mecanico);
        cita.setFecha(request.getFecha());
        cita.setHora(request.getHora());
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setServicios(buscarServicios(request.getServicioIds()));
        return toDTO(citaRepo.save(cita));
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarPorCliente(Integer clienteId) {
        validarClienteExiste(clienteId);
        return citaRepo.findByClienteIdOrderByFechaDescHoraDesc(clienteId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarPorMecanico(Integer mecanicoId) {
        validarMecanicoExiste(mecanicoId);
        return citaRepo.findByMecanicoIdOrderByFechaDescHoraDesc(mecanicoId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarPorSucursalYFecha(Integer sucursalId, LocalDate fecha) {
        if (!sucursalRepo.existsById(sucursalId)) {
            throw new EntityNotFoundException("Sucursal no encontrada: " + sucursalId);
        }
        return citaRepo.findBySucursalIdAndFechaOrderByHoraAsc(sucursalId, fecha).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarPendientes(Integer sucursalId) {
        if (sucursalId != null) {
            if (!sucursalRepo.existsById(sucursalId)) {
                throw new EntityNotFoundException("Sucursal no encontrada: " + sucursalId);
            }
            return citaRepo.findBySucursalIdAndMecanicoIsNullAndEstadoOrderByFechaAscHoraAsc(
                            sucursalId,
                            EstadoCita.PROGRAMADA)
                    .stream()
                    .map(this::toDTO)
                    .toList();
        }
        return citaRepo.findByMecanicoIsNullAndEstadoOrderByFechaAscHoraAsc(EstadoCita.PROGRAMADA)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public CitaResponseDTO aceptar(Integer citaId, Integer mecanicoId) {
        Cita cita = buscarParaActualizar(citaId);
        if (cita.getEstado() != EstadoCita.PROGRAMADA || cita.getMecanico() != null) {
            throw new BusinessRuleException("Solo se pueden aceptar citas programadas sin mecanico asignado");
        }

        Mecanico mecanico = mecanicoRepo.findByIdForUpdate(mecanicoId)
                .orElseThrow(() -> new EntityNotFoundException("Mecanico no encontrado: " + mecanicoId));
        validarMecanicoEnSucursal(mecanico, cita.getSucursal());
        validarTurno(cita.getFecha(), cita.getHora());
        validarDisponibilidad(mecanicoId, cita.getFecha(), cita.getHora(), citaId);

        cita.setMecanico(mecanico);
        cita.setEstado(EstadoCita.CONFIRMADA);
        Cita guardada = citaRepo.save(cita);
        notificar(
                cita.getCliente().getUsuario().getId(),
                "Tu cita #" + citaId + " fue aceptada por " + nombreMecanico(mecanico),
                "CITA_CONFIRMADA",
                citaId);
        return toDTO(guardada);
    }

    @Override
    @Transactional
    public CitaResponseDTO reprogramar(Integer citaId, ReprogramarCitaRequestDTO request) {
        validarTurno(request.getNuevaFecha(), request.getNuevaHora());
        Cita cita = buscarParaActualizar(citaId);
        if (cita.getMecanico() == null) {
            throw new BusinessRuleException("La cita no tiene mecanico asignado");
        }
        if (
                cita.getEstado() != EstadoCita.CONFIRMADA &&
                        cita.getEstado() != EstadoCita.PROGRAMADA &&
                        cita.getEstado() != EstadoCita.REPROGRAMADA
        ) {
            throw new BusinessRuleException("Solo se pueden proponer nuevas fechas para citas programadas, confirmadas o reprogramadas.");
        }

        mecanicoRepo.findByIdForUpdate(cita.getMecanico().getId())
                .orElseThrow(() -> new EntityNotFoundException("Mecanico no encontrado"));
        validarDisponibilidad(cita.getMecanico().getId(), request.getNuevaFecha(), request.getNuevaHora(), citaId);
        cita.setNuevaFechaPropuesta(request.getNuevaFecha());
        cita.setNuevaHoraPropuesta(request.getNuevaHora());
        cita.setEstado(EstadoCita.REPROGRAMADA);
        Cita guardada = citaRepo.save(cita);
        notificar(
                cita.getCliente().getUsuario().getId(),
                "Se propuso reprogramar tu cita #" + citaId + " para " + request.getNuevaFecha()
                        + " a las " + request.getNuevaHora(),
                "CITA_REPROGRAMADA",
                citaId);
        return toDTO(guardada);
    }

    @Override
    @Transactional
    public CitaResponseDTO aceptarReprogramacion(Integer citaId) {
        Cita cita = buscarParaActualizar(citaId);
        if (cita.getEstado() != EstadoCita.REPROGRAMADA
                || cita.getNuevaFechaPropuesta() == null
                || cita.getNuevaHoraPropuesta() == null) {
            throw new BusinessRuleException("La cita no tiene una reprogramacion pendiente");
        }

        validarTurno(cita.getNuevaFechaPropuesta(), cita.getNuevaHoraPropuesta());
        if (cita.getMecanico() != null) {
            mecanicoRepo.findByIdForUpdate(cita.getMecanico().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Mecanico no encontrado"));
            validarDisponibilidad(
                    cita.getMecanico().getId(),
                    cita.getNuevaFechaPropuesta(),
                    cita.getNuevaHoraPropuesta(),
                    citaId);
        }

        cita.setFecha(cita.getNuevaFechaPropuesta());
        cita.setHora(cita.getNuevaHoraPropuesta());
        cita.setNuevaFechaPropuesta(null);
        cita.setNuevaHoraPropuesta(null);
        cita.setEstado(EstadoCita.CONFIRMADA);
        Cita guardada = citaRepo.save(cita);
        if (cita.getMecanico() != null) {
            notificar(
                    cita.getMecanico().getUsuario().getId(),
                    "El cliente acepto la reprogramacion de la cita #" + citaId,
                    "REPROGRAMACION_ACEPTADA",
                    citaId);
        }
        return toDTO(guardada);
    }

    @Override
    @Transactional
    public CitaResponseDTO confirmar(Integer id) {
        Cita cita = buscarParaActualizar(id);
        if (cita.getEstado() == EstadoCita.CONFIRMADA) {
            return toDTO(cita);
        }
        if (cita.getEstado() != EstadoCita.PROGRAMADA || cita.getMecanico() == null) {
            throw new BusinessRuleException("Solo se pueden confirmar citas programadas con mecanico asignado");
        }
        validarTurno(cita.getFecha(), cita.getHora());
        cita.setEstado(EstadoCita.CONFIRMADA);
        return toDTO(citaRepo.save(cita));
    }

    @Override
    @Transactional
    public void cancelar(Integer id) {
        Cita cita = buscarParaActualizar(id);
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            return;
        }
        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new BusinessRuleException("No se puede cancelar una cita completada");
        }
        cita.setEstado(EstadoCita.CANCELADA);
        cita.setNuevaFechaPropuesta(null);
        cita.setNuevaHoraPropuesta(null);
        citaRepo.save(cita);
    }

    private List<Servicio> buscarServicios(List<Integer> servicioIds) {
        if (servicioIds == null || servicioIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        Set<Integer> unicos = new HashSet<>(servicioIds);
        if (unicos.size() != servicioIds.size()) {
            throw new BusinessRuleException("No se permiten servicios duplicados en una cita");
        }
        return new java.util.ArrayList<>(unicos.stream().map(id -> {
            Servicio servicio = servicioRepo.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado: " + id));
            if (servicio.getEstado() != EstadoServicio.ACTIVO) {
                throw new BusinessRuleException("El servicio no esta activo: " + id);
            }
            return servicio;
        }).toList());
    }

    private void validarTurno(LocalDate fecha, LocalTime hora) {
        if (hora.getMinute() != 0 || hora.getSecond() != 0 || hora.getNano() != 0) {
            throw new BusinessRuleException("Las citas deben iniciar en una hora exacta");
        }
        if (hora.isBefore(HORA_INICIO) || !hora.isBefore(HORA_FIN)) {
            throw new BusinessRuleException("La hora debe estar entre las 09:00 y las 19:00");
        }
        if (!LocalDateTime.of(fecha, hora).isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("La cita debe programarse en una fecha y hora futura");
        }
    }

    private void validarMecanicoEnSucursal(Mecanico mecanico, Sucursal sucursal) {
        if (!mecanico.getSucursal().getId().equals(sucursal.getId())) {
            throw new BusinessRuleException("El mecanico no pertenece a la sucursal seleccionada");
        }
    }

    private void validarDisponibilidad(Integer mecanicoId, LocalDate fecha, LocalTime hora, Integer citaIdExcluir) {
        boolean ocupado = citaIdExcluir == null
                ? citaRepo.existsByMecanicoIdAndFechaAndHoraAndEstadoNot(mecanicoId, fecha, hora, EstadoCita.CANCELADA)
                : citaRepo.existsByMecanicoIdAndFechaAndHoraAndIdNotAndEstadoNot(
                mecanicoId, fecha, hora, citaIdExcluir, EstadoCita.CANCELADA);
        if (ocupado) {
            throw new BusinessRuleException("El mecanico ya tiene una cita a esa hora");
        }
    }

    private void validarClienteExiste(Integer clienteId) {
        if (!clienteRepo.existsById(clienteId)) {
            throw new EntityNotFoundException("Cliente no encontrado: " + clienteId);
        }
    }

    private void validarMecanicoExiste(Integer mecanicoId) {
        if (!mecanicoRepo.existsById(mecanicoId)) {
            throw new EntityNotFoundException("Mecanico no encontrado: " + mecanicoId);
        }
    }

    private Cita buscar(Integer id) {
        return citaRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Cita no encontrada: " + id));
    }

    private Cita buscarParaActualizar(Integer id) {
        return citaRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Cita no encontrada: " + id));
    }

    private String nombreMecanico(Mecanico mecanico) {
        return mecanico.getUsuario().getNombre() + " " + mecanico.getUsuario().getApellido();
    }

    private void notificar(Integer usuarioId, String mensaje, String tipo, Integer referenciaId) {
        notificacionService.crear(usuarioId, mensaje, tipo, referenciaId);
    }

    private CitaResponseDTO toDTO(Cita cita) {
        return new CitaResponseDTO(
                cita.getId(),
                cita.getCliente().getUsuario().getNombre() + " " + cita.getCliente().getUsuario().getApellido(),
                cita.getCliente().getId(),
                cita.getSucursal().getNombre(),
                cita.getSucursal().getId(),
                cita.getMecanico() == null ? null : nombreMecanico(cita.getMecanico()),
                cita.getMecanico() == null ? null : cita.getMecanico().getId(),
                cita.getFecha(),
                cita.getHora(),
                cita.getEstado().name(),
                cita.getServicios().stream().map(Servicio::getNombre).toList(),
                cita.getNuevaFechaPropuesta(),
                cita.getNuevaHoraPropuesta());
    }

    private void validarHorarioLaboral(LocalTime hora) {
        if (hora.isBefore(HORA_INICIO) || hora.isAfter(HORA_FIN.minusMinutes(1)))
            throw new BusinessRuleException("La hora debe estar entre las 09:00 y las 19:00");
        if (hora.getMinute() != 0)
            throw new BusinessRuleException("Las citas deben agendarse en horas en punto (ej: 09:00, 10:00)");
    }

    private void validarDisponibilidadMecanico(Integer mecanicoId, LocalDate fecha, LocalTime hora, Integer citaIdExcluir) {
        boolean ocupado = citaIdExcluir != null
                ? citaRepo.existsByMecanicoIdAndFechaAndHoraAndIdNot(mecanicoId, fecha, hora, citaIdExcluir)
                : citaRepo.existsByMecanicoIdAndFechaAndHora(mecanicoId, fecha, hora);
        if (ocupado)
            throw new BusinessRuleException("El mecánico ya tiene una cita a esa hora y fecha");
    }
}