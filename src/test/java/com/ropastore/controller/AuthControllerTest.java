package com.ropastore.controller;

import com.ropastore.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Debe mostrar la página de login con status 200")
    void testLoginPageReturns200() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(containsString("Iniciar Sesión")))
                .andExpect(content().string(containsString("Admin")))
                .andExpect(content().string(containsString("1234")));
    }

    @Test
    @DisplayName("Debe redirigir a dashboard si el usuario ya está autenticado")
    void testLoginPageWhenAlreadyLoggedInRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, new Usuario("Admin", "1234", "Administrador", "ADMIN"));

        mockMvc.perform(get("/login").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("Debe rechazar credenciales incorrectas y mostrar mensaje de error")
    void testLoginFailureWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "Admin")
                        .param("password", "incorrecta"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("errorMensaje"));
    }

    @Test
    @DisplayName("Debe iniciar sesión correctamente con credenciales Admin / 1234")
    void testLoginSuccessWithValidCredentials() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "Admin")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(request().sessionAttribute(AuthController.SESSION_USER_KEY, org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("Debe cerrar sesión y redirigir al login")
    void testLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, new Usuario("Admin", "1234", "Administrador", "ADMIN"));

        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    @DisplayName("Debe redirigir al login cuando se intenta acceder a una ruta protegida sin autenticación")
    void testUnauthenticatedAccessRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Debe permitir el acceso a ruta protegida con sesión autenticada")
    void testAuthenticatedAccessAllowed() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, new Usuario("Admin", "1234", "Administrador", "ADMIN"));

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"));
    }
}

