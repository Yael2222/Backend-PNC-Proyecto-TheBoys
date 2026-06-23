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
import com.example.backend_tallerautomotriz.enums.EstadoServicio;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
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

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO crear(OrdenTrabajoRequestDTO request) {
        validarElementosUnicos(request);
        Cliente cliente = clienteRepo.findById(request.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + request.getClienteId()));
        Vehiculo vehiculo = vehiculoRepo.findById(request.getPatente().trim())
                .orElseThrow(() -> new EntityNotFoundException("Vehiculo no encontrado: " + request.getPatente()));
        if (!vehiculo.getCliente().getId().equals(cliente.getId())) {
            throw new BusinessRuleException("El vehiculo no pertenece al cliente seleccionado");
        }

        Mecanico mecanico = buscarMecanico(request.getMecanicoId());
        Sucursal sucursal = resolverSucursal(request.getSucursalId(), mecanico);
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setVehiculo(vehiculo);
        orden.setCliente(cliente);
        orden.setMecanico(mecanico);
        orden.setSucursal(sucursal);
        orden.setTipoOrden(request.getTipoOrden());
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setFechaCreacion(LocalDate.now());
        orden.setComentarios(normalizarOpcional(request.getComentarios()));
        orden = ordenRepo.save(orden);

        agregarServicios(orden, request.getServicios());
        agregarRepuestos(orden, sucursal, request.getRepuestos());
        return toDTO(ordenRepo.save(orden));
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenTrabajoResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponseDTO> listarTodos() {
        return ordenRepo.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponseDTO> listarPorCliente(Integer clienteId) {
        validarClienteExiste(clienteId);
        return ordenRepo.findByClienteIdOrderByFechaCreacionDesc(clienteId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponseDTO> listarPorMecanico(Integer mecanicoId) {
        validarMecanicoExiste(mecanicoId);
        return ordenRepo.findByMecanicoIdOrderByFechaCreacionDesc(mecanicoId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponseDTO> listarPorVehiculo(String patente) {
        if (!vehiculoRepo.existsById(patente)) {
            throw new EntityNotFoundException("Vehiculo no encontrado: " + patente);
        }
        return ordenRepo.findByVehiculoPatenteOrderByFechaCreacionDesc(patente).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponseDTO> listarPendientes(Integer sucursalId) {
        if (sucursalId != null) {
            if (!sucursalRepo.existsById(sucursalId)) {
                throw new EntityNotFoundException("Sucursal no encontrada: " + sucursalId);
            }
            return ordenRepo.findBySucursalIdAndMecanicoIsNullAndEstadoOrderByFechaCreacionAsc(
                            sucursalId, EstadoOrden.PENDIENTE)
                    .stream().map(this::toDTO).toList();
        }
        return ordenRepo.findByMecanicoIsNullAndEstadoOrderByFechaCreacionAsc(EstadoOrden.PENDIENTE)
                .stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO asignarMecanico(Integer ordenId, Integer mecanicoId) {
        OrdenTrabajo orden = buscarParaActualizar(ordenId);
        if (orden.getMecanico() != null) {
            throw new BusinessRuleException("La orden ya tiene un mecanico asignado");
        }
        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new BusinessRuleException("Solo se pueden reclamar ordenes pendientes");
        }

        Mecanico mecanico = mecanicoRepo.findByIdForUpdate(mecanicoId)
                .orElseThrow(() -> new EntityNotFoundException("Mecanico no encontrado: " + mecanicoId));

        if (orden.getSucursal() != null && !mecanico.getSucursal().getId().equals(orden.getSucursal().getId())) {
            throw new BusinessRuleException("El mecanico no pertenece a la sucursal de la orden");
        }

        orden.setMecanico(mecanico);
        if (orden.getSucursal() == null) {
            orden.setSucursal(mecanico.getSucursal());
        }
        OrdenTrabajo guardada = ordenRepo.save(orden);
        notificar(
                orden.getCliente().getUsuario().getId(),
                "Un mecanico fue asignado a tu orden #" + ordenId,
                "ORDEN_MECANICO_ASIGNADO",
                ordenId);
        return toDTO(guardada);
    }

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO cambiarEstado(Integer id, EstadoOrden estado) {
        if (estado != EstadoOrden.CANCELADA) {
            throw new BusinessRuleException("Use el endpoint especifico para cambiar a " + estado);
        }
        OrdenTrabajo orden = buscarParaActualizar(id);
        cancelarOrden(orden);
        return toDTO(ordenRepo.save(orden));
    }

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO enviarPresupuesto(Integer ordenId, PresupuestoRequestDTO request) {
        OrdenTrabajo orden = buscarParaActualizar(ordenId);

        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new BusinessRuleException("Solo se puede enviar presupuesto a ordenes pendientes");
        }

        if (orden.getMecanico() == null) {
            throw new BusinessRuleException("La orden debe tener un mecanico asignado");
        }

        validarElementosUnicosPresupuesto(request);

        Sucursal sucursal = obtenerSucursalParaOrden(orden);

        if (orden.getSucursal() == null && sucursal != null) {
            orden.setSucursal(sucursal);
        }

        agregarServiciosAdicionales(orden, request.getServicios());
        agregarRepuestosAdicionales(orden, sucursal, request.getRepuestos());

        orden.setPresupuestoTotal(request.getPresupuestoTotal().setScale(2, RoundingMode.HALF_UP));
        orden.setFechaFinalizacionEstimada(request.getFechaFinalizacionEstimada());

        if (request.getComentarios() != null) {
            orden.setComentarios(request.getComentarios().trim());
        }

        orden.setEstado(EstadoOrden.PENDIENTE_APROBACION);

        OrdenTrabajo guardada = ordenRepo.save(orden);

        notificar(
                orden.getCliente().getUsuario().getId(),
                "Se envio un presupuesto para la orden #" + ordenId + " por $" + orden.getPresupuestoTotal(),
                "PRESUPUESTO",
                ordenId
        );

        return toDTO(guardada);
    }

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO aprobarPresupuesto(Integer ordenId) {
        OrdenTrabajo orden = buscarParaActualizar(ordenId);
        validarPendienteAprobacion(orden);
        orden.setEstado(EstadoOrden.EN_PROGRESO);
        OrdenTrabajo guardada = ordenRepo.save(orden);
        notificarMecanico(orden, "El cliente aprobo el presupuesto de la orden #" + ordenId, "PRESUPUESTO_APROBADO");
        return toDTO(guardada);
    }

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO rechazarPresupuesto(Integer ordenId) {
        OrdenTrabajo orden = buscarParaActualizar(ordenId);
        validarPendienteAprobacion(orden);
        orden.setEstado(EstadoOrden.PENDIENTE);
        OrdenTrabajo guardada = ordenRepo.save(orden);
        notificarMecanico(orden, "El cliente rechazo el presupuesto de la orden #" + ordenId, "PRESUPUESTO_RECHAZADO");
        return toDTO(guardada);
    }

    @Override
    @Transactional
    public OrdenTrabajoResponseDTO marcarCompletada(Integer ordenId) {
        OrdenTrabajo orden = buscarParaActualizar(ordenId);
        if (orden.getEstado() != EstadoOrden.EN_PROGRESO) {
            throw new BusinessRuleException("Solo se pueden completar ordenes en progreso");
        }
        orden.setEstado(EstadoOrden.COMPLETADA);
        generarFactura(orden);
        OrdenTrabajo guardada = ordenRepo.save(orden);
        notificar(
                orden.getCliente().getUsuario().getId(),
                "Tu vehiculo esta listo. La orden #" + ordenId + " fue completada",
                "ORDEN_LISTA",
                ordenId);
        return toDTO(guardada);
    }

    @Override
    @Transactional
    public void cancelar(Integer id) {
        OrdenTrabajo orden = buscarParaActualizar(id);
        cancelarOrden(orden);
        ordenRepo.save(orden);
    }

    private Sucursal resolverSucursal(Integer sucursalId, Mecanico mecanico) {
        if (mecanico != null) {
            if (sucursalId != null && !mecanico.getSucursal().getId().equals(sucursalId)) {
                throw new BusinessRuleException("El mecanico no pertenece a la sucursal seleccionada");
            }
            return mecanico.getSucursal();
        }
        if (sucursalId == null) {
            throw new BusinessRuleException("Debe seleccionar una sucursal");
        }
        return sucursalRepo.findById(sucursalId)
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada: " + sucursalId));
    }

    private void validarElementosUnicos(OrdenTrabajoRequestDTO request) {
        Set<Integer> servicios = new HashSet<>();
        for (OrdenServicioRequestDTO servicio : request.getServicios()) {
            if (!servicios.add(servicio.getServicioId())) {
                throw new BusinessRuleException("No se permiten servicios duplicados en una orden");
            }
        }
        Set<Integer> repuestos = new HashSet<>();
        if (request.getRepuestos() != null) {
            for (OrdenRepuestoRequestDTO repuesto : request.getRepuestos()) {
                if (!repuestos.add(repuesto.getRepuestoId())) {
                    throw new BusinessRuleException("No se permiten repuestos duplicados en una orden");
                }
            }
        }
    }

    private Mecanico buscarMecanico(Integer mecanicoId) {
        if (mecanicoId == null) {
            return null;
        }
        return mecanicoRepo.findById(mecanicoId)
                .orElseThrow(() -> new EntityNotFoundException("Mecanico no encontrado: " + mecanicoId));
    }

    private void agregarServicios(OrdenTrabajo orden, List<OrdenServicioRequestDTO> solicitudes) {
        for (OrdenServicioRequestDTO solicitud : solicitudes) {
            Servicio servicio = servicioRepo.findById(solicitud.getServicioId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Servicio no encontrado: " + solicitud.getServicioId()
                    ));

            if (servicio.getEstado() != EstadoServicio.ACTIVO) {
                throw new BusinessRuleException("El servicio no esta activo: " + servicio.getId());
            }

            BigDecimal precioAplicado = solicitud.getPrecioAplicado() != null
                    ? solicitud.getPrecioAplicado()
                    : servicio.getPrecioBase();

            orden.getServicios().add(new OrdenServicio(
                    new OrdenServicioId(orden.getId(), servicio.getId()),
                    orden,
                    servicio,
                    precioAplicado.setScale(2, RoundingMode.HALF_UP)
            ));
        }
    }


    private void agregarRepuestos(OrdenTrabajo orden, Sucursal sucursal, List<OrdenRepuestoRequestDTO> solicitudes) {
        if (solicitudes == null) {
            return;
        }

        List<OrdenRepuestoRequestDTO> ordenadas = solicitudes.stream()
                .sorted(Comparator.comparing(OrdenRepuestoRequestDTO::getRepuestoId))
                .toList();

        for (OrdenRepuestoRequestDTO solicitud : ordenadas) {
            if (solicitud.getCantidad() == null || solicitud.getCantidad() <= 0) {
                throw new BusinessRuleException(
                        "La cantidad del repuesto debe ser mayor a cero: " + solicitud.getRepuestoId()
                );
            }

            Repuesto repuesto = repuestoRepo.findById(solicitud.getRepuestoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Repuesto no encontrado: " + solicitud.getRepuestoId()
                    ));

            Inventario inventario = buscarInventarioParaActualizar(sucursal.getId(), repuesto.getId());

            if (inventario.getStockTotal() < solicitud.getCantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para repuesto id: " + repuesto.getId()
                );
            }

            inventario.setStockTotal(inventario.getStockTotal() - solicitud.getCantidad());
            inventario.setFechaActualizacion(LocalDate.now());
            inventarioRepo.save(inventario);

            BigDecimal precioAplicado = solicitud.getPrecioAplicado() != null
                    ? solicitud.getPrecioAplicado()
                    : repuesto.getPrecioUnitario();

            orden.getRepuestos().add(new OrdenRepuesto(
                    new OrdenRepuestoId(orden.getId(), repuesto.getId()),
                    orden,
                    repuesto,
                    solicitud.getCantidad(),
                    precioAplicado.setScale(2, RoundingMode.HALF_UP)
            ));
        }
    }

    private void cancelarOrden(OrdenTrabajo orden) {
        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            return;
        }
        if (orden.getEstado() == EstadoOrden.COMPLETADA) {
            throw new BusinessRuleException("No se puede cancelar una orden completada");
        }
        reponerInventario(orden);
        orden.setEstado(EstadoOrden.CANCELADA);
    }

    private void reponerInventario(OrdenTrabajo orden) {
        Sucursal sucursal = orden.getSucursal() != null
                ? orden.getSucursal()
                : orden.getMecanico() == null ? null : orden.getMecanico().getSucursal();
        if (sucursal == null && !orden.getRepuestos().isEmpty()) {
            throw new BusinessRuleException("La orden no tiene una sucursal asociada para reponer inventario");
        }
        List<OrdenRepuesto> ordenados = orden.getRepuestos().stream()
                .sorted(Comparator.comparing(repuesto -> repuesto.getRepuesto().getId()))
                .toList();
        for (OrdenRepuesto ordenRepuesto : ordenados) {
            Inventario inventario = buscarInventarioParaActualizar(
                    sucursal.getId(),
                    ordenRepuesto.getRepuesto().getId());
            inventario.setStockTotal(inventario.getStockTotal() + ordenRepuesto.getCantidad());
            inventario.setFechaActualizacion(LocalDate.now());
            inventarioRepo.save(inventario);
        }
    }

    private Inventario buscarInventarioParaActualizar(Integer sucursalId, Integer repuestoId) {
        return inventarioRepo.findBySucursalIdAndRepuestoIdForUpdate(sucursalId, repuestoId)
                .orElseThrow(() -> new EntityNotFoundException("Repuesto sin inventario: " + repuestoId));
    }

    private void validarPendienteAprobacion(OrdenTrabajo orden) {
        if (orden.getEstado() != EstadoOrden.PENDIENTE_APROBACION) {
            throw new BusinessRuleException("La orden no esta esperando aprobacion de presupuesto");
        }
    }

    private void generarFactura(OrdenTrabajo orden) {
        if (facturaRepo.findByOrdenId(orden.getId()).isPresent()) {
            throw new BusinessRuleException("La orden ya tiene una factura");
        }
        BigDecimal subtotalServicios = orden.getServicios().stream()
                .map(OrdenServicio::getPrecioAplicado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotalRepuestos = orden.getRepuestos().stream()
                .map(repuesto -> repuesto.getPrecioAplicado().multiply(BigDecimal.valueOf(repuesto.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotal = subtotalServicios.add(subtotalRepuestos).setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuestos = subtotal.multiply(BigDecimal.valueOf(0.13)).setScale(2, RoundingMode.HALF_UP);
        facturaRepo.save(new Factura(
                null,
                orden,
                subtotal,
                impuestos,
                subtotal.add(impuestos).setScale(2, RoundingMode.HALF_UP),
                EstadoPago.PENDIENTE,
                null));
    }

    private void notificarMecanico(OrdenTrabajo orden, String mensaje, String tipo) {
        if (orden.getMecanico() != null) {
            notificar(orden.getMecanico().getUsuario().getId(), mensaje, tipo, orden.getId());
        }
    }

    private void notificar(Integer usuarioId, String mensaje, String tipo, Integer referenciaId) {
        notificacionService.crear(usuarioId, mensaje, tipo, referenciaId);
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

    private String normalizarOpcional(String valor) {
        return valor == null ? null : valor.trim();
    }

    private OrdenTrabajo buscar(Integer id) {
        return ordenRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Orden no encontrada: " + id));
    }

    private OrdenTrabajo buscarParaActualizar(Integer id) {
        return ordenRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada: " + id));
    }

    private OrdenTrabajoResponseDTO toDTO(OrdenTrabajo orden) {
        List<OrdenServicioResponseDTO> servicios = orden.getServicios().stream()
                .map(servicio -> new OrdenServicioResponseDTO(
                        servicio.getServicio().getId(),
                        servicio.getServicio().getNombre(),
                        servicio.getPrecioAplicado()))
                .toList();
        List<OrdenRepuestoResponseDTO> repuestos = orden.getRepuestos().stream()
                .map(repuesto -> new OrdenRepuestoResponseDTO(
                        repuesto.getRepuesto().getId(),
                        repuesto.getRepuesto().getNombre(),
                        repuesto.getCantidad(),
                        repuesto.getPrecioAplicado()))
                .toList();
        String mecanicoNombre = orden.getMecanico() == null
                ? null
                : orden.getMecanico().getUsuario().getNombre() + " " + orden.getMecanico().getUsuario().getApellido();
        return new OrdenTrabajoResponseDTO(
                orden.getId(),
                orden.getVehiculo().getPatente(),
                orden.getCliente().getUsuario().getNombre() + " " + orden.getCliente().getUsuario().getApellido(),
                mecanicoNombre,
                orden.getMecanico() == null ? null : orden.getMecanico().getId(),
                orden.getSucursal() == null ? null : orden.getSucursal().getNombre(),
                orden.getSucursal() == null ? null : orden.getSucursal().getId(),
                orden.getTipoOrden().name(),
                orden.getEstado().name(),
                orden.getFechaCreacion(),
                orden.getFechaFinalizacionEstimada(),
                orden.getComentarios(),
                orden.getPresupuestoTotal(),
                servicios,
                repuestos);
    }

    //Helpers
    private void validarElementosUnicosPresupuesto(PresupuestoRequestDTO request) {
        Set<Integer> servicios = new HashSet<>();

        if (request.getServicios() != null) {
            for (OrdenServicioRequestDTO servicio : request.getServicios()) {
                if (!servicios.add(servicio.getServicioId())) {
                    throw new BusinessRuleException("No se permiten servicios duplicados en el presupuesto");
                }
            }
        }

        Set<Integer> repuestos = new HashSet<>();

        if (request.getRepuestos() != null) {
            for (OrdenRepuestoRequestDTO repuesto : request.getRepuestos()) {
                if (!repuestos.add(repuesto.getRepuestoId())) {
                    throw new BusinessRuleException("No se permiten repuestos duplicados en el presupuesto");
                }
            }
        }
    }

    private Sucursal obtenerSucursalParaOrden(OrdenTrabajo orden) {
        if (orden.getSucursal() != null) {
            return orden.getSucursal();
        }

        if (orden.getMecanico() != null && orden.getMecanico().getSucursal() != null) {
            return orden.getMecanico().getSucursal();
        }

        return null;
    }

    private void agregarServiciosAdicionales(
            OrdenTrabajo orden,
            List<OrdenServicioRequestDTO> solicitudes
    ) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            return;
        }

        Set<Integer> serviciosExistentes = new HashSet<>();

        for (OrdenServicio ordenServicio : orden.getServicios()) {
            serviciosExistentes.add(ordenServicio.getServicio().getId());
        }

        for (OrdenServicioRequestDTO solicitud : solicitudes) {
            if (serviciosExistentes.contains(solicitud.getServicioId())) {
                throw new BusinessRuleException(
                        "El servicio ya esta agregado a la orden: " + solicitud.getServicioId()
                );
            }

            Servicio servicio = servicioRepo.findById(solicitud.getServicioId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Servicio no encontrado: " + solicitud.getServicioId()
                    ));

            if (servicio.getEstado() != EstadoServicio.ACTIVO) {
                throw new BusinessRuleException("El servicio no esta activo: " + servicio.getId());
            }

            BigDecimal precioAplicado = solicitud.getPrecioAplicado() != null
                    ? solicitud.getPrecioAplicado()
                    : servicio.getPrecioBase();

            orden.getServicios().add(new OrdenServicio(
                    new OrdenServicioId(orden.getId(), servicio.getId()),
                    orden,
                    servicio,
                    precioAplicado.setScale(2, RoundingMode.HALF_UP)
            ));

            serviciosExistentes.add(servicio.getId());
        }
    }

    private void agregarRepuestosAdicionales(
            OrdenTrabajo orden,
            Sucursal sucursal,
            List<OrdenRepuestoRequestDTO> solicitudes
    ) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            return;
        }

        if (sucursal == null) {
            throw new BusinessRuleException("La orden no tiene una sucursal asociada para usar repuestos");
        }

        Set<Integer> repuestosExistentes = new HashSet<>();

        for (OrdenRepuesto ordenRepuesto : orden.getRepuestos()) {
            repuestosExistentes.add(ordenRepuesto.getRepuesto().getId());
        }

        List<OrdenRepuestoRequestDTO> ordenadas = solicitudes.stream()
                .sorted(Comparator.comparing(OrdenRepuestoRequestDTO::getRepuestoId))
                .toList();

        for (OrdenRepuestoRequestDTO solicitud : ordenadas) {
            if (repuestosExistentes.contains(solicitud.getRepuestoId())) {
                throw new BusinessRuleException(
                        "El repuesto ya esta agregado a la orden: " + solicitud.getRepuestoId()
                );
            }

            if (solicitud.getCantidad() == null || solicitud.getCantidad() <= 0) {
                throw new BusinessRuleException(
                        "La cantidad del repuesto debe ser mayor a cero: " + solicitud.getRepuestoId()
                );
            }

            Repuesto repuesto = repuestoRepo.findById(solicitud.getRepuestoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Repuesto no encontrado: " + solicitud.getRepuestoId()
                    ));

            Inventario inventario = buscarInventarioParaActualizar(sucursal.getId(), repuesto.getId());

            if (inventario.getStockTotal() < solicitud.getCantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para repuesto id: " + repuesto.getId()
                );
            }

            inventario.setStockTotal(inventario.getStockTotal() - solicitud.getCantidad());
            inventario.setFechaActualizacion(LocalDate.now());
            inventarioRepo.save(inventario);

            BigDecimal precioAplicado = solicitud.getPrecioAplicado() != null
                    ? solicitud.getPrecioAplicado()
                    : repuesto.getPrecioUnitario();

            orden.getRepuestos().add(new OrdenRepuesto(
                    new OrdenRepuestoId(orden.getId(), repuesto.getId()),
                    orden,
                    repuesto,
                    solicitud.getCantidad(),
                    precioAplicado.setScale(2, RoundingMode.HALF_UP)
            ));

            repuestosExistentes.add(repuesto.getId());
        }
    }

}