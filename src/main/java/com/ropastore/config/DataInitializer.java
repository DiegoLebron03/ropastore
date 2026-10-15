package com.ropastore.config;

import com.ropastore.entity.Producto;
import com.ropastore.enums.CategoriaProducto;
import com.ropastore.enums.EstadoProducto;
import com.ropastore.enums.TallaProducto;
import com.ropastore.enums.TipoTransaccion;
import com.ropastore.repository.ProductoRepository;
import com.ropastore.service.InventoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository productoRepository;
    private final InventoryService inventoryService;

    public DataInitializer(ProductoRepository productoRepository, InventoryService inventoryService) {
        this.productoRepository = productoRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() > 0) {
            return;
        }

        // 1. CAM-001: Camisa Oversize Básica (Stock final: 30)
        Producto cam = new Producto("CAM-001", "Camisa Oversize Básica", new BigDecimal("850.00"),
            new BigDecimal("1450.00"), CategoriaProducto.CAMISAS, "Zara Studio", TallaProducto.M, "Blanco Ópalo", 10);
        cam.setImagenUrl("/images/productos/camisa-001.jpg");
        cam = productoRepository.save(cam);
        inventoryService.registrarEntrada(cam.getId(), TipoTransaccion.COMPRA, 50, new BigDecimal("850.00"),
            "Lote inicial de apertura de tienda", LocalDateTime.now().minusDays(14));
        inventoryService.registrarSalida(cam.getId(), TipoTransaccion.VENTA, 20, new BigDecimal("850.00"),
            "Ventas en tienda física primera quincena", LocalDateTime.now().minusDays(5));

        // 2. JEA-002: Jean Slim Fit Denim (Stock final: 35)
        Producto jea = new Producto("JEA-002", "Jean Slim Fit Denim", new BigDecimal("1200.00"),
            new BigDecimal("2100.00"), CategoriaProducto.JEANS, "Levi's", TallaProducto.L, "Azul Índigo", 8);
        jea.setImagenUrl("/images/productos/jeans-002.jpg");
        jea = productoRepository.save(jea);
        inventoryService.registrarEntrada(jea.getId(), TipoTransaccion.COMPRA, 40, new BigDecimal("1200.00"),
            "Recepción proveedor Denim Corp", LocalDateTime.now().minusDays(13));
        inventoryService.registrarSalida(jea.getId(), TipoTransaccion.VENTA, 15, new BigDecimal("1200.00"),
            "Ventas fin de semana", LocalDateTime.now().minusDays(6));
        inventoryService.registrarEntrada(jea.getId(), TipoTransaccion.AJUSTE_POSITIVO, 10, new BigDecimal("1200.00"),
            "Conteo físico de auditoría interna", LocalDateTime.now().minusDays(2));

        // 3. CHA-003: Chaqueta Denim Clásica (Stock final: 3 -> Bajo Stock! Min: 5)
        Producto cha = new Producto("CHA-003", "Chaqueta Denim Clásica", new BigDecimal("1800.00"),
            new BigDecimal("3200.00"), CategoriaProducto.CHAQUETAS, "Pull & Bear", TallaProducto.L, "Negro Deslavado", 5);
        cha.setImagenUrl("/images/productos/chaqueta-003.jpg");
        cha = productoRepository.save(cha);
        inventoryService.registrarEntrada(cha.getId(), TipoTransaccion.COMPRA, 15, new BigDecimal("1800.00"),
            "Colección Otoño", LocalDateTime.now().minusDays(11));
        inventoryService.registrarSalida(cha.getId(), TipoTransaccion.VENTA, 12, new BigDecimal("1800.00"),
            "Liquidación promocional", LocalDateTime.now().minusDays(3));

        // 4. GOR-004: Gorra Classic Vintage (Stock final: 25)
        Producto gor = new Producto("GOR-004", "Gorra Classic Vintage", new BigDecimal("350.00"),
            new BigDecimal("650.00"), CategoriaProducto.GORRAS, "New Era", TallaProducto.UNICA, "Verde Bosque", 12);
        gor.setImagenUrl("/images/productos/polo-004.jpg");
        gor = productoRepository.save(gor);
        inventoryService.registrarEntrada(gor.getId(), TipoTransaccion.COMPRA, 60, new BigDecimal("350.00"),
            "Surtido de accesorios", LocalDateTime.now().minusDays(9));
        inventoryService.registrarSalida(gor.getId(), TipoTransaccion.VENTA, 35, new BigDecimal("350.00"),
            "Venta masiva fin de mes", LocalDateTime.now().minusDays(1));

        // 5. ZAP-005: Sneakers Urban Casual (Stock final: 4 -> Bajo Stock! Min: 6)
        Producto zap = new Producto("ZAP-005", "Sneakers Urban Casual", new BigDecimal("2200.00"),
            new BigDecimal("3900.00"), CategoriaProducto.ZAPATOS, "Adidas Neo", TallaProducto.M, "Blanco / Verde", 6);
        zap.setImagenUrl("/images/productos/accesorios-005.jpg");
        zap = productoRepository.save(zap);
        inventoryService.registrarEntrada(zap.getId(), TipoTransaccion.COMPRA, 20, new BigDecimal("2200.00"),
            "Lote calzado deportivo", LocalDateTime.now().minusDays(7));
        inventoryService.registrarSalida(zap.getId(), TipoTransaccion.VENTA, 16, new BigDecimal("2200.00"),
            "Venta especial calzado", LocalDateTime.now().minusDays(1));
    }
}
