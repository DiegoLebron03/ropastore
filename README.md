# RopaStore — Sistema de Control y Gestión de Inventarios

Sistema integral de gestión y control de inventarios desarrollado para tiendas de ropa y moda minorista, implementado en Java 21 LTS, Spring Boot 3.3.4, Spring MVC, Thymeleaf, Bootstrap 5.3, Spring Data JPA y base de datos PostgreSQL en la nube alojada en Neon.

La interfaz de usuario implementa un diseño moderno, responsivo y minimalista (paleta esmeralda #16A34A, tipografía Inter, iconos Material Symbols) sin dependencias de frameworks JavaScript pesados.

---

## Características Principales

1. **Dashboard Operativo en Tiempo Real (`/` o `/dashboard`)**:
   - 4 métricas clave (KPIs): Total de productos, productos con stock bajo/crítico, movimientos del día y valoración total del inventario en USD/moneda local.
   - Banner de advertencia contextual para productos bajo nivel mínimo.
   - Feed de movimientos recientes con enlaces de acción inmediata.

2. **Catálogo y Gestión de Productos (`/productos`)**:
   - **Carga de Fotografía de la Prenda**: Subida de imágenes (JPG, PNG, WebP) con drag & drop, previsualización en vivo en el cliente y validación de seguridad.
   - **Inventario Visual**: Miniaturas fotográficas de prendas integradas en el catálogo, panel de detalles, operaciones de almacén y dashboard.
   - Listado paginado y filtrable por categoría, estado (Activo/Inactivo) y búsqueda de texto.
   - Indicadores visuales de stock: Normal, Advertencia y Agotado.
   - Modales de confirmación para Desactivación y Reactivación de productos.
   - Formulario de alta y edición con Bean Validation.
   - Ficha de detalle de producto con presentación fotográfica, historial específico y valorización.

3. **Operaciones de Inventario (`/inventario/operaciones`)**:
   - Formulario de pestañas para Entradas (Compras, Devoluciones, Ajustes positivos) y Salidas (Ventas, Mermas, Donaciones).
   - Carga dinámica del stock actual y costo en el cliente.
   - **Regla de Negocio Crítica**: Validación preventiva y del lado servidor (`InventarioInsuficienteException` / `ERR-409`) que impide que una salida exceda las existencias físicas disponibles.
   - Cálculo atómico del balance (`Existencia = Entradas - Salidas`).

4. **Kárdex y Auditoría de Movimientos (`/inventario/movimientos`)**:
   - Trazabilidad completa con fecha/hora, artículo, tipo de transacción, cantidad (+/-), costo unitario y balance acumulado.
   - Filtros por producto, tipo de movimiento y rango de fechas.

5. **Reporte Plano TXT con Ruptura por Artículo (`/reportes`)**:
   - Algoritmo de corte de control/ruptura por artículo.
   - Formato columnar ordenado por artículo y fecha: `Fecha | Tipo Transacción | Cantidad | Costo Unit. ($) | Balance`.
   - Total de existencia final por artículo y resumen global consolidado.
   - Descarga directa en UTF-8 (`/reportes/existencias/descargar`).

---

## Stack Tecnológico

- **Lenguaje:** Java 21 LTS
- **Framework:** Spring Boot 3.3.4
- **Persistencia:** Spring Data JPA / Hibernate 6, Flyway Migrations
- **Base de Datos:** PostgreSQL en la nube (Neon)
- **Motor de Plantillas:** Thymeleaf con layouts modulares (`Thymeleaf Layout / Fragments`)
- **Frontend y Estilos:** Bootstrap 5.3.3 y CSS personalizado (`ropastore.css`)
- **Pruebas:** JUnit 5, Mockito, Spring Boot Test, MockMvc

---

## Ejecución del Proyecto

### Prerrequisitos
- JDK 21 o superior instalado.
- Maven Wrapper incluido (`./mvnw`).
- Base de datos Neon PostgreSQL configurada en `.env.local` o variables de entorno.

### 1. Iniciar la Aplicación
Las variables de conexión a Neon PostgreSQL son cargadas automáticamente desde `.env.local` al iniciar:

```bash
npm run start
```
O directamente con Maven Wrapper:
```bash
./mvnw spring-boot:run
```

La aplicación iniciará en:
http://localhost:8080

Al iniciar, Flyway ejecuta automáticamente las migraciones (`V1__init_schema.sql`, `V2__seed_data.sql` y `V3__add_imagen_url_to_productos.sql`), aplicando el esquema y datos iniciales en Neon.

### 2. Ejecutar Pruebas Automatizadas
Para ejecutar la suite de pruebas unitarias y de integración:

```bash
npm run test
```
O con Maven:
```bash
./mvnw clean test
```

---

## Estructura del Proyecto

```
ropastore/
├── src/
│   ├── main/
│   │   ├── java/com/ropastore/
│   │   │   ├── config/         # Configuración y DataInitializer
│   │   │   ├── controller/     # Controladores MVC (Dashboard, Producto, Inventario, Reportes)
│   │   │   ├── dto/            # DTOs de transferencia y formularios
│   │   │   ├── exception/      # Manejador global y excepciones de dominio
│   │   │   ├── entity/         # Entidades JPA (Producto, Transaccion) y Enums
│   │   │   ├── repository/     # Repositorios Spring Data JPA con Specifications
│   │   │   └── service/        # Lógica de negocio (Product, Inventory, Report)
│   │   └── resources/
│   │       ├── db/migration/   # Migraciones Flyway en PostgreSQL
│   │       ├── static/         # CSS corporativo, Bootstrap 5, JS y assets
│   │       └── templates/      # Plantillas Thymeleaf (layout, productos, inventario, etc.)
│   │       └── application*.properties
│   └── test/java/com/ropastore/ # Suite de pruebas JUnit 5 / MockMvc
├── pom.xml                     # Configuración de dependencias Maven
└── package.json                # Scripts de automatización y despliegue Neon
```

---

## Licencia y Autoría

Desarrollado por Diego Lebron.
Distribuido bajo la Licencia MIT.

