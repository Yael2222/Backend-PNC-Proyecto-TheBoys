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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository repo;
    private final SucursalRepository sucursalRepo;
    private final RepuestoRepository repuestoRepo;

    @Override
    public InventarioResponseDTO crear(InventarioRequestDTO req) {
        Sucursal s = sucursalRepo.findById(req.getSucursalId())
                .orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
        Repuesto r = repuestoRepo.findById(req.getRepuestoId())
                .orElseThrow(() -> new EntityNotFoundException("Repuesto no encontrado"));
        if (repo.findBySucursalIdAndRepuestoId(req.getSucursalId(), req.getRepuestoId()).isPresent())
            throw new DuplicateResourceException("Ya existe inventario para este repuesto en esta sucursal");
        Inventario inv = new Inventario(null, s, r, req.getStockTotal(), LocalDate.now());
        return toDTO(repo.save(inv));
    }

    @Override
    public InventarioResponseDTO obtenerPorId(Integer id) { return toDTO(buscar(id)); }

    @Override
    public List<InventarioResponseDTO> listarPorSucursal(Integer sucursalId) {
        return repo.findBySucursalId(sucursalId).stream().map(this::toDTO).collect(Collectors.toList());
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

    @Override
    public InventarioResponseDTO actualizar(Integer id, InventarioRequestDTO req) {
        Inventario inv = buscar(id);
        inv.setStockTotal(req.getStockTotal());
        inv.setFechaActualizacion(LocalDate.now());
        return toDTO(repo.save(inv));
    }

    @Override
    public void eliminar(Integer id) { buscar(id); repo.deleteById(id); }

    private CategoriaRepuesto parsearCategoria(String cat) {
        try {
            return CategoriaRepuesto.valueOf(cat.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Categoría inválida: " + cat);
        }
    }

    private Inventario buscar(Integer id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + id));
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
