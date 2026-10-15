package com.ropastore.repository;

import com.ropastore.entity.Producto;
import com.ropastore.entity.Transaccion;
import com.ropastore.enums.TipoTransaccion;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long>, JpaSpecificationExecutor<Transaccion> {

    List<Transaccion> findByProductoOrderByFechaAscIdAsc(Producto producto);

    @Query("SELECT t FROM Transaccion t JOIN FETCH t.producto ORDER BY t.fecha DESC, t.id DESC")
    List<Transaccion> findAllWithProductoOrderByFechaDesc();

    @Query("SELECT t FROM Transaccion t JOIN FETCH t.producto ORDER BY t.fecha DESC, t.id DESC")
    List<Transaccion> findRecentWithProducto(org.springframework.data.domain.Pageable pageable);

    long countByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Consulta optimizada para la generación del Reporte TXT.
     * Ordena estrictamente por código de artículo y luego cronológicamente por fecha/id,
     * garantizando la correcta ejecución del algoritmo de ruptura de control.
     */
    @Query("SELECT t FROM Transaccion t JOIN FETCH t.producto p ORDER BY p.codigo ASC, t.fecha ASC, t.id ASC")
    List<Transaccion> findAllForReporteTxt();

    @Query("SELECT t FROM Transaccion t JOIN FETCH t.producto p WHERE t.id = :id")
    java.util.Optional<Transaccion> findByIdWithProducto(@Param("id") Long id);

    default List<Transaccion> findWithFilters(
        Long productoId,
        TipoTransaccion tipo,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
    ) {
        if (productoId == null && tipo == null && fechaInicio == null && fechaFin == null) {
            return findAllWithProductoOrderByFechaDesc();
        }
        Specification<Transaccion> spec = (root, q, cb) -> {
            if (q.getResultType() != Long.class && q.getResultType() != long.class) {
                root.fetch("producto", jakarta.persistence.criteria.JoinType.LEFT);
            }
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (productoId != null) {
                predicates.add(cb.equal(root.get("producto").get("id"), productoId));
            }
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaFin));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return findAll(spec, org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Order.desc("fecha"),
            org.springframework.data.domain.Sort.Order.desc("id")
        ));
    }

    default List<Transaccion> findWithFiltersForReporteTxt(
        Long productoId,
        TipoTransaccion tipo,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
    ) {
        if (productoId == null && tipo == null && fechaInicio == null && fechaFin == null) {
            return findAllForReporteTxt();
        }
        Specification<Transaccion> spec = (root, q, cb) -> {
            if (q.getResultType() != Long.class && q.getResultType() != long.class) {
                root.fetch("producto", jakarta.persistence.criteria.JoinType.INNER);
            }
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (productoId != null) {
                predicates.add(cb.equal(root.get("producto").get("id"), productoId));
            }
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaFin));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return findAll(spec, org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Order.asc("producto.codigo"),
            org.springframework.data.domain.Sort.Order.asc("fecha"),
            org.springframework.data.domain.Sort.Order.asc("id")
        ));
    }
}
