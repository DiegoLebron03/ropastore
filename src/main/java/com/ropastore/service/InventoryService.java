package com.ropastore.service;

import com.ropastore.dto.TransaccionDTO;
import com.ropastore.entity.Producto;
import com.ropastore.entity.Transaccion;
import com.ropastore.enums.TipoTransaccion;
import com.ropastore.exception.InventarioInsuficienteException;
import com.ropastore.exception.ProductoInactivoException;
import com.ropastore.exception.ResourceNotFoundException;
import com.ropastore.repository.ProductoRepository;
import com.ropastore.repository.TransaccionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final ProductoRepository productoRepository;
    private final TransaccionRepository transaccionRepository;

    public InventoryService(ProductoRepository productoRepository, TransaccionRepository transaccionRepository) {
        this.productoRepository = productoRepository;
        this.transaccionRepository = transaccionRepository;
    }

    /**
     * Registra una entrada de inventario aumentando la existencia.
     */
    @Transactional
    public Transaccion registrarEntrada(Long productoId, TipoTransaccion tipo, Integer cantidad,
                                        BigDecimal costoUnitario, String observacion, LocalDateTime fecha) {
        if (!tipo.isEntrada()) {
            throw new IllegalArgumentException("El tipo de transacción debe corresponder a una entrada.");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        if (!producto.isActivo()) {
            throw new ProductoInactivoException(producto.getCodigo());
        }

        int stockPrevio = producto.getStockActual() != null ? producto.getStockActual() : 0;
        int nuevoBalance = stockPrevio + cantidad;

        producto.setStockActual(nuevoBalance);
        productoRepository.save(producto);

        LocalDateTime fechaOperacion = fecha != null ? fecha : LocalDateTime.now();
        BigDecimal costo = costoUnitario != null ? costoUnitario : producto.getCosto();

        Transaccion transaccion = new Transaccion(
            producto,
            fechaOperacion,
            tipo,
            cantidad,
            costo,
            nuevoBalance,
            observacion
        );

        return transaccionRepository.save(transaccion);
    }

    /**
     * Registra una salida de inventario disminuyendo la existencia.
     * REGLA CRÍTICA: Prohibido permitir una salida superior al stock disponible.
     */
    @Transactional
    public Transaccion registrarSalida(Long productoId, TipoTransaccion tipo, Integer cantidad,
                                       BigDecimal costoUnitario, String observacion, LocalDateTime fecha) {
        if (!tipo.isSalida()) {
            throw new IllegalArgumentException("El tipo de transacción debe corresponder a una salida.");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        if (!producto.isActivo()) {
            throw new ProductoInactivoException(producto.getCodigo());
        }

        int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;

        // Validación estricta en Backend: No permitir salida superior a la existencia
        if (cantidad > stockActual) {
            throw new InventarioInsuficienteException(producto.getCodigo(), stockActual, cantidad);
        }

        int nuevoBalance = stockActual - cantidad;
        producto.setStockActual(nuevoBalance);
        productoRepository.save(producto);

        LocalDateTime fechaOperacion = fecha != null ? fecha : LocalDateTime.now();
        BigDecimal costo = costoUnitario != null ? costoUnitario : producto.getCosto();

        Transaccion transaccion = new Transaccion(
            producto,
            fechaOperacion,
            tipo,
            cantidad,
            costo,
            nuevoBalance,
            observacion
        );

        return transaccionRepository.save(transaccion);
    }

    /**
     * Método de conveniencia para registrar operaciones desde el DTO.
     */
    @Transactional
    public Transaccion registrarOperacion(TransaccionDTO dto) {
        if (dto.getTipo().isEntrada()) {
            return registrarEntrada(
                dto.getProductoId(),
                dto.getTipo(),
                dto.getCantidad(),
                dto.getCostoUnitario(),
                dto.getObservacion(),
                dto.getFecha()
            );
        } else {
            return registrarSalida(
                dto.getProductoId(),
                dto.getTipo(),
                dto.getCantidad(),
                dto.getCostoUnitario(),
                dto.getObservacion(),
                dto.getFecha()
            );
        }
    }

    public List<Transaccion> findAllMovimientos() {
        return transaccionRepository.findAllWithProductoOrderByFechaDesc();
    }

    public List<Transaccion> findUltimosMovimientos(int limit) {
        return transaccionRepository.findRecentWithProducto(PageRequest.of(0, limit));
    }

    public List<Transaccion> findMovimientosPorProducto(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));
        return transaccionRepository.findByProductoOrderByFechaAscIdAsc(producto);
    }

    public List<Transaccion> findMovimientosConFiltros(Long productoId, TipoTransaccion tipo,
                                                      LocalDateTime inicio, LocalDateTime fin) {
        return transaccionRepository.findWithFilters(productoId, tipo, inicio, fin);
    }

    public long countMovimientosHoy() {
        LocalDateTime inicio = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fin = inicio.plusDays(1).minusNanos(1);
        return transaccionRepository.countByFechaBetween(inicio, fin);
    }

    /**
     * Reconstruye y audita la existencia matemática recalculando todas las transacciones históricas
     * Balance = Sum(Entradas) - Sum(Salidas)
     */
    public int reconstruirBalance(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        List<Transaccion> historico = transaccionRepository.findByProductoOrderByFechaAscIdAsc(producto);

        int balanceCalculado = 0;
        for (Transaccion t : historico) {
            if (t.isEntrada()) {
                balanceCalculado += t.getCantidad();
            } else if (t.isSalida()) {
                balanceCalculado -= t.getCantidad();
            }
        }
        return balanceCalculado;
    }

    public Transaccion findTransaccionById(Long id) {
        return transaccionRepository.findByIdWithProducto(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada con ID: " + id));
    }
}
