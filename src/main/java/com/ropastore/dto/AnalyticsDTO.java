package com.ropastore.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Métricas clave / KPIs
    private BigDecimal totalVentasValor = BigDecimal.ZERO;
    private int totalVentasUnidades = 0;
    private BigDecimal totalComprasValor = BigDecimal.ZERO;
    private int totalComprasUnidades = 0;
    private int totalTransacciones = 0;
    private BigDecimal valorInventarioActual = BigDecimal.ZERO;
    private long totalProductos = 0;
    private long totalBajoStock = 0;

    // Gráfico 1: Evolución temporal de unidades
    private List<String> labelsFechas = new ArrayList<>();
    private List<Integer> unidadesEntrada = new ArrayList<>();
    private List<Integer> unidadesSalida = new ArrayList<>();

    // Gráfico 2: Evolución financiera por fecha ($)
    private List<BigDecimal> montoCompras = new ArrayList<>();
    private List<BigDecimal> montoVentas = new ArrayList<>();

    // Gráfico 3: Stock y Valor por Categoría
    private List<String> labelsCategorias = new ArrayList<>();
    private List<Integer> stockPorCategoria = new ArrayList<>();
    private List<BigDecimal> valorPorCategoria = new ArrayList<>();

    // Gráfico 4: Top 5 Prendas con Mayor Salida / Ventas
    private List<String> labelsTopProductos = new ArrayList<>();
    private List<Integer> cantidadesTopProductos = new ArrayList<>();

    // Gráfico 5: Desglose por Tipo de Movimiento
    private List<String> labelsTipos = new ArrayList<>();
    private List<Integer> cantidadesPorTipo = new ArrayList<>();

    // Gráfico 6: Stock Actual vs Mínimo
    private List<String> labelsStockVsMin = new ArrayList<>();
    private List<Integer> stockActualVsMin = new ArrayList<>();
    private List<Integer> stockMinimoVsMin = new ArrayList<>();

    // Tabla de desglose analítico por prenda
    private List<ItemResumenDTO> itemsResumen = new ArrayList<>();

    public static class ItemResumenDTO {
        private String codigo;
        private String descripcion;
        private String categoria;
        private int stockActual;
        private int stockMinimo;
        private int totalEntradas;
        private int totalSalidas;
        private BigDecimal costoUnitario;
        private BigDecimal valorInventario;

        public ItemResumenDTO() {
        }

        public ItemResumenDTO(String codigo, String descripcion, String categoria, int stockActual,
                              int stockMinimo, int totalEntradas, int totalSalidas,
                              BigDecimal costoUnitario, BigDecimal valorInventario) {
            this.codigo = codigo;
            this.descripcion = descripcion;
            this.categoria = categoria;
            this.stockActual = stockActual;
            this.stockMinimo = stockMinimo;
            this.totalEntradas = totalEntradas;
            this.totalSalidas = totalSalidas;
            this.costoUnitario = costoUnitario;
            this.valorInventario = valorInventario;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public int getStockActual() {
            return stockActual;
        }

        public void setStockActual(int stockActual) {
            this.stockActual = stockActual;
        }

        public int getStockMinimo() {
            return stockMinimo;
        }

        public void setStockMinimo(int stockMinimo) {
            this.stockMinimo = stockMinimo;
        }

        public int getTotalEntradas() {
            return totalEntradas;
        }

        public void setTotalEntradas(int totalEntradas) {
            this.totalEntradas = totalEntradas;
        }

        public int getTotalSalidas() {
            return totalSalidas;
        }

        public void setTotalSalidas(int totalSalidas) {
            this.totalSalidas = totalSalidas;
        }

        public BigDecimal getCostoUnitario() {
            return costoUnitario;
        }

        public void setCostoUnitario(BigDecimal costoUnitario) {
            this.costoUnitario = costoUnitario;
        }

        public BigDecimal getValorInventario() {
            return valorInventario;
        }

        public void setValorInventario(BigDecimal valorInventario) {
            this.valorInventario = valorInventario;
        }
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getTotalVentasValor() {
        return totalVentasValor;
    }

    public void setTotalVentasValor(BigDecimal totalVentasValor) {
        this.totalVentasValor = totalVentasValor;
    }

    public int getTotalVentasUnidades() {
        return totalVentasUnidades;
    }

    public void setTotalVentasUnidades(int totalVentasUnidades) {
        this.totalVentasUnidades = totalVentasUnidades;
    }

    public BigDecimal getTotalComprasValor() {
        return totalComprasValor;
    }

    public void setTotalComprasValor(BigDecimal totalComprasValor) {
        this.totalComprasValor = totalComprasValor;
    }

    public int getTotalComprasUnidades() {
        return totalComprasUnidades;
    }

    public void setTotalComprasUnidades(int totalComprasUnidades) {
        this.totalComprasUnidades = totalComprasUnidades;
    }

    public int getTotalTransacciones() {
        return totalTransacciones;
    }

    public void setTotalTransacciones(int totalTransacciones) {
        this.totalTransacciones = totalTransacciones;
    }

    public BigDecimal getValorInventarioActual() {
        return valorInventarioActual;
    }

    public void setValorInventarioActual(BigDecimal valorInventarioActual) {
        this.valorInventarioActual = valorInventarioActual;
    }

    public long getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(long totalProductos) {
        this.totalProductos = totalProductos;
    }

    public long getTotalBajoStock() {
        return totalBajoStock;
    }

    public void setTotalBajoStock(long totalBajoStock) {
        this.totalBajoStock = totalBajoStock;
    }

    public List<String> getLabelsFechas() {
        return labelsFechas;
    }

    public void setLabelsFechas(List<String> labelsFechas) {
        this.labelsFechas = labelsFechas;
    }

    public List<Integer> getUnidadesEntrada() {
        return unidadesEntrada;
    }

    public void setUnidadesEntrada(List<Integer> unidadesEntrada) {
        this.unidadesEntrada = unidadesEntrada;
    }

    public List<Integer> getUnidadesSalida() {
        return unidadesSalida;
    }

    public void setUnidadesSalida(List<Integer> unidadesSalida) {
        this.unidadesSalida = unidadesSalida;
    }

    public List<BigDecimal> getMontoCompras() {
        return montoCompras;
    }

    public void setMontoCompras(List<BigDecimal> montoCompras) {
        this.montoCompras = montoCompras;
    }

    public List<BigDecimal> getMontoVentas() {
        return montoVentas;
    }

    public void setMontoVentas(List<BigDecimal> montoVentas) {
        this.montoVentas = montoVentas;
    }

    public List<String> getLabelsCategorias() {
        return labelsCategorias;
    }

    public void setLabelsCategorias(List<String> labelsCategorias) {
        this.labelsCategorias = labelsCategorias;
    }

    public List<Integer> getStockPorCategoria() {
        return stockPorCategoria;
    }

    public void setStockPorCategoria(List<Integer> stockPorCategoria) {
        this.stockPorCategoria = stockPorCategoria;
    }

    public List<BigDecimal> getValorPorCategoria() {
        return valorPorCategoria;
    }

    public void setValorPorCategoria(List<BigDecimal> valorPorCategoria) {
        this.valorPorCategoria = valorPorCategoria;
    }

    public List<String> getLabelsTopProductos() {
        return labelsTopProductos;
    }

    public void setLabelsTopProductos(List<String> labelsTopProductos) {
        this.labelsTopProductos = labelsTopProductos;
    }

    public List<Integer> getCantidadesTopProductos() {
        return cantidadesTopProductos;
    }

    public void setCantidadesTopProductos(List<Integer> cantidadesTopProductos) {
        this.cantidadesTopProductos = cantidadesTopProductos;
    }

    public List<String> getLabelsTipos() {
        return labelsTipos;
    }

    public void setLabelsTipos(List<String> labelsTipos) {
        this.labelsTipos = labelsTipos;
    }

    public List<Integer> getCantidadesPorTipo() {
        return cantidadesPorTipo;
    }

    public void setCantidadesPorTipo(List<Integer> cantidadesPorTipo) {
        this.cantidadesPorTipo = cantidadesPorTipo;
    }

    public List<String> getLabelsStockVsMin() {
        return labelsStockVsMin;
    }

    public void setLabelsStockVsMin(List<String> labelsStockVsMin) {
        this.labelsStockVsMin = labelsStockVsMin;
    }

    public List<Integer> getStockActualVsMin() {
        return stockActualVsMin;
    }

    public void setStockActualVsMin(List<Integer> stockActualVsMin) {
        this.stockActualVsMin = stockActualVsMin;
    }

    public List<Integer> getStockMinimoVsMin() {
        return stockMinimoVsMin;
    }

    public void setStockMinimoVsMin(List<Integer> stockMinimoVsMin) {
        this.stockMinimoVsMin = stockMinimoVsMin;
    }

    public List<ItemResumenDTO> getItemsResumen() {
        return itemsResumen;
    }

    public void setItemsResumen(List<ItemResumenDTO> itemsResumen) {
        this.itemsResumen = itemsResumen;
    }
}

