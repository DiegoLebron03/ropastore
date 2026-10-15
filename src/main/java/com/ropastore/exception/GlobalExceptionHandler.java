package com.ropastore.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InventarioInsuficienteException.class)
    public String handleInventarioInsuficiente(InventarioInsuficienteException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMensaje", ex.getMessage() +
            String.format(" (Stock disponible: %d | Cantidad solicitada: %d)",
                ex.getStockDisponible(), ex.getCantidadSolicitada()));
        return "redirect:/inventario/operaciones";
    }

    @ExceptionHandler(ProductoDuplicadoException.class)
    public String handleProductoDuplicado(ProductoDuplicadoException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMensaje", ex.getMessage());
        return "redirect:/productos";
    }

    @ExceptionHandler(ProductoInactivoException.class)
    public String handleProductoInactivo(ProductoInactivoException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMensaje", ex.getMessage());
        return "redirect:/inventario/operaciones";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMensaje", ex.getMessage());
        return "redirect:/productos";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMensaje", ex.getMessage());
        return "redirect:/productos";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMensaje",
            "Ocurrió un error inesperado al procesar la solicitud (ERR-500). Por favor intente nuevamente.");
        return "redirect:/dashboard";
    }
}
