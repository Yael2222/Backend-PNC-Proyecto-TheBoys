package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.OrdenRepuestoRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.OrdenServicioRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.OrdenTrabajoRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.PresupuestoRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.OrdenRepuestoResponseDTO;
import com.example.backend_tallerautomotriz.dto.response.OrdenServicioResponseDTO;
import com.example.backend_tallerautomotriz.dto.response.OrdenTrabajoResponseDTO;
import com.example.backend_tallerautomotriz.entity.*;
import com.example.backend_tallerautomotriz.enums.EstadoOrden;
import com.example.backend_tallerautomotriz.enums.EstadoPago;
import com.example.backend_tallerautomotriz.enums.TipoOrden;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.exception.StockInsuficienteException;
import com.example.backend_tallerautomotriz.repository.*;
import com.example.backend_tallerautomotriz.service.NotificacionService;
import com.example.backend_tallerautomotriz.service.OrdenTrabajoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdenTrabajoServiceImpl implements OrdenTrabajoService {

    private final OrdenTrabajoRepository ordenRepo;
    private final ClienteRepository clienteRepo;
    private final MecanicoRepository mecanicoRepo;
    private final VehiculoRepository vehiculoRepo;
    private final ServicioRepository servicioRepo;
    private final RepuestoRepository repuestoRepo;
    private final InventarioRepository inventarioRepo;
    private final FacturaRepository facturaRepo;
    private final SucursalRepository sucursalRepo;
    private final NotificacionService notificacionService;

    private static final Map<EstadoOrden, List<EstadoOrden>> TRANSICIONES = Map.of(
            EstadoOrden.PENDIENTE,              List.of(EstadoOrden.PENDIENTE_APROBACION, EstadoOrden.EN_PROGRESO, EstadoOrden.CANCELADA),
            EstadoOrden.PENDIENTE_APROBACION,   List.of(EstadoOrden.EN_PROGRESO, EstadoOrden.PENDIENTE, EstadoOrden.CANCELADA),
            EstadoOrden.EN_PROGRESO,            List.of(EstadoOrden.COMPLETADA, EstadoOrden.CANCELADA),
            EstadoOrden.COMPLETADA,             List.of(),
            EstadoOrden.CANCELADA,              List.of()
    );

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO crear(OrdenTrabajoRequestDTO req) {
        Cliente cliente = clienteRepo.findById(req.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + req.getClienteId()));
        Vehiculo vehiculo = vehiculoRepo.findById(req.getPatente())
                .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado: " + req.getPatente()));

        Mecanico mecanico = null;
        if (req.getMecanicoId() != null)
            mecanico = mecanicoRepo.findById(req.getMecanicoId())
                    .orElseThrow(() -> new EntityNotFoundException("Mecánico no encontrado"));

        Sucursal sucursal = null;
        if (req.getSucursalId() != null)
            sucursal = sucursalRepo.findById(req.getSucursalId())
                    .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
        else if (mecanico != null)
            sucursal = mecanico.getSucursal();

        // Verificar stock antes de comprometer la orden
        final Sucursal sucursalFinal = sucursal;
        if (req.getRepuestos() != null && sucursalFinal != null) {
            for (OrdenRepuestoRequestDTO r : req.getRepuestos()) {
                Inventario inv = inventarioRepo
                        .findBySucursalIdAndRepuestoId(sucursalFinal.getId(), r.getRepuestoId())
                        .orElseThrow(() -> new EntityNotFoundException("Repuesto sin inventario en esta sucursal: " + r.getRepuestoId()));
                if (inv.getStockTotal() < r.getCantidad())
                    throw new StockInsuficienteException("Stock insuficiente para repuesto id: " + r.getRepuestoId());
            }
        }

        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setVehiculo(vehiculo);
        orden.setCliente(cliente);
        orden.setMecanico(mecanico);
        orden.setSucursal(sucursalFinal);
        orden.setTipoOrden(TipoOrden.valueOf(req.getTipoOrden().toUpperCase()));
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setFechaCreacion(LocalDate.now());
        orden.setComentarios(req.getComentarios());
        orden = ordenRepo.save(orden);

        if (req.getServicios() != null) {
            for (OrdenServicioRequestDTO s : req.getServicios()) {
                Servicio servicio = servicioRepo.findById(s.getServicioId())
                        .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado: " + s.getServicioId()));
                OrdenServicio os = new OrdenServicio(
                        new OrdenServicioId(orden.getId(), s.getServicioId()), orden, servicio, s.getPrecioAplicado());
                orden.getServicios().add(os);
            }
        }

        if (req.getRepuestos() != null) {
            for (OrdenRepuestoRequestDTO r : req.getRepuestos()) {
                Repuesto repuesto = repuestoRepo.findById(r.getRepuestoId())
                        .orElseThrow(() -> new EntityNotFoundException("Repuesto no encontrado: " + r.getRepuestoId()));
                OrdenRepuesto or2 = new OrdenRepuesto(
                        new OrdenRepuestoId(orden.getId(), r.getRepuestoId()),
                        orden, repuesto, r.getCantidad(), r.getPrecioAplicado());
                orden.getRepuestos().add(or2);
                if (sucursalFinal != null) {
                    inventarioRepo.findBySucursalIdAndRepuestoId(sucursalFinal.getId(), r.getRepuestoId())
                            .ifPresent(inv -> {
                                inv.setStockTotal(inv.getStockTotal() - r.getCantidad());
                                inventarioRepo.save(inv);
                            });
                }
            }
        }

        return toDTO(ordenRepo.save(orden));
    }

    @Override
    public OrdenTrabajoResponseDTO obtenerPorId(Integer id) { return toDTO(buscar(id)); }

    @Override
    public List<OrdenTrabajoResponseDTO> listarTodos() {
        return ordenRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OrdenTrabajoResponseDTO> listarPorCliente(Integer clienteId) {
        return ordenRepo.findByClienteId(clienteId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OrdenTrabajoResponseDTO> listarPorMecanico(Integer mecanicoId) {
        return ordenRepo.findByMecanicoId(mecanicoId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<OrdenTrabajoResponseDTO> listarPorVehiculo(String patente) {
        return ordenRepo.findByVehiculoPatente(patente).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO cambiarEstado(Integer id, EstadoOrden nuevoEstado) {
        OrdenTrabajo orden = buscar(id);
        List<EstadoOrden> permitidos = TRANSICIONES.get(orden.getEstado());
        if (!permitidos.contains(nuevoEstado))
            throw new BusinessRuleException("Transición inválida: " + orden.getEstado() + " → " + nuevoEstado);
        orden.setEstado(nuevoEstado);
        return toDTO(ordenRepo.save(orden));
    }

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO enviarPresupuesto(Integer ordenId, PresupuestoRequestDTO req) {
        OrdenTrabajo orden = buscar(ordenId);
        if (orden.getEstado() != EstadoOrden.PENDIENTE)
            throw new BusinessRuleException("Solo se puede enviar presupuesto a órdenes en estado PENDIENTE");

        orden.setPresupuestoTotal(req.getPresupuestoTotal());
        if (req.getFechaFinalizacionEstimada() != null)
            orden.setFechaFinalizacionEstimada(req.getFechaFinalizacionEstimada());
        if (req.getComentarios() != null)
            orden.setComentarios(req.getComentarios());
        orden.setEstado(EstadoOrden.PENDIENTE_APROBACION);
        ordenRepo.save(orden);

        // Notificar al cliente
        Integer usuarioClienteId = orden.getCliente().getUsuario().getId();
        notificacionService.crear(
                usuarioClienteId,
                "Tu mecánico envió un presupuesto de $" + req.getPresupuestoTotal() +
                        " para la orden #" + ordenId + ". Por favor, revísalo y aprueba o rechaza.",
                "PRESUPUESTO",
                ordenId
        );

        return toDTO(orden);
    }

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO aprobarPresupuesto(Integer ordenId) {
        OrdenTrabajo orden = buscar(ordenId);
        if (orden.getEstado() != EstadoOrden.PENDIENTE_APROBACION)
            throw new BusinessRuleException("La orden no está esperando aprobación de presupuesto");

        orden.setEstado(EstadoOrden.EN_PROGRESO);
        ordenRepo.save(orden);

        if (orden.getMecanico() != null) {
            notificacionService.crear(
                    orden.getMecanico().getUsuario().getId(),
                    "El cliente aprobó el presupuesto de la orden #" + ordenId + ". ¡Puedes comenzar el trabajo!",
                    "PRESUPUESTO_APROBADO",
                    ordenId
            );
        }

        return toDTO(orden);
    }

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO rechazarPresupuesto(Integer ordenId) {
        OrdenTrabajo orden = buscar(ordenId);
        if (orden.getEstado() != EstadoOrden.PENDIENTE_APROBACION)
            throw new BusinessRuleException("La orden no está esperando aprobación de presupuesto");

        orden.setEstado(EstadoOrden.PENDIENTE);
        ordenRepo.save(orden);

        // Notificar al mecánico
        if (orden.getMecanico() != null) {
            notificacionService.crear(
                    orden.getMecanico().getUsuario().getId(),
                    "El cliente rechazó el presupuesto de la orden #" + ordenId + ". Por favor, negocia un nuevo monto.",
                    "PRESUPUESTO_RECHAZADO",
                    ordenId
            );
        }

        return toDTO(orden);
    }

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO marcarCompletada(Integer ordenId) {
        OrdenTrabajo orden = buscar(ordenId);
        if (orden.getEstado() != EstadoOrden.EN_PROGRESO)
            throw new BusinessRuleException("Solo se pueden completar órdenes EN_PROGRESO");

        orden.setEstado(EstadoOrden.COMPLETADA);
        generarFactura(orden);
        ordenRepo.save(orden);

        // Notificar al cliente
        notificacionService.crear(
                orden.getCliente().getUsuario().getId(),
                "¡Tu vehículo está listo! La orden #" + ordenId + " ha sido completada. Puedes proceder al pago.",
                "ORDEN_LISTA",
                ordenId
        );

        return toDTO(orden);
    }

    @Override
    @Transactional
    public void cancelar(Integer id) {
        OrdenTrabajo orden = buscar(id);
        List<EstadoOrden> permitidos = TRANSICIONES.get(orden.getEstado());
        if (!permitidos.contains(EstadoOrden.CANCELADA))
            throw new BusinessRuleException("No se puede cancelar una orden en estado: " + orden.getEstado());
        orden.setEstado(EstadoOrden.CANCELADA);
        ordenRepo.save(orden);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void generarFactura(OrdenTrabajo orden) {
        // Evitar duplicar facturas si ya existe
        if (orden.getFactura() != null) return;

        BigDecimal subtotal = orden.getServicios().stream()
                .map(OrdenServicio::getPrecioAplicado)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(orden.getRepuestos().stream()
                        .map(r -> r.getPrecioAplicado().multiply(BigDecimal.valueOf(r.getCantidad())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal impuestos = subtotal.multiply(BigDecimal.valueOf(0.13));
        Factura f = new Factura(null, orden, subtotal, impuestos, subtotal.add(impuestos), EstadoPago.PENDIENTE, null);
        facturaRepo.save(f);
    }

    private OrdenTrabajo buscar(Integer id) {
        return ordenRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada: " + id));
    }

    private OrdenTrabajoResponseDTO toDTO(OrdenTrabajo o) {
        List<OrdenServicioResponseDTO> servicios = o.getServicios().stream()
                .map(s -> new OrdenServicioResponseDTO(
                        s.getServicio().getId(), s.getServicio().getNombre(), s.getPrecioAplicado()))
                .collect(Collectors.toList());

        List<OrdenRepuestoResponseDTO> repuestos = o.getRepuestos().stream()
                .map(r -> new OrdenRepuestoResponseDTO(
                        r.getRepuesto().getId(), r.getRepuesto().getNombre(), r.getCantidad(), r.getPrecioAplicado()))
                .collect(Collectors.toList());

        String mecanicoNombre = o.getMecanico() != null
                ? o.getMecanico().getUsuario().getNombre() + " " + o.getMecanico().getUsuario().getApellido()
                : null;
        Integer mecanicoId = o.getMecanico() != null ? o.getMecanico().getId() : null;
        String sucursalNombre = o.getSucursal() != null ? o.getSucursal().getNombre() : null;
        Integer sucursalId = o.getSucursal() != null ? o.getSucursal().getId() : null;

        return new OrdenTrabajoResponseDTO(
                o.getId(),
                o.getVehiculo().getPatente(),
                o.getCliente().getUsuario().getNombre() + " " + o.getCliente().getUsuario().getApellido(),
                mecanicoNombre, mecanicoId,
                sucursalNombre, sucursalId,
                o.getTipoOrden().name(),
                o.getEstado().name(),
                o.getFechaCreacion(),
                o.getFechaFinalizacionEstimada(),
                o.getComentarios(),
                o.getPresupuestoTotal(),
                servicios, repuestos
        );
    }
}
