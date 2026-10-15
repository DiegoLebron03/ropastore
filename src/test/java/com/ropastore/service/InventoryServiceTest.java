package com.ropastore.service;

import com.ropastore.entity.Producto;
import com.ropastore.entity.Transaccion;
import com.ropastore.enums.CategoriaProducto;
import com.ropastore.enums.EstadoProducto;
import com.ropastore.enums.TipoTransaccion;
import com.ropastore.exception.InventarioInsuficienteException;
import com.ropastore.exception.ProductoInactivoException;
import com.ropastore.repository.ProductoRepository;
import com.ropastore.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    private Producto productoPrueba;

    @BeforeEach
    void setUp() {
        productoPrueba = new Producto(
            "TEST-001",
            "Producto de Prueba",
            new BigDecimal("500.00"),
            new BigDecimal("800.00"),
            CategoriaProducto.CAMISAS,
            "Marca Test",
            null,
            "Azul",
            10
        );
        productoPrueba = productoRepository.save(productoPrueba);
    }

    @Test
    @DisplayName("Debe registrar entrada y aumentar la existencia correctamente")
    void testRegistrarEntrada() {
        Transaccion t = inventoryService.registrarEntrada(
            productoPrueba.getId(),
            TipoTransaccion.COMPRA,
            25,
            new BigDecimal("500.00"),
            "Entrada test",
            LocalDateTime.now()
        );

        assertNotNull(t.getId());
        assertEquals(25, t.getCantidad());
        assertEquals(25, t.getBalanceResultante());

        Producto actualizado = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(25, actualizado.getStockActual());
    }

    @Test
    @DisplayName("Debe registrar salida y disminuir la existencia correctamente")
    void testRegistrarSalida() {
        // Inicializar con stock 50
        inventoryService.registrarEntrada(
            productoPrueba.getId(),
            TipoTransaccion.COMPRA,
            50,
            new BigDecimal("500.00"),
            "Lote inicial",
            LocalDateTime.now()
        );

        Transaccion salida = inventoryService.registrarSalida(
            productoPrueba.getId(),
            TipoTransaccion.VENTA,
            15,
            new BigDecimal("500.00"),
            "Venta regular",
            LocalDateTime.now()
        );

        assertEquals(15, salida.getCantidad());
        assertEquals(35, salida.getBalanceResultante());

        Producto actualizado = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(35, actualizado.getStockActual());
    }

    @Test
    @DisplayName("CRÍTICO: Debe rechazar salida superior al stock disponible y lanzar InventarioInsuficienteException")
    void testRechazarSalidaSuperiorAStock() {
        // Stock actual: 10
        inventoryService.registrarEntrada(
            productoPrueba.getId(),
            TipoTransaccion.COMPRA,
            10,
            new BigDecimal("500.00"),
            "Stock 10",
            LocalDateTime.now()
        );

        // Solicitar salida de 15
        InventarioInsuficienteException ex = assertThrows(
            InventarioInsuficienteException.class,
            () -> inventoryService.registrarSalida(
                productoPrueba.getId(),
                TipoTransaccion.VENTA,
                15,
                new BigDecimal("500.00"),
                "Salida inválida",
                LocalDateTime.now()
            )
        );

        assertEquals("No hay suficiente inventario disponible para realizar esta operación.", ex.getMessage());
        assertEquals("TEST-001", ex.getCodigoProducto());
        assertEquals(10, ex.getStockDisponible());
        assertEquals(15, ex.getCantidadSolicitada());

        // Verificar que el stock no cambió
        Producto producto = productoRepository.findById(productoPrueba.getId()).orElseThrow();
        assertEquals(10, producto.getStockActual());
    }

    @Test
    @DisplayName("Prueba de Balance requerida en especificación: Entrada 100 -> Salida 30 -> Entrada 20 -> Salida 10 = 80")
    void testSecuenciaBalanceEspecificacion() {
        Long prodId = productoPrueba.getId();

        inventoryService.registrarEntrada(prodId, TipoTransaccion.COMPRA, 100, new BigDecimal("500.00"), "Entrada 100", LocalDateTime.now());
        inventoryService.registrarSalida(prodId, TipoTransaccion.VENTA, 30, new BigDecimal("500.00"), "Salida 30", LocalDateTime.now());
        inventoryService.registrarEntrada(prodId, TipoTransaccion.AJUSTE_POSITIVO, 20, new BigDecimal("500.00"), "Entrada 20", LocalDateTime.now());
        inventoryService.registrarSalida(prodId, TipoTransaccion.VENTA, 10, new BigDecimal("500.00"), "Salida 10", LocalDateTime.now());

        Producto actualizado = productoRepository.findById(prodId).orElseThrow();
        assertEquals(80, actualizado.getStockActual(), "El stock final debe ser exactamente 80");

        int balanceAuditado = inventoryService.reconstruirBalance(prodId);
        assertEquals(80, balanceAuditado, "El balance reconstruido debe ser exactamente 80");
    }

    @Test
    @DisplayName("Debe rechazar operaciones en productos inactivos")
    void testRechazarOperacionesEnProductoInactivo() {
        productoPrueba.setEstado(EstadoProducto.INACTIVO);
        productoRepository.save(productoPrueba);

        assertThrows(
            ProductoInactivoException.class,
            () -> inventoryService.registrarEntrada(
                productoPrueba.getId(),
                TipoTransaccion.COMPRA,
                10,
                new BigDecimal("500.00"),
                "Entrada",
                LocalDateTime.now()
            )
        );
    }
}
