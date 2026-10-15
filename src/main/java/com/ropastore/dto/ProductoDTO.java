package com.ropastore.dto;

import com.ropastore.enums.CategoriaProducto;
import com.ropastore.enums.TallaProducto;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class ProductoDTO {

    private Long id;

    @NotBlank(message = "El código del artículo es obligatorio")
    @Size(min = 2, max = 50, message = "El código debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]+$", message = "El código solo debe contener letras, números, guiones y guiones bajos")
    private String codigo;

    @NotBlank(message = "La descripción del artículo es obligatoria")
    @Size(min = 3, max = 255, message = "La descripción debe tener entre 3 y 255 caracteres")
    private String descripcion;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El costo debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de costo inválido (máx. 2 decimales)")
    private BigDecimal costo;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de precio inválido (máx. 2 decimales)")
    private BigDecimal precio;

    @NotNull(message = "La categoría es obligatoria")
    private CategoriaProducto categoria;

    @Size(max = 80, message = "La marca no debe exceder 80 caracteres")
    private String marca;

    private TallaProducto talla;

    @Size(max = 50, message = "El color no debe exceder 50 caracteres")
    private String color;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo = 5;

    private String imagenUrl;

    private MultipartFile imagenArchivo;

    public ProductoDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo != null ? codigo.trim().toUpperCase() : null;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion != null ? descripcion.trim() : null;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public CategoriaProducto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProducto categoria) {
        this.categoria = categoria;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public TallaProducto getTalla() {
        return talla;
    }

    public void setTalla(TallaProducto talla) {
        this.talla = talla;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public MultipartFile getImagenArchivo() {
        return imagenArchivo;
    }

    public void setImagenArchivo(MultipartFile imagenArchivo) {
        this.imagenArchivo = imagenArchivo;
    }
}
