package com.ropastore.service;

import com.ropastore.entity.Producto;
import com.ropastore.entity.Transaccion;
import com.ropastore.enums.TipoTransaccion;
import com.ropastore.repository.ProductoRepository;
import com.ropastore.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String SEPARADOR = "========================================================================================================\n";
    private static final String SUB_SEPARADOR = "--------------------------------------------------------------------------------------------------------\n";

    private final ProductoRepository productoRepository;
    private final TransaccionRepository transaccionRepository;

    public ReportService(ProductoRepository productoRepository, TransaccionRepository transaccionRepository) {
        this.productoRepository = productoRepository;
        this.transaccionRepository = transaccionRepository;
    }

    /**
     * Genera el contenido del reporte de existencias en formato de texto plano (TXT)
     * implementando el algoritmo clásico de ruptura de control por artículo.
     */
    public String generarReporteExistenciasTexto() {
        StringBuilder sb = new StringBuilder();

        // 1. Encabezado institucional
        sb.append(SEPARADOR);
        sb.append("                                 ROPASTORE — SISTEMA DE INVENTARIO                                      \n");
        sb.append("                         REPORTE DE EXISTENCIAS Y MOVIMIENTOS POR ARTÍCULO                              \n");
        sb.append(SEPARADOR);
        sb.append(String.format("Fecha de emisión : %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        sb.append("Criterio         : Ruptura de control por artículo con histórico de transacciones\n");
        sb.append(SEPARADOR).append("\n");

        // 2. Obtener todos los productos y todas las transacciones ordenadas
        List<Producto> productos = productoRepository.findAllByOrderByCodigoAsc();
        List<Transaccion> todasTransacciones = transaccionRepository.findAllForReporteTxt();

        // Agrupar transacciones por ID de producto manteniendo el orden cronológico
        Map<Long, List<Transaccion>> transaccionesPorProducto = new LinkedHashMap<>();
        for (Producto p : productos) {
            transaccionesPorProducto.put(p.getId(), new ArrayList<>());
        }
        for (Transaccion t : todasTransacciones) {
            Long prodId = t.getProducto().getId();
            if (transaccionesPorProducto.containsKey(prodId)) {
                transaccionesPorProducto.get(prodId).add(t);
            }
        }

        int totalArticulos = productos.size();
        int totalUnidadesGlobal = 0;

        // 3. Iteración con ruptura de control por artículo
        for (Producto producto : productos) {
            sb.append(SUB_SEPARADOR);
            sb.append(String.format("ARTÍCULO: [%s] %s\n", producto.getCodigo(), producto.getDescripcion()));
            sb.append(String.format("Categoría: %-15s | Marca: %-15s | Talla: %-8s | Color: %-12s | Estado: %s\n",
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : "N/A",
                producto.getMarca() != null ? producto.getMarca() : "N/A",
                producto.getTalla() != null ? producto.getTalla().getLabel() : "N/A",
                producto.getColor() != null ? producto.getColor() : "N/A",
                producto.getEstado().getDescripcion()
            ));
            sb.append(SUB_SEPARADOR);
            sb.append(String.format("%-18s | %-28s | %10s | %16s | %10s\n",
                "Fecha", "Tipo Transacción", "Cantidad", "Costo Unit. ($)", "Balance"));
            sb.append(SUB_SEPARADOR);

            List<Transaccion> movimientos = transaccionesPorProducto.getOrDefault(producto.getId(), Collections.emptyList());

            int balanceAcumulado = 0;
            if (movimientos.isEmpty()) {
                sb.append("   (Sin movimientos registrados para este artículo)\n");
            } else {
                for (Transaccion m : movimientos) {
                    balanceAcumulado = m.getBalanceResultante();
                    String signo = m.isEntrada() ? "+" : "-";
                    String cantidadFormateada = String.format("%s%d", signo, m.getCantidad());

                    sb.append(String.format("%-18s | %-28s | %10s | %16.2f | %10d\n",
                        m.getFecha().format(DATE_FORMATTER),
                        m.getTipo().getDescripcion(),
                        cantidadFormateada,
                        m.getCostoUnitario(),
                        m.getBalanceResultante()
                    ));
                }
            }

            int existenciaFinal = (producto.getStockActual() != null) ? producto.getStockActual() : balanceAcumulado;
            totalUnidadesGlobal += existenciaFinal;

            sb.append(SUB_SEPARADOR);
            sb.append(String.format(">>> EXISTENCIA FINAL DEL ARTÍCULO [%s]: %d UNIDADES", producto.getCodigo(), existenciaFinal));
            if (producto.isBajoStock()) {
                sb.append(String.format("  [ALERTA: STOCK BAJO <= %d]", producto.getStockMinimo()));
            }
            sb.append("\n\n");
        }

        // 4. Pie de reporte con resumen global
        sb.append(SEPARADOR);
        sb.append("                                              RESUMEN GLOBAL                                            \n");
        sb.append(SEPARADOR);
        sb.append(String.format("Total de artículos catalogados : %d\n", totalArticulos));
        sb.append(String.format("Total de unidades en inventario: %d unidades físicas\n", totalUnidadesGlobal));
        sb.append(SEPARADOR);
        sb.append("                                          FIN DEL REPORTE OFICIAL                                       \n");
        sb.append(SEPARADOR);

        return sb.toString();
    }

    /**
     * Retorna los bytes del reporte de existencias codificados en UTF-8 listos para descargar por HTTP.
     */
    public byte[] generarReporteExistenciasBytes() {
        return generarReporteExistenciasTexto().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Genera el comprobante oficial en formato TXT para una transacción individual.
     */
    public String generarComprobanteTransaccionTexto(Transaccion t) {
        StringBuilder sb = new StringBuilder();
        Producto p = t.getProducto();
        int stockPrevio = t.isEntrada()
            ? (t.getBalanceResultante() - t.getCantidad())
            : (t.getBalanceResultante() + t.getCantidad());

        sb.append("================================================================================\n");
        sb.append("                       ROPASTORE — SISTEMA DE INVENTARIO                        \n");
        sb.append("                     COMPROBANTE DE MOVIMIENTO DE ALMACÉN                       \n");
        sb.append("================================================================================\n");
        sb.append(String.format("FOLIO OPERACIÓN : TRX-%06d\n", t.getId()));
        sb.append(String.format("FECHA / HORA    : %s\n", t.getFecha().format(DATE_FORMATTER)));
        sb.append(String.format("TIPO MOVIMIENTO : %s (%s)\n",
            t.isEntrada() ? "ENTRADA" : "SALIDA",
            t.getTipo().getDescripcion().toUpperCase()));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("DATOS DEL ARTÍCULO\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("Código          : %s\n", p.getCodigo()));
        sb.append(String.format("Descripción     : %s\n", p.getDescripcion()));
        sb.append(String.format("Categoría       : %s\n", p.getCategoria() != null ? p.getCategoria().getNombre() : "N/A"));
        sb.append(String.format("Marca           : %s\n", p.getMarca() != null ? p.getMarca() : "N/A"));
        sb.append(String.format("Talla           : %s\n", p.getTalla() != null ? p.getTalla().getLabel() : "N/A"));
        sb.append(String.format("Color           : %s\n", p.getColor() != null ? p.getColor() : "N/A"));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("DETALLE DE LA OPERACIÓN\n");
        sb.append("--------------------------------------------------------------------------------\n");
        String signo = t.isEntrada() ? "+" : "-";
        sb.append(String.format("Cantidad Movida : %s%d unidades\n", signo, t.getCantidad()));
        sb.append(String.format("Costo Unitario  : $ %,.2f\n", t.getCostoUnitario()));
        sb.append(String.format("Total Operación : $ %,.2f\n", t.getTotalOperacion()));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("BALANCE Y AUDITORÍA DE INVENTARIO\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("Stock Previo    : %d unidades\n", stockPrevio));
        sb.append(String.format("Impacto Neto    : %s%d unidades\n", signo, t.getCantidad()));
        sb.append(String.format("Balance Final   : %d unidades\n", t.getBalanceResultante()));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("OBSERVACIONES\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(t.getObservacion() != null && !t.getObservacion().isBlank()
            ? t.getObservacion() + "\n"
            : "(Sin observaciones registradas)\n");
        sb.append("================================================================================\n");
        sb.append("FIRMAS DE CONFORMIDAD:\n\n\n");
        sb.append("____________________________               ____________________________\n");
        sb.append("      RESPONSABLE ALMACÉN                        SUPERVISOR DE ÁREA     \n");
        sb.append("================================================================================\n");
        sb.append("                               FIN DEL COMPROBANTE                              \n");
        sb.append("================================================================================\n");

        return sb.toString();
    }

    public byte[] generarComprobanteTransaccionBytes(Transaccion t) {
        return generarComprobanteTransaccionTexto(t).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Genera el reporte de movimientos en formato TXT aplicando los filtros seleccionados
     * por el usuario y manteniendo el principio de ruptura de control por artículo.
     */
    public String generarReporteMovimientosFiltradosTexto(
        Long productoId,
        TipoTransaccion tipo,
        LocalDateTime inicio,
        LocalDateTime fin,
        LocalDate filtroFechaInicio,
        LocalDate filtroFechaFin
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append(SEPARADOR);
        sb.append("                                 ROPASTORE — SISTEMA DE INVENTARIO                                      \n");
        sb.append("                        REPORTE DE MOVIMIENTOS HISTÓRICOS (FILTRO APLICADO)                             \n");
        sb.append(SEPARADOR);
        sb.append(String.format("Fecha de emisión : %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));

        // Describir los filtros aplicados
        sb.append("Criterios de filtro aplicados:\n");
        if (productoId != null) {
            Producto prod = productoRepository.findById(productoId).orElse(null);
            if (prod != null) {
                sb.append(String.format(" • Artículo       : [%s] %s\n", prod.getCodigo(), prod.getDescripcion()));
            } else {
                sb.append(String.format(" • Artículo ID    : %d\n", productoId));
            }
        } else {
            sb.append(" • Artículo       : Todos los artículos\n");
        }

        if (tipo != null) {
            sb.append(String.format(" • Tipo Operación : %s (%s)\n", tipo.getDescripcion(), tipo.isEntrada() ? "ENTRADA" : "SALIDA"));
        } else {
            sb.append(" • Tipo Operación : Todos los tipos\n");
        }

        if (filtroFechaInicio != null || filtroFechaFin != null) {
            String fIni = filtroFechaInicio != null ? filtroFechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Inicio histórico";
            String fFin = filtroFechaFin != null ? filtroFechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Hoy";
            sb.append(String.format(" • Rango Fechas   : Desde %s hasta %s\n", fIni, fFin));
        } else {
            sb.append(" • Rango Fechas   : Histórico completo sin restricción de fecha\n");
        }
        sb.append(SEPARADOR).append("\n");

        List<Transaccion> transaccionesFiltradas = transaccionRepository.findWithFiltersForReporteTxt(productoId, tipo, inicio, fin);

        if (transaccionesFiltradas.isEmpty()) {
            sb.append(SUB_SEPARADOR);
            sb.append("   (No se encontraron movimientos registrados que coincidan con los filtros seleccionados)\n");
            sb.append(SUB_SEPARADOR).append("\n");
        } else {
            // Agrupar por producto preservando orden por código de producto
            Map<Long, List<Transaccion>> transaccionesPorProducto = new LinkedHashMap<>();
            Map<Long, Producto> productosMap = new LinkedHashMap<>();

            for (Transaccion t : transaccionesFiltradas) {
                Long pId = t.getProducto().getId();
                productosMap.putIfAbsent(pId, t.getProducto());
                transaccionesPorProducto.computeIfAbsent(pId, k -> new ArrayList<>()).add(t);
            }

            int totalUnidadesEntrada = 0;
            BigDecimal totalMontoEntrada = BigDecimal.ZERO;
            int totalUnidadesSalida = 0;
            BigDecimal totalMontoSalida = BigDecimal.ZERO;

            for (Map.Entry<Long, List<Transaccion>> entry : transaccionesPorProducto.entrySet()) {
                Producto prod = productosMap.get(entry.getKey());
                List<Transaccion> movimientos = entry.getValue();

                sb.append(SUB_SEPARADOR);
                sb.append(String.format("ARTÍCULO: [%s] %s\n", prod.getCodigo(), prod.getDescripcion()));
                sb.append(String.format("Categoría: %-15s | Marca: %-15s | Talla: %-8s | Color: %-12s | Stock Actual: %d\n",
                    prod.getCategoria() != null ? prod.getCategoria().getNombre() : "N/A",
                    prod.getMarca() != null ? prod.getMarca() : "N/A",
                    prod.getTalla() != null ? prod.getTalla().getLabel() : "N/A",
                    prod.getColor() != null ? prod.getColor() : "N/A",
                    prod.getStockActual() != null ? prod.getStockActual() : 0
                ));
                sb.append(SUB_SEPARADOR);
                sb.append(String.format("%-18s | %-12s | %-24s | %10s | %14s | %14s | %9s\n",
                    "Fecha", "Folio", "Tipo Operación", "Cantidad", "Costo Unit. ($)", "Total ($)", "Balance"));
                sb.append(SUB_SEPARADOR);

                int subtotalEntradasArticulo = 0;
                BigDecimal subtotalMontoEntradaArticulo = BigDecimal.ZERO;
                int subtotalSalidasArticulo = 0;
                BigDecimal subtotalMontoSalidaArticulo = BigDecimal.ZERO;

                for (Transaccion m : movimientos) {
                    String signo = m.isEntrada() ? "+" : "-";
                    String cantidadFormateada = String.format("%s%d", signo, m.getCantidad());
                    String folio = String.format("TRX-%06d", m.getId());

                    if (m.isEntrada()) {
                        subtotalEntradasArticulo += m.getCantidad();
                        subtotalMontoEntradaArticulo = subtotalMontoEntradaArticulo.add(m.getTotalOperacion());
                    } else {
                        subtotalSalidasArticulo += m.getCantidad();
                        subtotalMontoSalidaArticulo = subtotalMontoSalidaArticulo.add(m.getTotalOperacion());
                    }

                    sb.append(String.format("%-18s | %-12s | %-24s | %10s | %14.2f | %14.2f | %9d\n",
                        m.getFecha().format(DATE_FORMATTER),
                        folio,
                        m.getTipo().getDescripcion(),
                        cantidadFormateada,
                        m.getCostoUnitario(),
                        m.getTotalOperacion(),
                        m.getBalanceResultante()
                    ));
                }

                totalUnidadesEntrada += subtotalEntradasArticulo;
                totalMontoEntrada = totalMontoEntrada.add(subtotalMontoEntradaArticulo);
                totalUnidadesSalida += subtotalSalidasArticulo;
                totalMontoSalida = totalMontoSalida.add(subtotalMontoSalidaArticulo);

                sb.append(SUB_SEPARADOR);
                sb.append(String.format(">>> SUBTOTAL ARTÍCULO [%s]: %d transacciones | Entradas: +%d ($ %,.2f) | Salidas: -%d ($ %,.2f)\n\n",
                    prod.getCodigo(),
                    movimientos.size(),
                    subtotalEntradasArticulo,
                    subtotalMontoEntradaArticulo,
                    subtotalSalidasArticulo,
                    subtotalMontoSalidaArticulo
                ));
            }

            // Resumen global del filtro
            sb.append(SEPARADOR);
            sb.append("                                        RESUMEN DEL REPORTE FILTRADO                                    \n");
            sb.append(SEPARADOR);
            sb.append(String.format("Total de movimientos en este reporte : %d registros\n", transaccionesFiltradas.size()));
            sb.append(String.format("Total unidades ingresadas (+)         : %d unidades  ($ %,.2f)\n", totalUnidadesEntrada, totalMontoEntrada));
            sb.append(String.format("Total unidades egresadas (-)          : %d unidades  ($ %,.2f)\n", totalUnidadesSalida, totalMontoSalida));
            int flujoNeto = totalUnidadesEntrada - totalUnidadesSalida;
            sb.append(String.format("Flujo neto en unidades del período    : %s%d unidades\n", flujoNeto >= 0 ? "+" : "", flujoNeto));
            sb.append(SEPARADOR);
            sb.append("                                            FIN DEL REPORTE OFICIAL                                     \n");
            sb.append(SEPARADOR);
        }

        return sb.toString();
    }

    public byte[] generarReporteMovimientosFiltradosBytes(
        Long productoId,
        TipoTransaccion tipo,
        LocalDateTime inicio,
        LocalDateTime fin,
        LocalDate filtroFechaInicio,
        LocalDate filtroFechaFin
    ) {
        return generarReporteMovimientosFiltradosTexto(productoId, tipo, inicio, fin, filtroFechaInicio, filtroFechaFin).getBytes(StandardCharsets.UTF_8);
    }
}
