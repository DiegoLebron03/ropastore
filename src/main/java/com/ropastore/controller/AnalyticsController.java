package com.ropastore.controller;

import com.ropastore.dto.AnalyticsDTO;
import com.ropastore.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analisis")
    public String index(
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model
    ) {
        // Validación de fechas lógicas
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            LocalDate temp = fechaInicio;
            fechaInicio = fechaFin;
            fechaFin = temp;
        }

        AnalyticsDTO analytics = analyticsService.obtenerDatosAnaliticos(fechaInicio, fechaFin);

        model.addAttribute("analytics", analytics);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("activeNav", "analisis");
        model.addAttribute("pageTitle", "Gráficos y Análisis");

        return "analisis/index";
    }
}

