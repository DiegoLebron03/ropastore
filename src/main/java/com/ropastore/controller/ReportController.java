package com.ropastore.controller;

import com.ropastore.entity.Transaccion;
import com.ropastore.enums.TipoTransaccion;
import com.ropastore.service.InventoryService;
import com.ropastore.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/reportes")
public class ReportController {

    private final ReportService reportService;
    private final InventoryService inventoryService;

    public ReportController(ReportService reportService, InventoryService inventoryService) {
        this.reportService = reportService;
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String index(Model model) {
        String contenidoReporte = reportService.generarReporteExistenciasTexto();
        model.addAttribute("reporteTexto", contenidoReporte);
        model.addAttribute("activeNav", "reportes");
        return "reportes/index";
    }

    @GetMapping("/existencias/descargar")
    public ResponseEntity<byte[]> descargarReporteTxt() {
        byte[] archivoBytes = reportService.generarReporteExistenciasBytes();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String filename = "reporte_existencias_ropastore_" + timestamp + ".txt";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
            .contentLength(archivoBytes.length)
            .body(archivoBytes);
    }

    /**
     * Descarga el comprobante TXT de una transacción individual (entrada o salida específica).
     */
    @GetMapping("/transacciones/{id}/descargar")
    public ResponseEntity<byte[]> descargarComprobanteTransaccion(@PathVariable Long id) {
        Transaccion transaccion = inventoryService.findTransaccionById(id);
        byte[] archivoBytes = reportService.generarComprobanteTransaccionBytes(transaccion);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String filename = String.format("comprobante_TRX%06d_%s.txt", transaccion.getId(), timestamp);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
            .contentLength(archivoBytes.length)
            .body(archivoBytes);
    }

    /**
     * Descarga el reporte TXT de movimientos aplicando los filtros actuales de la consulta.
     */
    @GetMapping("/movimientos/descargar")
    public ResponseEntity<byte[]> descargarMovimientosFiltradosTxt(
        @RequestParam(name = "productoId", required = false) Long productoId,
        @RequestParam(name = "tipo", required = false) TipoTransaccion tipo,
        @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
        @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.plusDays(1).atStartOfDay().minusNanos(1) : null;

        byte[] archivoBytes = reportService.generarReporteMovimientosFiltradosBytes(productoId, tipo, inicio, fin, fechaInicio, fechaFin);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        boolean tieneFiltro = (productoId != null || tipo != null || fechaInicio != null || fechaFin != null);
        String prefix = tieneFiltro ? "reporte_movimientos_filtrados_" : "reporte_movimientos_historico_";
        String filename = prefix + timestamp + ".txt";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
            .contentLength(archivoBytes.length)
            .body(archivoBytes);
    }
}
