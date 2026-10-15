package com.ropastore.dto;

import com.ropastore.enums.TipoTransaccion;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransaccionDTO {

    @NotNull(message = "Debe seleccionar un producto")
    private Long productoId;

    @NotNull(message = "El tipo de operación es obligatorio")
    private TipoTransaccion tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima debe ser al menos 1 unidad")
    private Integer cantidad;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El costo no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de costo inválido")
    private BigDecimal costoUnitario;

    @Size(max = 255, message = "La observación no debe exceder 255 caracteres")
    private String observacion;

    private LocalDateTime fecha;

    private Boolean generarReporte = false;

    public TransaccionDTO() {
        this.fecha = LocalDateTime.now();
    }

    public Boolean getGenerarReporte() {
        return generarReporte != null && generarReporte;
    }

    public void setGenerarReporte(Boolean generarReporte) {
        this.generarReporte = generarReporte;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
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

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
