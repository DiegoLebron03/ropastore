package com.ropastore.service;

import com.ropastore.entity.Producto;
import com.ropastore.entity.Transaccion;
import com.ropastore.enums.CategoriaProducto;
import com.ropastore.enums.TipoTransaccion;
import com.ropastore.repository.ProductoRepository;
import com.ropastore.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    private Producto productoA;
    private Producto productoB;

    @BeforeEach
    void setUp() {
        productoA = new Producto(
            "AAA-001",
            "Camisa Algodon Pima",
            new BigDecimal("600.00"),
            new BigDecimal("1200.00"),
            CategoriaProducto.CAMISAS,
            "Marca A",
            null,
            "Blanco",
            5
        );
        productoA = productoRepository.save(productoA);

        productoB = new Producto(
            "BBB-002",
            "Pantalon Jean Regular",
            new BigDecimal("800.00"),
            new BigDecimal("1500.00"),
            CategoriaProducto.PANTALONES,
            "Marca B",
            null,
            "Azul",
            5
        );
        productoB = productoRepository.save(productoB);
    }

    @Test
    @DisplayName("Debe generar reporte de existencias histórico completo con algoritmo de corte de control")
    void testGenerarReporteExistenciasTextoHistorico() {
        inventoryService.registrarEntrada(productoA.getId(), TipoTransaccion.COMPRA, 20, new BigDecimal("600.00"), "Lote 1", LocalDateTime.now().minusDays(3));
        inventoryService.registrarSalida(productoA.getId(), TipoTransaccion.VENTA, 5, new BigDecimal("600.00"), "Venta 1", LocalDateTime.now().minusDays(1));

        String reporte = reportService.generarReporteExistenciasTexto();

        assertNotNull(reporte);
        assertTrue(reporte.contains("ROPASTORE — SISTEMA DE INVENTARIO"));
        assertTrue(reporte.contains("REPORTE DE EXISTENCIAS Y MOVIMIENTOS POR ARTÍCULO"));
        assertTrue(reporte.contains("Período          : Histórico completo"));
        assertTrue(reporte.contains("Ruptura de control por artículo"));
        assertTrue(reporte.contains("ARTÍCULO: [AAA-001] Camisa Algodon Pima"));
        assertTrue(reporte.contains(">>> EXISTENCIA FINAL DEL ARTÍCULO [AAA-001]: 15 UNIDADES"));
        assertTrue(reporte.contains("RESUMEN GLOBAL"));
    }

    @Test
    @DisplayName("Debe filtrar reporte de existencias por rango de fechas")
    void testGenerarReporteExistenciasTextoConRangoFechas() {
        LocalDate ayer = LocalDate.now().minusDays(1);
        LocalDate hoy = LocalDate.now();

        // Transacción antes del período (hace 10 días)
        inventoryService.registrarEntrada(productoA.getId(), TipoTransaccion.COMPRA, 30, new BigDecimal("600.00"), "Lote Antiguo", LocalDateTime.now().minusDays(10));

        // Transacción dentro del período (ayer)
        inventoryService.registrarSalida(productoA.getId(), TipoTransaccion.VENTA, 10, new BigDecimal("600.00"), "Venta de ayer", ayer.atTime(14, 0));

        String reporteFiltrado = reportService.generarReporteExistenciasTexto(ayer, hoy);

        assertNotNull(reporteFiltrado);
        String fAyer = ayer.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String fHoy = hoy.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        assertTrue(reporteFiltrado.contains("Período          : Desde " + fAyer + " hasta " + fHoy));
        // Debe contener la transacción de ayer
        assertTrue(reporteFiltrado.contains("Salida por Venta"));
        // El resumen final y el artículo deben estar presentes
        assertTrue(reporteFiltrado.contains("AAA-001"));
        assertTrue(reporteFiltrado.contains("RESUMEN GLOBAL"));
    }

    @Test
    @DisplayName("Debe generar comprobante individual TXT con datos oficiales y firmas")
    void testGenerarReporteTransaccionTexto() {
        Transaccion t = inventoryService.registrarEntrada(
            productoA.getId(),
            TipoTransaccion.COMPRA,
            50,
            new BigDecimal("600.00"),
            "Compra mayorista proveedor textil",
            LocalDateTime.now()
        );

        String voucher = reportService.generarComprobanteTransaccionTexto(t.getId());

        assertNotNull(voucher);
        assertTrue(voucher.contains("ROPASTORE — SISTEMA DE INVENTARIO"));
        assertTrue(voucher.contains("COMPROBANTE DE MOVIMIENTO DE ALMACÉN"));
        assertTrue(voucher.contains("FOLIO OPERACIÓN : TRX-" + String.format("%06d", t.getId())));
        assertTrue(voucher.contains("AAA-001"));
        assertTrue(voucher.contains("Camisa Algodon Pima"));
        assertTrue(voucher.contains("+50 unidades"));
        assertTrue(voucher.contains("RESPONSABLE ALMACÉN"));
        assertTrue(voucher.contains("SUPERVISOR DE ÁREA"));
    }

    @Test
    @DisplayName("Debe generar reporte general de movimientos con formato plano")
    void testGenerarReporteMovimientosTexto() {
        inventoryService.registrarEntrada(productoA.getId(), TipoTransaccion.COMPRA, 10, new BigDecimal("600.00"), "Entrada", LocalDateTime.now());
        inventoryService.registrarEntrada(productoB.getId(), TipoTransaccion.COMPRA, 15, new BigDecimal("800.00"), "Entrada B", LocalDateTime.now());

        String auditReport = reportService.generarReporteMovimientosFiltradosTexto(null, null, null, null, null, null);

        assertNotNull(auditReport);
        assertTrue(auditReport.contains("ROPASTORE — SISTEMA DE INVENTARIO"));
        assertTrue(auditReport.contains("REPORTE DE MOVIMIENTOS"));
        assertTrue(auditReport.contains("AAA-001"));
        assertTrue(auditReport.contains("BBB-002"));
    }

    @Test
    @DisplayName("Debe generar nomenclaturas automáticas coherentes y sanitizar nombres de archivo")
    void testNomenclaturasYSanitizacion() {
        // Existencias histórico
        String nombreHistorico = reportService.generarNombreArchivoExistencias(null, null);
        assertTrue(nombreHistorico.startsWith("reporte_existencias_ropastore_"));
        assertTrue(nombreHistorico.endsWith(".txt"));

        // Existencias con rango de fechas
        LocalDate d1 = LocalDate.of(2026, 1, 1);
        LocalDate d2 = LocalDate.of(2026, 1, 31);
        String nombreRango = reportService.generarNombreArchivoExistencias(d1, d2);
        assertEquals("reporte_existencias_2026-01-01_al_2026-01-31.txt", nombreRango);

        // Comprobante
        Transaccion t = inventoryService.registrarEntrada(
            productoA.getId(),
            TipoTransaccion.COMPRA,
            10,
            new BigDecimal("600.00"),
            "Test",
            LocalDateTime.of(2026, 5, 10, 10, 30)
        );
        String nombreComprobante = reportService.generarNombreArchivoComprobante(t);
        assertTrue(nombreComprobante.contains(String.format("comprobante_TRX%06d_COMPRA_AAA-001_", t.getId())));
        assertTrue(nombreComprobante.endsWith(".txt"));

        // Sanitización
        assertEquals("reporte_personalizado.txt", reportService.sanitizarNombreArchivo("reporte_personalizado", "fallback.txt"));
        assertEquals("reporte_personalizado.txt", reportService.sanitizarNombreArchivo("reporte_personalizado.txt", "fallback.txt"));
        assertEquals("mi_reporte_123_.txt", reportService.sanitizarNombreArchivo("mi reporte/123?.txt", "fallback.txt"));
        assertEquals("fallback.txt", reportService.sanitizarNombreArchivo("", "fallback.txt"));
        assertEquals("fallback.txt", reportService.sanitizarNombreArchivo(null, "fallback.txt"));
    }
}
