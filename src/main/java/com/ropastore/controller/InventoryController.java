package com.ropastore.controller;

import com.ropastore.dto.TransaccionDTO;
import com.ropastore.entity.Producto;
import com.ropastore.entity.Transaccion;
import com.ropastore.enums.TipoTransaccion;
import com.ropastore.service.InventoryService;
import com.ropastore.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/inventario")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    public InventoryController(InventoryService inventoryService, ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
    }

    @GetMapping("/operaciones")
    public String formularioOperaciones(
        @RequestParam(name = "productoId", required = false) Long productoId,
        @RequestParam(name = "tipo", required = false) String tipoDefecto,
        Model model
    ) {
        TransaccionDTO dto = new TransaccionDTO();
        if (productoId != null) {
            dto.setProductoId(productoId);
            try {
                Producto prod = productService.findById(productoId);
                dto.setCostoUnitario(prod.getCosto());
            } catch (Exception ignored) {
            }
        }

        if ("SALIDA".equalsIgnoreCase(tipoDefecto)) {
            dto.setTipo(TipoTransaccion.VENTA);
        } else {
            dto.setTipo(TipoTransaccion.COMPRA);
        }

        List<Producto> productosActivos = productService.findActivos();
        List<TipoTransaccion> tiposEntrada = Arrays.stream(TipoTransaccion.values())
            .filter(TipoTransaccion::isEntrada).toList();
        List<TipoTransaccion> tiposSalida = Arrays.stream(TipoTransaccion.values())
            .filter(TipoTransaccion::isSalida).toList();

        model.addAttribute("transaccionDTO", dto);
        model.addAttribute("productos", productosActivos);
        model.addAttribute("tiposEntrada", tiposEntrada);
        model.addAttribute("tiposSalida", tiposSalida);
        model.addAttribute("activeNav", "operaciones");

        return "inventario/operaciones";
    }

    @PostMapping("/operaciones")
    public String procesarOperacion(
        @Valid @ModelAttribute("transaccionDTO") TransaccionDTO transaccionDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productos", productService.findActivos());
            model.addAttribute("tiposEntrada", Arrays.stream(TipoTransaccion.values()).filter(TipoTransaccion::isEntrada).toList());
            model.addAttribute("tiposSalida", Arrays.stream(TipoTransaccion.values()).filter(TipoTransaccion::isSalida).toList());
            model.addAttribute("activeNav", "operaciones");
            return "inventario/operaciones";
        }

        try {
            Transaccion transaccion = inventoryService.registrarOperacion(transaccionDTO);
            String operacionNombre = transaccion.isEntrada() ? "Entrada" : "Salida";
            redirectAttributes.addFlashAttribute("exitoMensaje",
                String.format("%s registrada exitosamente: %d unidades de [%s] %s. Nuevo balance: %d unidades.",
                    operacionNombre,
                    transaccion.getCantidad(),
                    transaccion.getProducto().getCodigo(),
                    transaccion.getProducto().getDescripcion(),
                    transaccion.getBalanceResultante()));

            if (Boolean.TRUE.equals(transaccionDTO.getGenerarReporte())) {
                redirectAttributes.addFlashAttribute("descargarReporteTransaccionId", transaccion.getId());
            }

            return "redirect:/inventario/movimientos";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMensaje", ex.getMessage());
            return "redirect:/inventario/operaciones";
        }
    }

    @GetMapping("/movimientos")
    public String listarMovimientos(
        @RequestParam(name = "productoId", required = false) Long productoId,
        @RequestParam(name = "tipo", required = false) TipoTransaccion tipo,
        @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
        @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
        Model model
    ) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.plusDays(1).atStartOfDay().minusNanos(1) : null;

        List<Transaccion> movimientos = inventoryService.findMovimientosConFiltros(productoId, tipo, inicio, fin);

        model.addAttribute("movimientos", movimientos);
        model.addAttribute("productos", productService.findAll());
        model.addAttribute("tipos", TipoTransaccion.values());
        model.addAttribute("filtroProductoId", productoId);
        model.addAttribute("filtroTipo", tipo);
        model.addAttribute("filtroFechaInicio", fechaInicio);
        model.addAttribute("filtroFechaFin", fechaFin);
        model.addAttribute("activeNav", "movimientos");

        return "inventario/movimientos";
    }
}
