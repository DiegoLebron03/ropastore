package com.ropastore.controller;

import com.ropastore.entity.Usuario;
import com.ropastore.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    public static final String SESSION_USER_KEY = "usuarioLogueado";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showLoginForm(HttpSession session,
                                @RequestParam(required = false) String logout,
                                Model model) {
        if (session != null && session.getAttribute(SESSION_USER_KEY) != null) {
            return "redirect:/dashboard";
        }
        if (logout != null) {
            model.addAttribute("infoMensaje", "Has cerrado sesión correctamente.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(required = false) String username,
                        @RequestParam(required = false) String password,
                        HttpServletRequest request,
                        Model model) {
        Optional<Usuario> usuarioOpt = authService.authenticate(username, password);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("errorMensaje", "Usuario o contraseña incorrectos.");
            model.addAttribute("username", username);
            return "login";
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_KEY, usuarioOpt.get());
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout=true";
    }
}

