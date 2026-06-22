package com.example.backend_tallerautomotriz.service.impl;

import com.example.backend_tallerautomotriz.dto.request.InventarioRequestDTO;
import com.example.backend_tallerautomotriz.dto.response.InventarioResponseDTO;
import com.example.backend_tallerautomotriz.entity.Inventario;
import com.example.backend_tallerautomotriz.entity.Repuesto;
import com.example.backend_tallerautomotriz.entity.Sucursal;
import com.example.backend_tallerautomotriz.enums.CategoriaRepuesto;
import com.example.backend_tallerautomotriz.exception.BusinessRuleException;
import com.example.backend_tallerautomotriz.exception.DuplicateResourceException;
import com.example.backend_tallerautomotriz.exception.EntityNotFoundException;
import com.example.backend_tallerautomotriz.repository.InventarioRepository;
import com.example.backend_tallerautomotriz.repository.RepuestoRepository;
import com.example.backend_tallerautomotriz.repository.SucursalRepository;
import com.example.backend_tallerautomotriz.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {
    private final InventarioRepository repo;
    private final SucursalRepository sucursalRepo;
    private final RepuestoRepository repuestoRepo;

    @Override
    @Transactional
    public InventarioResponseDTO crear(InventarioRequestDTO req) {
        Sucursal sucursal = sucursalRepo.findById(req.getSucursalId())
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
        Repuesto repuesto = repuestoRepo.findById(req.getRepuestoId())
                .orElseThrow(() -> new EntityNotFoundException("Repuesto no encontrado"));
        if (repo.findBySucursalIdAndRepuestoId(req.getSucursalId(), req.getRepuestoId()).isPresent()) {
            throw new DuplicateResourceException("Ya existe inventario para este repuesto en esta sucursal");
        }
        Inventario inventario = new Inventario(null, sucursal, repuesto, req.getStockTotal(), LocalDate.now());
        return toDTO(repo.save(inventario));
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioResponseDTO obtenerPorId(Integer id) {
        return toDTO(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioResponseDTO> listarPorSucursal(Integer sucursalId) {
        if (!sucursalRepo.existsById(sucursalId)) {
            throw new EntityNotFoundException("Sucursal no encontrada: " + sucursalId);
        }
        return repo.findBySucursalId(sucursalId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public InventarioResponseDTO actualizar(Integer id, InventarioRequestDTO req) {
        Inventario inventario = repo.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + id));
        if (!inventario.getSucursal().getId().equals(req.getSucursalId())
                || !inventario.getRepuesto().getId().equals(req.getRepuestoId())) {
            throw new BusinessRuleException("No se puede cambiar la sucursal o repuesto de un inventario");
        }
        inventario.setStockTotal(req.getStockTotal());
        inventario.setFechaActualizacion(LocalDate.now());
        return toDTO(repo.save(inventario));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        repo.delete(buscar(id));
    }

    private Inventario buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + id));
    }

    @Override
    public List<InventarioResponseDTO> filtrar(Integer sucursalId, String categoria, String nombre) {
        List<Inventario> resultado;
        boolean tieneCategoria = categoria != null && !categoria.isBlank();
        boolean tieneNombre    = nombre    != null && !nombre.isBlank();

        if (tieneCategoria && tieneNombre) {
            resultado = repo.findBySucursalIdAndCategoriaAndNombre(sucursalId, parsearCategoria(categoria), nombre);
        } else if (tieneCategoria) {
            resultado = repo.findBySucursalIdAndCategoria(sucursalId, parsearCategoria(categoria));
        } else if (tieneNombre) {
            resultado = repo.findBySucursalIdAndNombre(sucursalId, nombre);
        } else {
            resultado = repo.findBySucursalId(sucursalId);
        }

        return resultado.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private CategoriaRepuesto parsearCategoria(String cat) {
        if (cat == null || cat.isBlank()) {
            throw new BusinessRuleException("Categoria obligatoria");
        }
        try {
            return CategoriaRepuesto.valueOf(cat.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Categoría inválida: " + cat);
        }
    }

    private InventarioResponseDTO toDTO(Inventario i) {
        return new InventarioResponseDTO(
                i.getId(),
                i.getSucursal().getId(), i.getSucursal().getNombre(),
                i.getRepuesto().getId(), i.getRepuesto().getNombre(),
                i.getRepuesto().getCategoria().name(),
                i.getRepuesto().getPrecioUnitario(),
                i.getStockTotal(), i.getFechaActualizacion());
    }
}
