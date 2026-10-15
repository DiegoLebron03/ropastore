package com.ropastore.controller;

import com.ropastore.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, new Usuario("Admin", "1234", "Administrador", "ADMIN"));
    }

    @Test
    @DisplayName("Debe cargar el Dashboard con status 200 y todos los KPIs requeridos cuando hay sesión")
    void testDashboardCargaCorrecta() throws Exception {
        mockMvc.perform(get("/dashboard").session(session))
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
