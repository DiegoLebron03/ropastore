package com.ropastore.entity;

import com.ropastore.enums.CategoriaProducto;
import com.ropastore.enums.EstadoProducto;
import com.ropastore.enums.TallaProducto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_productos_codigo", columnList = "codigo", unique = true),
    @Index(name = "idx_productos_estado", columnList = "estado"),
    @Index(name = "idx_productos_categoria", columnList = "categoria")
})
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código del artículo es obligatorio")
    @Size(max = 50, message = "El código no debe exceder 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @NotBlank(message = "La descripción del artículo es obligatoria")
    @Size(max = 255, message = "La descripción no debe exceder 255 caracteres")
    @Column(nullable = false, length = 255)
    private String descripcion;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El costo no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El costo debe tener formato válido (máx. 2 decimales)")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener formato válido (máx. 2 decimales)")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @NotNull(message = "El estado del producto es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProducto estado = EstadoProducto.ACTIVO;

    @NotNull(message = "La categoría es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaProducto categoria;

    @Size(max = 80, message = "La marca no debe exceder 80 caracteres")
    @Column(length = 80)
    private String marca;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TallaProducto talla;

    @Size(max = 50, message = "El color no debe exceder 50 caracteres")
    @Column(length = 50)
    private String color;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;

    @NotNull
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("fecha ASC, id ASC")
    private List<Transaccion> transacciones = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Producto() {
    }

    public Producto(String codigo, String descripcion, BigDecimal costo, BigDecimal precio,
                    CategoriaProducto categoria, String marca, TallaProducto talla, String color, Integer stockMinimo) {
        this.codigo = codigo != null ? codigo.trim().toUpperCase() : null;
        this.descripcion = descripcion != null ? descripcion.trim() : null;
        this.costo = costo;
        this.precio = precio;
        this.categoria = categoria;
        this.marca = marca;
        this.talla = talla;
        this.color = color;
        this.stockMinimo = stockMinimo != null ? stockMinimo : 5;
        this.stockActual = 0;
        this.estado = EstadoProducto.ACTIVO;
    }

    // Métodos helper
    public boolean isActivo() {
        return this.estado == EstadoProducto.ACTIVO;
    }

    public boolean isBajoStock() {
        return this.stockActual != null && this.stockMinimo != null && this.stockActual <= this.stockMinimo;
    }

    public BigDecimal getValorInventario() {
        if (this.costo == null || this.stockActual == null) {
            return BigDecimal.ZERO;
        }
        return this.costo.multiply(BigDecimal.valueOf(this.stockActual));
    }

    // Getters y Setters
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

    public EstadoProducto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProducto estado) {
        this.estado = estado;
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

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public List<Transaccion> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(List<Transaccion> transacciones) {
        this.transacciones = transacciones;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public boolean hasImagen() {
        return imagenUrl != null && !imagenUrl.trim().isEmpty();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
