package cl.duoc.bn.inventario.repository;

import cl.duoc.bn.inventario.model.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Producto> findByCategoriaId(Long categoriaId);

    long countByCategoriaId(Long categoriaId);
}