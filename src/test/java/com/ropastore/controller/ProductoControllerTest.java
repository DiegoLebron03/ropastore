package com.ropastore.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Debe listar productos en catálogo con status 200")
    void testListarProductos() throws Exception {
        mockMvc.perform(get("/productos"))
            .andExpect(status().isOk())
            .andExpect(view().name("productos/list"))
            .andExpect(model().attributeExists("productos"))
            .andExpect(content().string(containsString("Catálogo de Productos")));
    }

    @Test
    @DisplayName("Debe mostrar formulario de nuevo producto")
    void testFormularioNuevo() throws Exception {
        mockMvc.perform(get("/productos/nuevo"))
            .andExpect(status().isOk())
            .andExpect(view().name("productos/form"))
            .andExpect(model().attributeExists("productoDTO"));
    }

    @Test
    @DisplayName("Debe crear producto nuevo y redirigir a catálogo con mensaje")
    void testCrearProductoExitoso() throws Exception {
        mockMvc.perform(post("/productos/nuevo")
                .param("codigo", "TEST-NOVO")
                .param("descripcion", "Prenda de prueba nueva")
                .param("costo", "450.00")
                .param("precio", "890.00")
                .param("categoria", "CAMISAS")
                .param("marca", "Test Brand")
                .param("stockMinimo", "5"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/productos"))
            .andExpect(flash().attributeExists("exitoMensaje"));
    }

    @Test
    @DisplayName("Debe crear producto cargando archivo de imagen y asignando ruta web")
    void testCrearProductoConImagenExitoso() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "imagenArchivo",
            "foto-camisa.jpg",
            "image/jpeg",
            "fake-image-bytes".getBytes()
        );

        mockMvc.perform(multipart("/productos/nuevo")
                .file(file)
                .param("codigo", "CAM-IMG-1")
                .param("descripcion", "Camisa con Imagen Subida")
                .param("costo", "600.00")
                .param("precio", "1200.00")
                .param("categoria", "CAMISAS")
                .param("stockMinimo", "5"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/productos"))
            .andExpect(flash().attributeExists("exitoMensaje"));
    }

    @Test
    @DisplayName("Debe rechazar subida si el archivo no es una imagen permitida")
    void testCrearProductoConArchivoInvalido() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "imagenArchivo",
            "script.exe",
            "application/x-msdownload",
            "contenido-invalido".getBytes()
        );

        mockMvc.perform(multipart("/productos/nuevo")
                .file(file)
                .param("codigo", "CAM-BAD")
                .param("descripcion", "Prenda con archivo ejecutable")
                .param("costo", "600.00")
                .param("precio", "1200.00")
                .param("categoria", "CAMISAS")
                .param("stockMinimo", "5"))
            .andExpect(status().isOk())
            .andExpect(view().name("productos/form"))
            .andExpect(model().hasErrors());
    }
}
