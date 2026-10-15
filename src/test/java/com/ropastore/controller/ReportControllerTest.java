package com.ropastore.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Debe cargar la vista de reportes con previsualización")
    void testVerReportesVista() throws Exception {
        mockMvc.perform(get("/reportes"))
            .andExpect(status().isOk())
            .andExpect(view().name("reportes/index"))
            .andExpect(model().attributeExists("reporteTexto"))
            .andExpect(content().string(containsString("REPORTE DE EXISTENCIAS")));
    }

    @Test
    @DisplayName("CRÍTICO: Debe descargar el archivo TXT con ruptura por artículo y cabeceras correctas")
    void testDescargarReporteTxt() throws Exception {
        mockMvc.perform(get("/reportes/existencias/descargar"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"reporte_existencias_ropastore_")))
            .andExpect(content().contentTypeCompatibleWith("text/plain"))
            .andExpect(content().string(containsString("ROPASTORE")))
            .andExpect(content().string(containsString("CAM-001")))
            .andExpect(content().string(containsString("EXISTENCIA FINAL")));
    }
}
