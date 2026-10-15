package com.ropastore.service;

import com.ropastore.dto.AnalyticsDTO;
import com.ropastore.entity.Producto;
import com.ropastore.entity.Transaccion;
import com.ropastore.enums.CategoriaProducto;
import com.ropastore.enums.EstadoProducto;
import com.ropastore.enums.TipoTransaccion;
import com.ropastore.repository.ProductoRepository;
import com.ropastore.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ProductoRepository productoRepository;
    private final TransaccionRepository transaccionRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");

    public AnalyticsService(ProductoRepository productoRepository, TransaccionRepository transaccionRepository) {
        this.productoRepository = productoRepository;
        this.transaccionRepository = transaccionRepository;
    }

    public AnalyticsDTO obtenerDatosAnaliticos(LocalDate fechaInicio, LocalDate fechaFin) {
        AnalyticsDTO dto = new AnalyticsDTO();
        dto.setFechaInicio(fechaInicio);
        dto.setFechaFin(fechaFin);

        LocalDateTime inicioDT = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime finDT = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : null;

        // 1. Obtener todas las transacciones del período ordenadas cronológicamente
        List<Transaccion> transacciones = transaccionRepository.findWithFilters(null, null, inicioDT, finDT);
        // Invertir a orden cronológico ascendente para gráficos de tiempo
        List<Transaccion> transaccionesCronologicas = new ArrayList<>(transacciones);
        transaccionesCronologicas.sort(Comparator.comparing(Transaccion::getFecha).thenComparing(Transaccion::getId));

        // 2. Obtener productos activos
        List<Producto> productos = productoRepository.findAllByOrderByCodigoAsc();

        // 3. Totales y KPIs
        BigDecimal totalVentas = BigDecimal.ZERO;
        int unidadesVendidas = 0;
        BigDecimal totalCompras = BigDecimal.ZERO;
        int unidadesCompradas = 0;

        Map<Long, Integer> entradasPorProducto = new HashMap<>();
        Map<Long, Integer> salidasPorProducto = new HashMap<>();
        Map<TipoTransaccion, Integer> operacionesPorTipo = new LinkedHashMap<>();
        for (TipoTransaccion tipo : TipoTransaccion.values()) {
            operacionesPorTipo.put(tipo, 0);
        }

        // Estructuras para series temporales
        Map<LocalDate, Integer> entradasDia = new TreeMap<>();
        Map<LocalDate, Integer> salidasDia = new TreeMap<>();
        Map<LocalDate, BigDecimal> comprasMontoDia = new TreeMap<>();
        Map<LocalDate, BigDecimal> ventasMontoDia = new TreeMap<>();

        for (Transaccion t : transacciones) {
            int cant = t.getCantidad() != null ? t.getCantidad() : 0;
            BigDecimal costoUnit = t.getCostoUnitario() != null ? t.getCostoUnitario() : BigDecimal.ZERO;
            BigDecimal monto = costoUnit.multiply(BigDecimal.valueOf(cant));

            // Registro por tipo
            operacionesPorTipo.put(t.getTipo(), operacionesPorTipo.getOrDefault(t.getTipo(), 0) + cant);

            LocalDate dia = t.getFecha().toLocalDate();

            if (t.isEntrada()) {
                unidadesCompradas += cant;
                totalCompras = totalCompras.add(monto);
                entradasDia.put(dia, entradasDia.getOrDefault(dia, 0) + cant);
                comprasMontoDia.put(dia, comprasMontoDia.getOrDefault(dia, BigDecimal.ZERO).add(monto));

                if (t.getProducto() != null) {
                    Long pid = t.getProducto().getId();
                    entradasPorProducto.put(pid, entradasPorProducto.getOrDefault(pid, 0) + cant);
                }
            } else if (t.isSalida()) {
                unidadesVendidas += cant;
                totalVentas = totalVentas.add(monto);
                salidasDia.put(dia, salidasDia.getOrDefault(dia, 0) + cant);
                ventasMontoDia.put(dia, ventasMontoDia.getOrDefault(dia, BigDecimal.ZERO).add(monto));

                if (t.getProducto() != null) {
                    Long pid = t.getProducto().getId();
                    salidasPorProducto.put(pid, salidasPorProducto.getOrDefault(pid, 0) + cant);
                }
            }
        }

        dto.setTotalVentasValor(totalVentas);
        dto.setTotalVentasUnidades(unidadesVendidas);
        dto.setTotalComprasValor(totalCompras);
        dto.setTotalComprasUnidades(unidadesCompradas);
        dto.setTotalTransacciones(transacciones.size());
        dto.setValorInventarioActual(productoRepository.sumValorInventario());
        dto.setTotalProductos(productoRepository.countByEstado(EstadoProducto.ACTIVO));
        dto.setTotalBajoStock(productoRepository.countProductosBajoStock());

        // 4. Población de Gráficos Temporales (días ordenados)
        Set<LocalDate> todasLasFechas = new TreeSet<>();
        todasLasFechas.addAll(entradasDia.keySet());
        todasLasFechas.addAll(salidasDia.keySet());
        todasLasFechas.addAll(comprasMontoDia.keySet());
        todasLasFechas.addAll(ventasMontoDia.keySet());

        // Si hay pocas fechas, rellenar últimos días para una curva estética
        if (todasLasFechas.isEmpty()) {
            LocalDate hoy = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                todasLasFechas.add(hoy.minusDays(i));
            }
        }

        List<String> labelsFechas = new ArrayList<>();
        List<Integer> listEntradas = new ArrayList<>();
        List<Integer> listSalidas = new ArrayList<>();
        List<BigDecimal> listMontoCompras = new ArrayList<>();
        List<BigDecimal> listMontoVentas = new ArrayList<>();

        for (LocalDate d : todasLasFechas) {
            labelsFechas.add(d.format(DATE_FORMATTER));
            listEntradas.add(entradasDia.getOrDefault(d, 0));
            listSalidas.add(salidasDia.getOrDefault(d, 0));
            listMontoCompras.add(comprasMontoDia.getOrDefault(d, BigDecimal.ZERO));
            listMontoVentas.add(ventasMontoDia.getOrDefault(d, BigDecimal.ZERO));
        }

        dto.setLabelsFechas(labelsFechas);
        dto.setUnidadesEntrada(listEntradas);
        dto.setUnidadesSalida(listSalidas);
        dto.setMontoCompras(listMontoCompras);
        dto.setMontoVentas(listMontoVentas);

        // 5. Gráfico de Categorías: Stock y Valor
        Map<CategoriaProducto, Integer> stockPorCat = new LinkedHashMap<>();
        Map<CategoriaProducto, BigDecimal> valorPorCat = new LinkedHashMap<>();
        for (CategoriaProducto cat : CategoriaProducto.values()) {
            stockPorCat.put(cat, 0);
            valorPorCat.put(cat, BigDecimal.ZERO);
        }

        for (Producto p : productos) {
            if (p.isActivo() && p.getCategoria() != null) {
                int stock = p.getStockActual() != null ? p.getStockActual() : 0;
                BigDecimal costo = p.getCosto() != null ? p.getCosto() : BigDecimal.ZERO;
                BigDecimal val = costo.multiply(BigDecimal.valueOf(stock));

                stockPorCat.put(p.getCategoria(), stockPorCat.getOrDefault(p.getCategoria(), 0) + stock);
                valorPorCat.put(p.getCategoria(), valorPorCat.getOrDefault(p.getCategoria(), BigDecimal.ZERO).add(val));
            }
        }

        List<String> labelsCat = new ArrayList<>();
        List<Integer> stockCat = new ArrayList<>();
        List<BigDecimal> valorCat = new ArrayList<>();
        for (Map.Entry<CategoriaProducto, Integer> entry : stockPorCat.entrySet()) {
            labelsCat.add(entry.getKey().getNombre());
            stockCat.add(entry.getValue());
            valorCat.add(valorPorCat.getOrDefault(entry.getKey(), BigDecimal.ZERO));
        }
        dto.setLabelsCategorias(labelsCat);
        dto.setStockPorCategoria(stockCat);
        dto.setValorPorCategoria(valorCat);

        // 6. Top 5 Productos con más salidas/ventas
        List<Map.Entry<Long, Integer>> topSalidas = salidasPorProducto.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        Map<Long, Producto> productoMap = productos.stream()
                .collect(Collectors.toMap(Producto::getId, p -> p, (p1, p2) -> p1));

        List<String> labelsTop = new ArrayList<>();
        List<Integer> cantTop = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : topSalidas) {
            Producto prod = productoMap.get(entry.getKey());
            if (prod != null) {
                labelsTop.add(prod.getCodigo() + " - " + prod.getDescripcion());
                cantTop.add(entry.getValue());
            }
        }
        // Si no hay salidas suficientes, mostrar productos activos más representativos
        if (labelsTop.isEmpty()) {
            for (Producto p : productos.stream().limit(5).collect(Collectors.toList())) {
                labelsTop.add(p.getCodigo() + " - " + p.getDescripcion());
                cantTop.add(0);
            }
        }
        dto.setLabelsTopProductos(labelsTop);
        dto.setCantidadesTopProductos(cantTop);

        // 7. Gráfico Desglose por Tipo de Transacción
        List<String> labelsTipos = new ArrayList<>();
        List<Integer> cantTipos = new ArrayList<>();
        for (Map.Entry<TipoTransaccion, Integer> entry : operacionesPorTipo.entrySet()) {
            labelsTipos.add(entry.getKey().getDescripcion());
            cantTipos.add(entry.getValue());
        }
        dto.setLabelsTipos(labelsTipos);
        dto.setCantidadesPorTipo(cantTipos);

        // 8. Gráfico Stock Actual vs Stock Mínimo (Primeros 8 artículos para legibilidad)
        List<String> labelsStockVsMin = new ArrayList<>();
        List<Integer> stockActualVsMin = new ArrayList<>();
        List<Integer> stockMinimoVsMin = new ArrayList<>();
        for (Producto p : productos.stream().limit(8).collect(Collectors.toList())) {
            labelsStockVsMin.add(p.getCodigo());
            stockActualVsMin.add(p.getStockActual() != null ? p.getStockActual() : 0);
            stockMinimoVsMin.add(p.getStockMinimo() != null ? p.getStockMinimo() : 0);
        }
        dto.setLabelsStockVsMin(labelsStockVsMin);
        dto.setStockActualVsMin(stockActualVsMin);
        dto.setStockMinimoVsMin(stockMinimoVsMin);

        // 9. Tabla Resumen Detallado por Producto
        List<AnalyticsDTO.ItemResumenDTO> items = new ArrayList<>();
        for (Producto p : productos) {
            int stock = p.getStockActual() != null ? p.getStockActual() : 0;
            int stockMin = p.getStockMinimo() != null ? p.getStockMinimo() : 0;
            BigDecimal costo = p.getCosto() != null ? p.getCosto() : BigDecimal.ZERO;
            BigDecimal val = costo.multiply(BigDecimal.valueOf(stock));
            int entradas = entradasPorProducto.getOrDefault(p.getId(), 0);
            int salidas = salidasPorProducto.getOrDefault(p.getId(), 0);

            items.add(new AnalyticsDTO.ItemResumenDTO(
                    p.getCodigo(),
                    p.getDescripcion(),
                    p.getCategoria() != null ? p.getCategoria().getNombre() : "General",
                    stock,
                    stockMin,
                    entradas,
                    salidas,
                    costo,
                    val
            ));
        }
        dto.setItemsResumen(items);

        return dto;
    }
}

