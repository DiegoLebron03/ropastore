package com.ropastore.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Debe cargar el Dashboard con status 200 y todos los KPIs requeridos")
    void testDashboardCargaCorrecta() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/index"))
            .andExpect(model().attributeExists("totalProductos"))
            .andExpect(model().attributeExists("totalBajoStock"))
            .andExpect(model().attributeExists("movimientosHoy"))
            .andExpect(model().attributeExists("valorInventario"))
            .andExpect(model().attributeExists("productosCriticos"))
            .andExpect(model().attributeExists("ultimosMovimientos"))
            .andExpect(content().string(containsString("Dashboard Operativo")));
    }
}
