package com.ropastore.entity;

import com.ropastore.enums.TipoTransaccion;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones", indexes = {
    @Index(name = "idx_transacciones_producto", columnList = "producto_id"),
    @Index(name = "idx_transacciones_fecha", columnList = "fecha"),
    @Index(name = "idx_transacciones_tipo", columnList = "tipo")
})
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El producto asociado a la transacción es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull(message = "La fecha de la transacción es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fecha;

    @NotNull(message = "El tipo de transacción es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoTransaccion tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El costo unitario no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de costo inválido")
    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @NotNull
    @Column(name = "balance_resultante", nullable = false)
    private Integer balanceResultante;

    @Size(max = 255, message = "La observación no debe exceder 255 caracteres")
    @Column(length = 255)
    private String observacion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaccion() {
    }

    public Transaccion(Producto producto, LocalDateTime fecha, TipoTransaccion tipo,
                       Integer cantidad, BigDecimal costoUnitario, Integer balanceResultante, String observacion) {
        this.producto = producto;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.balanceResultante = balanceResultante;
        this.observacion = observacion != null ? observacion.trim() : null;
    }

    // Métodos helper
    public boolean isEntrada() {
        return this.tipo != null && this.tipo.isEntrada();
    }

    public boolean isSalida() {
        return this.tipo != null && this.tipo.isSalida();
    }

    public BigDecimal getTotalOperacion() {
        if (this.costoUnitario == null || this.cantidad == null) {
            return BigDecimal.ZERO;
        }
        return this.costoUnitario.multiply(BigDecimal.valueOf(this.cantidad));
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransaccion tipo) {
        this.tipo = tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public Integer getBalanceResultante() {
        return balanceResultante;
    }

    public void setBalanceResultante(Integer balanceResultante) {
        this.balanceResultante = balanceResultante;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion != null ? observacion.trim() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
