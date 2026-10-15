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
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/reportes")
public class ReportController {

    private final ReportService reportService;
    private final InventoryService inventoryService;

    public ReportController(ReportService reportService, InventoryService inventoryService) {
        this.reportService = reportService;
        this.inventoryService = inventoryService;
    }

    /**
     * Vista principal de reportes TXT con soporte de filtrado por rango de fechas.
     */
    @GetMapping
    public String index(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {
        String contenidoReporte = reportService.generarReporteExistenciasTexto(fechaInicio, fechaFin);
        String defaultFilename = reportService.generarNombreArchivoExistencias(fechaInicio, fechaFin);

        model.addAttribute("reporteTexto", contenidoReporte);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("defaultFilename", defaultFilename);
        model.addAttribute("activeNav", "reportes");
        return "reportes/index";
    }

    /**
     * Endpoint API para previsualizar el reporte de existencias antes de descargarlo.
     */
    @GetMapping("/existencias/preview")
    @ResponseBody
    public Map<String, String> previewExistencias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        String contenido = reportService.generarReporteExistenciasTexto(fechaInicio, fechaFin);
        String filename = reportService.generarNombreArchivoExistencias(fechaInicio, fechaFin);
        String titulo = (fechaInicio != null || fechaFin != null)
                ? "Reporte de Existencias (Período Filtrado)"
                : "Reporte General de Existencias";
        return Map.of("titulo", titulo, "contenido", contenido, "defaultFilename", filename);
    }

    /**
     * Descarga el reporte de existencias con soporte de filtrado y nombre personalizado.
     */
    @GetMapping("/existencias/descargar")
    public ResponseEntity<byte[]> descargarReporteTxt(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String filename) {
        byte[] archivoBytes = reportService.generarReporteExistenciasBytes(fechaInicio, fechaFin);
        String defaultName = reportService.generarNombreArchivoExistencias(fechaInicio, fechaFin);
        String finalFilename = ReportService.sanitizarNombreArchivo(filename, defaultName);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + finalFilename + "\"")
            .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
            .contentLength(archivoBytes.length)
            .body(archivoBytes);
    }

    /**
     * Endpoint API para previsualizar el comprobante individual de una transacción.
     */
    @GetMapping("/transacciones/{id}/preview")
    @ResponseBody
    public Map<String, String> previewComprobanteTransaccion(@PathVariable Long id) {
        Transaccion t = inventoryService.findTransaccionById(id);
        String contenido = reportService.generarComprobanteTransaccionTexto(t);
        String filename = reportService.generarNombreArchivoComprobante(t);
        String titulo = String.format("Comprobante de Operación TRX-%06d", t.getId());
        return Map.of("titulo", titulo, "contenido", contenido, "defaultFilename", filename);
    }

    /**
     * Descarga el comprobante TXT de una transacción individual con soporte de nombre personalizado.
     */
    @GetMapping("/transacciones/{id}/descargar")
    public ResponseEntity<byte[]> descargarComprobanteTransaccion(
            @PathVariable Long id,
            @RequestParam(required = false) String filename) {
        Transaccion transaccion = inventoryService.findTransaccionById(id);
        byte[] archivoBytes = reportService.generarComprobanteTransaccionBytes(transaccion);
        String defaultName = reportService.generarNombreArchivoComprobante(transaccion);
        String finalFilename = ReportService.sanitizarNombreArchivo(filename, defaultName);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + finalFilename + "\"")
            .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
            .contentLength(archivoBytes.length)
            .body(archivoBytes);
    }

    /**
     * Endpoint API para previsualizar el reporte de movimientos filtrados.
     */
    @GetMapping("/movimientos/preview")
    @ResponseBody
    public Map<String, String> previewMovimientosFiltrados(
            @RequestParam(name = "productoId", required = false) Long productoId,
            @RequestParam(name = "tipo", required = false) TipoTransaccion tipo,
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.plusDays(1).atStartOfDay().minusNanos(1) : null;
        String contenido = reportService.generarReporteMovimientosFiltradosTexto(productoId, tipo, inicio, fin, fechaInicio, fechaFin);
        String filename = reportService.generarNombreArchivoMovimientos(productoId, tipo, fechaInicio, fechaFin);
        boolean tieneFiltro = (productoId != null || tipo != null || fechaInicio != null || fechaFin != null);
        String titulo = tieneFiltro ? "Reporte de Movimientos (Filtrado)" : "Reporte de Movimientos (Histórico)";
        return Map.of("titulo", titulo, "contenido", contenido, "defaultFilename", filename);
    }

    /**
     * Descarga el reporte TXT de movimientos aplicando los filtros actuales y nombre personalizado.
     */
    @GetMapping("/movimientos/descargar")
    public ResponseEntity<byte[]> descargarMovimientosFiltradosTxt(
            @RequestParam(name = "productoId", required = false) Long productoId,
            @RequestParam(name = "tipo", required = false) TipoTransaccion tipo,
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "filename", required = false) String filename) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.plusDays(1).atStartOfDay().minusNanos(1) : null;

        byte[] archivoBytes = reportService.generarReporteMovimientosFiltradosBytes(productoId, tipo, inicio, fin, fechaInicio, fechaFin);
        String defaultName = reportService.generarNombreArchivoMovimientos(productoId, tipo, fechaInicio, fechaFin);
        String finalFilename = ReportService.sanitizarNombreArchivo(filename, defaultName);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + finalFilename + "\"")
            .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
            .contentLength(archivoBytes.length)
            .body(archivoBytes);
    }
}
