package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.OrdenRepuestoRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.OrdenServicioRequestDTO;
import com.example.backend_tallerautomotriz.dto.request.OrdenTrabajoRequestDTO;
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
import com.example.backend_tallerautomotriz.service.OrdenTrabajoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class OrdenTrabajoServiceImpl implements OrdenTrabajoService {

    private final OrdenTrabajoRepository ordenRepo;
    private final ClienteRepository clienteRepo;
    private final MecanicoRepository mecanicoRepo;
    private final VehiculoRepository vehiculoRepo;
    private final ServicioRepository servicioRepo;
    private final RepuestoRepository repuestoRepo;
    private final InventarioRepository inventarioRepo;
    private final FacturaRepository facturaRepo;

    private static final Map<EstadoOrden, List<EstadoOrden>> TRANSICIONES = Map.of(
            EstadoOrden.PENDIENTE,   List.of(EstadoOrden.EN_PROGRESO, EstadoOrden.CANCELADA),
            EstadoOrden.EN_PROGRESO, List.of(EstadoOrden.COMPLETADA, EstadoOrden.CANCELADA),
            EstadoOrden.COMPLETADA,  List.of(),
            EstadoOrden.CANCELADA,   List.of()
    );

    @Override @Transactional
    public OrdenTrabajoResponseDTO crear(OrdenTrabajoRequestDTO req) {
        Cliente cliente = clienteRepo.findById(req.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + req.getClienteId()));
        Vehiculo vehiculo = vehiculoRepo.findById(req.getPatente())
                .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado: " + req.getPatente()));
        Mecanico mecanico = null;
        if (req.getMecanicoId() != null)
            mecanico = mecanicoRepo.findById(req.getMecanicoId())
                    .orElseThrow(() -> new EntityNotFoundException("Mecánico no encontrado"));

        // Verificar stock antes de crear
        if (req.getRepuestos() != null) {
            for (OrdenRepuestoRequestDTO r : req.getRepuestos()) {
                Inventario inv = inventarioRepo
                        .findBySucursalIdAndRepuestoId(
                                mecanico != null ? mecanico.getSucursal().getId() : 1, r.getRepuestoId())
                        .orElseThrow(() -> new EntityNotFoundException("Repuesto sin inventario: " + r.getRepuestoId()));
                if (inv.getStockTotal() < r.getCantidad())
                    throw new StockInsuficienteException("Stock insuficiente para repuesto id: " + r.getRepuestoId());
            }
        }

        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setVehiculo(vehiculo);
        orden.setCliente(cliente);
        orden.setMecanico(mecanico);
        orden.setTipoOrden(TipoOrden.valueOf(req.getTipoOrden().toUpperCase()));
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setFechaCreacion(LocalDate.now());
        orden.setComentarios(req.getComentarios());
        orden = ordenRepo.save(orden);

        // Agregar servicios
        for (OrdenServicioRequestDTO s : req.getServicios()) {
            Servicio servicio = servicioRepo.findById(s.getServicioId())
                    .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado: " + s.getServicioId()));
            OrdenServicio os = new OrdenServicio(
                    new OrdenServicioId(orden.getId(), s.getServicioId()), orden, servicio, s.getPrecioAplicado());
            orden.getServicios().add(os);
        }

        // Agregar repuestos y descontar stock
        if (req.getRepuestos() != null) {
            for (OrdenRepuestoRequestDTO r : req.getRepuestos()) {
                Repuesto repuesto = repuestoRepo.findById(r.getRepuestoId())
                        .orElseThrow(() -> new EntityNotFoundException("Repuesto no encontrado: " + r.getRepuestoId()));
                OrdenRepuesto or2 = new OrdenRepuesto(
                        new OrdenRepuestoId(orden.getId(), r.getRepuestoId()),
                        orden, repuesto, r.getCantidad(), r.getPrecioAplicado());
                orden.getRepuestos().add(or2);
                // Descontar stock
                if (mecanico != null) {
                    inventarioRepo.findBySucursalIdAndRepuestoId(mecanico.getSucursal().getId(), r.getRepuestoId())
                            .ifPresent(inv -> { inv.setStockTotal(inv.getStockTotal() - r.getCantidad()); inventarioRepo.save(inv); });
                }
            }
        }

        return toDTO(ordenRepo.save(orden));
    }

    @Override public OrdenTrabajoResponseDTO obtenerPorId(Integer id) { return toDTO(buscar(id)); }

    @Override public List<OrdenTrabajoResponseDTO> listarTodos() {
        return ordenRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override public List<OrdenTrabajoResponseDTO> listarPorCliente(Integer clienteId) {
        return ordenRepo.findByClienteId(clienteId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override public List<OrdenTrabajoResponseDTO> listarPorMecanico(Integer mecanicoId) {
        return ordenRepo.findByMecanicoId(mecanicoId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override @Transactional
    public OrdenTrabajoResponseDTO cambiarEstado(Integer id, EstadoOrden nuevoEstado) {
        OrdenTrabajo orden = buscar(id);
        List<EstadoOrden> permitidos = TRANSICIONES.get(orden.getEstado());
        if (!permitidos.contains(nuevoEstado))
            throw new BusinessRuleException("Transición inválida: " + orden.getEstado() + " → " + nuevoEstado);
        orden.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoOrden.COMPLETADA) generarFactura(orden);
        return toDTO(ordenRepo.save(orden));
    }

    @Override @Transactional
    public void cancelar(Integer id) { cambiarEstado(id, EstadoOrden.CANCELADA); }

    private void generarFactura(OrdenTrabajo orden) {
        BigDecimal subtotal = orden.getServicios().stream()
                .map(OrdenServicio::getPrecioAplicado).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(orden.getRepuestos().stream()
                        .map(r -> r.getPrecioAplicado().multiply(BigDecimal.valueOf(r.getCantidad())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal impuestos = subtotal.multiply(BigDecimal.valueOf(0.13));
        Factura f = new Factura(null, orden, subtotal, impuestos, subtotal.add(impuestos), EstadoPago.PENDIENTE, null);
        facturaRepo.save(f);
    }

    private OrdenTrabajo buscar(Integer id) {
        return ordenRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Orden no encontrada: " + id));
    }

    private OrdenTrabajoResponseDTO toDTO(OrdenTrabajo o) {
        List<OrdenServicioResponseDTO> servicios = o.getServicios().stream()
                .map(s -> new OrdenServicioResponseDTO(s.getServicio().getId(), s.getServicio().getNombre(), s.getPrecioAplicado()))
                .collect(Collectors.toList());
        List<OrdenRepuestoResponseDTO> repuestos = o.getRepuestos().stream()
                .map(r -> new OrdenRepuestoResponseDTO(r.getRepuesto().getId(), r.getRepuesto().getNombre(), r.getCantidad(), r.getPrecioAplicado()))
                .collect(Collectors.toList());
        String mecanico = o.getMecanico() != null ?
                o.getMecanico().getUsuario().getNombre() + " " + o.getMecanico().getUsuario().getApellido() : null;
        return new OrdenTrabajoResponseDTO(o.getId(), o.getVehiculo().getPatente(),
                o.getCliente().getUsuario().getNombre() + " " + o.getCliente().getUsuario().getApellido(),
                mecanico, o.getTipoOrden().name(), o.getEstado().name(),
                o.getFechaCreacion(), o.getComentarios(), servicios, repuestos);
    }
}

