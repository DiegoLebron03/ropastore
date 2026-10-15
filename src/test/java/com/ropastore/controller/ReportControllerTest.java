package com.ropastore.controller;

import com.ropastore.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, new Usuario("Admin", "1234", "Administrador", "ADMIN"));
    }

    @Test
    @DisplayName("Debe cargar la vista de reportes con previsualización")
    void testVerReportesVista() throws Exception {
        mockMvc.perform(get("/reportes").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("reportes/index"))
            .andExpect(model().attributeExists("reporteTexto"))
            .andExpect(model().attributeExists("defaultFilename"))
            .andExpect(content().string(containsString("REPORTE DE EXISTENCIAS")));
    }

    @Test
    @DisplayName("Debe cargar la vista de reportes aplicando filtros de fecha")
    void testVerReportesVistaConFiltroFechas() throws Exception {
        mockMvc.perform(get("/reportes")
                .param("fechaInicio", "2026-01-01")
                .param("fechaFin", "2026-12-31")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("reportes/index"))
            .andExpect(model().attribute("fechaInicio", java.time.LocalDate.of(2026, 1, 1)))
            .andExpect(model().attribute("fechaFin", java.time.LocalDate.of(2026, 12, 31)))
            .andExpect(model().attribute("defaultFilename", "reporte_existencias_2026-01-01_al_2026-12-31.txt"));
    }

    @Test
    @DisplayName("Debe entregar JSON de previsualización para el reporte de existencias")
    void testPreviewReporteExistencias() throws Exception {
        mockMvc.perform(get("/reportes/existencias/preview").session(session))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contenido", containsString("ROPASTORE — SISTEMA DE INVENTARIO")))
            .andExpect(jsonPath("$.defaultFilename", containsString("reporte_existencias_ropastore_")))
            .andExpect(jsonPath("$.titulo", containsString("Reporte General de Existencias")));
    }

    @Test
    @DisplayName("Debe entregar JSON de previsualización de existencias con filtro por fechas")
    void testPreviewReporteExistenciasConFiltro() throws Exception {
        mockMvc.perform(get("/reportes/existencias/preview")
                .param("fechaInicio", "2026-01-01")
                .param("fechaFin", "2026-12-31")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.contenido", containsString("Desde 01/01/2026 hasta 31/12/2026")))
            .andExpect(jsonPath("$.defaultFilename", containsString("reporte_existencias_2026-01-01_al_2026-12-31.txt")));
    }

    @Test
    @DisplayName("Debe entregar JSON de previsualización para el comprobante individual de transacción")
    void testPreviewReporteTransaccion() throws Exception {
        mockMvc.perform(get("/reportes/transacciones/1/preview").session(session))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contenido", containsString("COMPROBANTE DE MOVIMIENTO DE ALMACÉN")))
            .andExpect(jsonPath("$.defaultFilename", containsString("comprobante_TRX")));
    }

    @Test
    @DisplayName("Debe entregar JSON de previsualización para el historial de movimientos")
    void testPreviewReporteMovimientos() throws Exception {
        mockMvc.perform(get("/reportes/movimientos/preview").session(session))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.contenido", containsString("REPORTE DE MOVIMIENTOS HISTÓRICOS")))
            .andExpect(jsonPath("$.defaultFilename", containsString("reporte_movimientos_")));
    }

    @Test
    @DisplayName("CRÍTICO: Debe descargar el archivo TXT con ruptura por artículo y cabeceras correctas")
    void testDescargarReporteTxt() throws Exception {
        mockMvc.perform(get("/reportes/existencias/descargar").session(session))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"reporte_existencias_ropastore_")))
            .andExpect(content().contentTypeCompatibleWith("text/plain"))
            .andExpect(content().string(containsString("ROPASTORE")))
            .andExpect(content().string(containsString("CAM-001")))
            .andExpect(content().string(containsString("EXISTENCIA FINAL")));
    }

    @Test
    @DisplayName("Debe descargar reporte de existencias respetando el nombre de archivo personalizado")
    void testDescargarReporteExistenciasConNombrePersonalizado() throws Exception {
        mockMvc.perform(get("/reportes/existencias/descargar")
                .param("filename", "inventario_anual_2026")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventario_anual_2026.txt\""))
            .andExpect(content().string(containsString("ROPASTORE")));
    }

    @Test
    @DisplayName("Debe descargar comprobante de transacción respetando el nombre de archivo personalizado")
    void testDescargarComprobanteConNombrePersonalizado() throws Exception {
        mockMvc.perform(get("/reportes/transacciones/1/descargar")
                .param("filename", "factura_cliente_especial.txt")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factura_cliente_especial.txt\""))
            .andExpect(content().string(containsString("COMPROBANTE DE MOVIMIENTO")));
    }
}
