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
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, new Usuario("Admin", "1234", "Administrador", "ADMIN"));
    }

    @Test
    @DisplayName("Debe redirigir al login si se accede a /analisis sin sesión iniciada")
    void testAccesoSinSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/analisis"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Debe cargar la vista de gráficos y analíticas con status 200 y métricas cuando hay sesión")
    void testAnalisisCargaCorrectaConSesion() throws Exception {
        mockMvc.perform(get("/analisis").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("analisis/index"))
                .andExpect(model().attributeExists("analytics"))
                .andExpect(model().attribute("activeNav", "analisis"))
                .andExpect(content().string(containsString("Gráficos y Estadísticas del Inventario")));
    }

    @Test
    @DisplayName("Debe filtrar métricas analíticas por rango de fechas correctamente")
    void testAnalisisConFiltroFechas() throws Exception {
        mockMvc.perform(get("/analisis")
                        .session(session)
                        .param("fechaInicio", "2026-10-01")
                        .param("fechaFin", "2026-10-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("analisis/index"))
                .andExpect(model().attributeExists("analytics"))
                .andExpect(model().attributeExists("fechaInicio"))
                .andExpect(model().attributeExists("fechaFin"));
    }
}

