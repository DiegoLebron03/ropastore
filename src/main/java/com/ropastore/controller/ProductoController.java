package com.ropastore.controller;

import com.ropastore.dto.ProductoDTO;
import com.ropastore.entity.Producto;
import com.ropastore.entity.Transaccion;
import com.ropastore.enums.CategoriaProducto;
import com.ropastore.enums.EstadoProducto;
import com.ropastore.enums.TallaProducto;
import com.ropastore.service.InventoryService;
import com.ropastore.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    public ProductoController(ProductService productService, InventoryService inventoryService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String listarProductos(
        @RequestParam(name = "q", required = false) String busqueda,
        @RequestParam(name = "categoria", required = false) CategoriaProducto categoria,
        @RequestParam(name = "estado", required = false) EstadoProducto estado,
        Model model
    ) {
        List<Producto> productos = productService.search(busqueda, categoria, estado);

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", CategoriaProducto.values());
        model.addAttribute("estados", EstadoProducto.values());
        model.addAttribute("filtroBusqueda", busqueda);
        model.addAttribute("filtroCategoria", categoria);
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("totalProductos", productService.countTotal());
        model.addAttribute("totalActivos", productService.countActivos());
        model.addAttribute("totalInactivos", productService.countInactivos());
        model.addAttribute("totalBajoStock", productService.countBajoStock());
        model.addAttribute("activeNav", "productos");

        return "productos/list";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("productoDTO", new ProductoDTO());
        model.addAttribute("categorias", CategoriaProducto.values());
        model.addAttribute("tallas", TallaProducto.values());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeNav", "productos");
        return "productos/form";
    }

    @PostMapping("/nuevo")
    public String guardarNuevo(
        @Valid @ModelAttribute("productoDTO") ProductoDTO productoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", CategoriaProducto.values());
            model.addAttribute("tallas", TallaProducto.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeNav", "productos");
            return "productos/form";
        }

        try {
            Producto guardado = productService.create(productoDTO);
            redirectAttributes.addFlashAttribute("exitoMensaje",
                String.format("Producto [%s] %s registrado correctamente.",
                    guardado.getCodigo(), guardado.getDescripcion()));
            return "redirect:/productos";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("imagenArchivo", "error.imagen", ex.getMessage());
            model.addAttribute("categorias", CategoriaProducto.values());
            model.addAttribute("tallas", TallaProducto.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeNav", "productos");
            return "productos/form";
        } catch (Exception ex) {
            bindingResult.rejectValue("codigo", "error.codigo", ex.getMessage());
            model.addAttribute("categorias", CategoriaProducto.values());
            model.addAttribute("tallas", TallaProducto.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeNav", "productos");
            return "productos/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable("id") Long id, Model model) {
        Producto producto = productService.findById(id);

        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigo());
        dto.setDescripcion(producto.getDescripcion());
        dto.setCosto(producto.getCosto());
        dto.setPrecio(producto.getPrecio());
        dto.setCategoria(producto.getCategoria());
        dto.setMarca(producto.getMarca());
        dto.setTalla(producto.getTalla());
        dto.setColor(producto.getColor());
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setImagenUrl(producto.getImagenUrl());

        model.addAttribute("productoDTO", dto);
        model.addAttribute("productoOriginal", producto);
        model.addAttribute("categorias", CategoriaProducto.values());
        model.addAttribute("tallas", TallaProducto.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("activeNav", "productos");
        return "productos/form";
    }

    @PostMapping("/{id}/editar")
    public String guardarEdicion(
        @PathVariable("id") Long id,
        @Valid @ModelAttribute("productoDTO") ProductoDTO productoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productoOriginal", productService.findById(id));
            model.addAttribute("categorias", CategoriaProducto.values());
            model.addAttribute("tallas", TallaProducto.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeNav", "productos");
            return "productos/form";
        }

        try {
            Producto actualizado = productService.update(id, productoDTO);
            redirectAttributes.addFlashAttribute("exitoMensaje",
                String.format("Producto [%s] actualizado correctamente.", actualizado.getCodigo()));
            return "redirect:/productos/" + id;
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("imagenArchivo", "error.imagen", ex.getMessage());
            model.addAttribute("productoOriginal", productService.findById(id));
            model.addAttribute("categorias", CategoriaProducto.values());
            model.addAttribute("tallas", TallaProducto.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeNav", "productos");
            return "productos/form";
        } catch (Exception ex) {
            bindingResult.rejectValue("codigo", "error.codigo", ex.getMessage());
            model.addAttribute("productoOriginal", productService.findById(id));
            model.addAttribute("categorias", CategoriaProducto.values());
            model.addAttribute("tallas", TallaProducto.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeNav", "productos");
            return "productos/form";
        }
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable("id") Long id, Model model) {
        Producto producto = productService.findById(id);
        List<Transaccion> movimientos = inventoryService.findMovimientosPorProducto(id);

        model.addAttribute("producto", producto);
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("activeNav", "productos");
        return "productos/detail";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivarProducto(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Producto producto = productService.findById(id);
        productService.desactivar(id);
        redirectAttributes.addFlashAttribute("advertenciaMensaje",
            String.format("El producto [%s] ha sido desactivado.", producto.getCodigo()));
        return "redirect:/productos";
    }

    @PostMapping("/{id}/reactivar")
    public String reactivarProducto(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Producto producto = productService.findById(id);
        productService.reactivar(id);
        redirectAttributes.addFlashAttribute("exitoMensaje",
            String.format("El producto [%s] ha sido reactivado exitosamente.", producto.getCodigo()));
        return "redirect:/productos";
    }
}
