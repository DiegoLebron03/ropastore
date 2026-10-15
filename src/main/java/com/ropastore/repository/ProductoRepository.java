package com.ropastore.repository;

import com.ropastore.entity.Producto;
import com.ropastore.enums.CategoriaProducto;
import com.ropastore.enums.EstadoProducto;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    Optional<Producto> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    List<Producto> findByEstadoOrderByCodigoAsc(EstadoProducto estado);

    List<Producto> findAllByOrderByCodigoAsc();

    long countByEstado(EstadoProducto estado);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.estado = 'ACTIVO' AND p.stockActual <= p.stockMinimo")
    long countProductosBajoStock();

    @Query("SELECT p FROM Producto p WHERE p.estado = 'ACTIVO' AND p.stockActual <= p.stockMinimo ORDER BY p.stockActual ASC")
    List<Producto> findProductosBajoStock();

    @Query("SELECT COALESCE(SUM(p.costo * p.stockActual), 0) FROM Producto p WHERE p.estado = 'ACTIVO'")
    BigDecimal sumValorInventario();

    @Query("SELECT p.categoria, COUNT(p) FROM Producto p WHERE p.estado = 'ACTIVO' GROUP BY p.categoria")
    List<Object[]> countProductosPorCategoria();

    default List<Producto> findWithFilters(String filtroTexto, CategoriaProducto categoria, EstadoProducto estado) {
        String texto = (filtroTexto != null && !filtroTexto.trim().isEmpty()) ? filtroTexto.trim().toLowerCase() : null;
        if (texto == null && categoria == null && estado == null) {
            return findAllByOrderByCodigoAsc();
        }
        Specification<Producto> spec = (root, q, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (texto != null) {
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("codigo")), "%" + texto + "%"),
                    cb.like(cb.lower(root.get("descripcion")), "%" + texto + "%")
                ));
            }
            if (categoria != null) {
                predicates.add(cb.equal(root.get("categoria"), categoria));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return findAll(spec, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "codigo"));
    }
}
