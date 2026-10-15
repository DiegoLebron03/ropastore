package com.ropastore.controller;

import com.ropastore.entity.Producto;
import com.ropastore.entity.Transaccion;
import com.ropastore.service.InventoryService;
import com.ropastore.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class DashboardController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    public DashboardController(ProductService productService, InventoryService inventoryService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @GetMapping({"/", "/dashboard"})
    public String index(Model model) {
        long totalProductos = productService.countTotal();
        long totalBajoStock = productService.countBajoStock();
        long movimientosHoy = inventoryService.countMovimientosHoy();
        BigDecimal valorInventario = productService.sumValorInventario();

        List<Producto> productosCriticos = productService.findBajoStock();
        List<Transaccion> ultimosMovimientos = inventoryService.findUltimosMovimientos(10);
        List<Object[]> distribucionCategorias = productService.countProductosPorCategoria();

        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalBajoStock", totalBajoStock);
        model.addAttribute("movimientosHoy", movimientosHoy);
        model.addAttribute("valorInventario", valorInventario);
        model.addAttribute("productosCriticos", productosCriticos);
        model.addAttribute("ultimosMovimientos", ultimosMovimientos);
        model.addAttribute("distribucionCategorias", distribucionCategorias);
        model.addAttribute("activeNav", "dashboard");

        return "dashboard/index";
    }
}
