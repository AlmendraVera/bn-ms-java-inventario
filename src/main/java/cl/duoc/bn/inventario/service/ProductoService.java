package cl.duoc.bn.inventario.service;

import cl.duoc.bn.inventario.exception.ApiException;
import cl.duoc.bn.inventario.model.Categoria;
import cl.duoc.bn.inventario.model.Producto;
import cl.duoc.bn.inventario.repository.CategoriaRepository;
import cl.duoc.bn.inventario.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    /** Respuesta de la consulta de disponibilidad (la usan ordenes y picking-despacho). */
    public record Disponibilidad(String sku, Integer cantidadSolicitada, Integer stockActual, boolean disponible) {
    }

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Producto> listar(Long categoriaId) {
        if (categoriaId != null) {
            return productoRepository.findByCategoriaId(categoriaId);
        }
        return productoRepository.findAll();
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe un producto con id " + id));
    }

    public Producto buscarPorSku(String sku) {
        return productoRepository.findBySku(sku)
                .orElseThrow(() -> ApiException.noEncontrado("No existe un producto con SKU " + sku));
    }

    public Producto crear(Producto producto) {
        validar(producto);
        String sku = producto.getSku().trim().toUpperCase();
        if (productoRepository.existsBySku(sku)) {
            throw ApiException.conflicto("Ya existe un producto con el SKU " + sku);
        }
        producto.setId(null);
        producto.setSku(sku);
        producto.setCategoria(resolverCategoria(producto.getCategoria()));
        return productoRepository.save(producto);
    }

    public Producto actualizar(Long id, Producto datos) {
        validar(datos);
        Producto producto = buscarPorId(id);
        String sku = datos.getSku().trim().toUpperCase();
        productoRepository.findBySku(sku).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw ApiException.conflicto("Ya existe un producto con el SKU " + sku);
            }
        });
        producto.setSku(sku);
        producto.setNombre(datos.getNombre().trim());
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());
        producto.setCategoria(resolverCategoria(datos.getCategoria()));
        return productoRepository.save(producto);
    }

    public void eliminar(Long id) {
        productoRepository.delete(buscarPorId(id));
    }

    public Disponibilidad consultarDisponibilidad(String sku, int cantidad) {
        if (cantidad <= 0) {
            throw ApiException.solicitudInvalida("La cantidad debe ser mayor a 0");
        }
        Producto p = buscarPorSku(sku.toUpperCase());
        return new Disponibilidad(p.getSku(), cantidad, p.getStock(), p.getStock() >= cantidad);
    }

    /** Descuenta stock (lo invoca picking-despacho al despachar). Falla con 409 si no alcanza. */
    @Transactional
    public Producto descontar(String sku, Integer cantidad) {
        validarCantidad(cantidad);
        Producto p = buscarPorSku(sku.toUpperCase());
        if (p.getStock() < cantidad) {
            throw ApiException.conflicto("Stock insuficiente para el SKU " + p.getSku()
                    + ": disponible " + p.getStock() + ", solicitado " + cantidad);
        }
        p.setStock(p.getStock() - cantidad);
        return productoRepository.save(p);
    }

    @Transactional
    public Producto reponer(String sku, Integer cantidad) {
        validarCantidad(cantidad);
        Producto p = buscarPorSku(sku.toUpperCase());
        p.setStock(p.getStock() + cantidad);
        return productoRepository.save(p);
    }

    private Categoria resolverCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() == null) {
            throw ApiException.solicitudInvalida("Debe indicar la categoría: \"categoria\": {\"id\": 1}");
        }
        return categoriaRepository.findById(categoria.getId())
                .orElseThrow(() -> ApiException.noEncontrado("No existe una categoría con id " + categoria.getId()));
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw ApiException.solicitudInvalida("La cantidad debe ser un número mayor a 0");
        }
    }

    private void validar(Producto p) {
        if (p.getSku() == null || p.getSku().isBlank()) {
            throw ApiException.solicitudInvalida("El SKU es obligatorio");
        }
        if (p.getNombre() == null || p.getNombre().isBlank()) {
            throw ApiException.solicitudInvalida("El nombre es obligatorio");
        }
        if (p.getPrecio() == null || p.getPrecio() < 0) {
            throw ApiException.solicitudInvalida("El precio es obligatorio y no puede ser negativo");
        }
        if (p.getStock() == null || p.getStock() < 0) {
            throw ApiException.solicitudInvalida("El stock es obligatorio y no puede ser negativo");
        }
    }
}