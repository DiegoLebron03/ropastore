package com.ropastore.service;

import com.ropastore.dto.ProductoDTO;
import com.ropastore.entity.Producto;
import com.ropastore.enums.CategoriaProducto;
import com.ropastore.enums.EstadoProducto;
import com.ropastore.exception.ProductoDuplicadoException;
import com.ropastore.exception.ResourceNotFoundException;
import com.ropastore.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductoRepository productoRepository;
    private final FileStorageService fileStorageService;

    public ProductService(ProductoRepository productoRepository, FileStorageService fileStorageService) {
        this.productoRepository = productoRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Producto> findAll() {
        return productoRepository.findAllByOrderByCodigoAsc();
    }

    public List<Producto> findActivos() {
        return productoRepository.findByEstadoOrderByCodigoAsc(EstadoProducto.ACTIVO);
    }

    public List<Producto> findInactivos() {
        return productoRepository.findByEstadoOrderByCodigoAsc(EstadoProducto.INACTIVO);
    }

    public Producto findById(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto con ID: " + id));
    }

    public Producto findByCodigo(String codigo) {
        return productoRepository.findByCodigoIgnoreCase(codigo)
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto con código: " + codigo));
    }

    public List<Producto> search(String query, CategoriaProducto categoria, EstadoProducto estado) {
        String texto = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        return productoRepository.findWithFilters(texto, categoria, estado);
    }

    @Transactional
    public Producto create(ProductoDTO dto) {
        String codigoUpper = dto.getCodigo().trim().toUpperCase();
        if (productoRepository.existsByCodigoIgnoreCase(codigoUpper)) {
            throw new ProductoDuplicadoException(codigoUpper);
        }

        Producto producto = new Producto(
            codigoUpper,
            dto.getDescripcion(),
            dto.getCosto(),
            dto.getPrecio(),
            dto.getCategoria(),
            dto.getMarca(),
            dto.getTalla(),
            dto.getColor(),
            dto.getStockMinimo()
        );

        // Procesar subida de imagen
        if (dto.getImagenArchivo() != null && !dto.getImagenArchivo().isEmpty()) {
            String rutaImagen = fileStorageService.storeProductoImage(dto.getImagenArchivo());
            producto.setImagenUrl(rutaImagen);
        } else if (dto.getImagenUrl() != null && !dto.getImagenUrl().isBlank()) {
            producto.setImagenUrl(dto.getImagenUrl().trim());
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto update(Long id, ProductoDTO dto) {
        Producto producto = findById(id);
        String codigoUpper = dto.getCodigo().trim().toUpperCase();

        if (productoRepository.existsByCodigoIgnoreCaseAndIdNot(codigoUpper, id)) {
            throw new ProductoDuplicadoException(codigoUpper);
        }

        producto.setCodigo(codigoUpper);
        producto.setDescripcion(dto.getDescripcion());
        producto.setCosto(dto.getCosto());
        producto.setPrecio(dto.getPrecio());
        producto.setCategoria(dto.getCategoria());
        producto.setMarca(dto.getMarca());
        producto.setTalla(dto.getTalla());
        producto.setColor(dto.getColor());
        producto.setStockMinimo(dto.getStockMinimo());

        // Actualizar imagen si se subió un nuevo archivo
        if (dto.getImagenArchivo() != null && !dto.getImagenArchivo().isEmpty()) {
            String imagenAnterior = producto.getImagenUrl();
            String nuevaRuta = fileStorageService.storeProductoImage(dto.getImagenArchivo());
            producto.setImagenUrl(nuevaRuta);
            fileStorageService.deleteProductoImage(imagenAnterior);
        } else if (dto.getImagenUrl() != null && !dto.getImagenUrl().isBlank()) {
            producto.setImagenUrl(dto.getImagenUrl().trim());
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public void desactivar(Long id) {
        Producto producto = findById(id);
        producto.setEstado(EstadoProducto.INACTIVO);
        productoRepository.save(producto);
    }

    @Transactional
    public void reactivar(Long id) {
        Producto producto = findById(id);
        producto.setEstado(EstadoProducto.ACTIVO);
        productoRepository.save(producto);
    }

    public long countTotal() {
        return productoRepository.count();
    }

    public long countActivos() {
        return productoRepository.countByEstado(EstadoProducto.ACTIVO);
    }

    public long countInactivos() {
        return productoRepository.countByEstado(EstadoProducto.INACTIVO);
    }

    public long countBajoStock() {
        return productoRepository.countProductosBajoStock();
    }

    public List<Producto> findBajoStock() {
        return productoRepository.findProductosBajoStock();
    }

    public BigDecimal sumValorInventario() {
        return productoRepository.sumValorInventario();
    }

    public List<Object[]> countProductosPorCategoria() {
        return productoRepository.countProductosPorCategoria();
    }
}
