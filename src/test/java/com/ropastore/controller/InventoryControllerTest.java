package com.ropastore.controller;

import com.ropastore.entity.Producto;
import com.ropastore.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    @DisplayName("Debe mostrar la pantalla de operaciones de inventario con status 200")
    void testFormularioOperaciones() throws Exception {
        mockMvc.perform(get("/inventario/operaciones"))
            .andExpect(status().isOk())
            .andExpect(view().name("inventario/operaciones"))
            .andExpect(model().attributeExists("productos"))
            .andExpect(model().attributeExists("tiposEntrada"))
            .andExpect(model().attributeExists("tiposSalida"))
            .andExpect(content().string(containsString("Entradas y Salidas")));
    }

    @Test
    @DisplayName("Debe registrar entrada y redirigir a historial con mensaje de éxito")
    void testRegistrarEntradaOperacion() throws Exception {
        Producto cam = productoRepository.findByCodigoIgnoreCase("CAM-001").orElseThrow();

        mockMvc.perform(post("/inventario/operaciones")
                .param("productoId", String.valueOf(cam.getId()))
                .param("tipo", "COMPRA")
                .param("cantidad", "10")
                .param("costoUnitario", "850.00")
                .param("observacion", "Entrada de prueba"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/inventario/movimientos"))
            .andExpect(flash().attributeExists("exitoMensaje"))
            .andExpect(flash().attributeCount(1));
    }

    @Test
    @DisplayName("Debe registrar operación con generarReporte=true y adjuntar flash attribute descargarReporteTransaccionId")
    void testRegistrarOperacionConGeneracionDeReporteIndividual() throws Exception {
        Producto cam = productoRepository.findByCodigoIgnoreCase("CAM-001").orElseThrow();

        mockMvc.perform(post("/inventario/operaciones")
                .param("productoId", String.valueOf(cam.getId()))
                .param("tipo", "COMPRA")
                .param("cantidad", "5")
                .param("costoUnitario", "850.00")
                .param("observacion", "Entrada con solicitud de comprobante")
                .param("generarReporte", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/inventario/movimientos"))
            .andExpect(flash().attributeExists("exitoMensaje"))
            .andExpect(flash().attributeExists("descargarReporteTransaccionId"));
    }

    @Test
    @DisplayName("Debe listar historial de movimientos de inventario con status 200")
    void testListarMovimientos() throws Exception {
        mockMvc.perform(get("/inventario/movimientos"))
            .andExpect(status().isOk())
            .andExpect(view().name("inventario/movimientos"))
            .andExpect(model().attributeExists("movimientos"))
            .andExpect(content().string(containsString("Historial de Movimientos")));
    }
}
