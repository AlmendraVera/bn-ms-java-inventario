package cl.duoc.bn.inventario.service;

import cl.duoc.bn.inventario.exception.ApiException;
import cl.duoc.bn.inventario.model.Categoria;
import cl.duoc.bn.inventario.repository.CategoriaRepository;
import cl.duoc.bn.inventario.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe una categoría con id " + id));
    }

    public Categoria crear(Categoria categoria) {
        validar(categoria);
        String nombre = categoria.getNombre().trim();
        if (categoriaRepository.existsByNombre(nombre)) {
            throw ApiException.conflicto("Ya existe una categoría con el nombre " + nombre);
        }
        categoria.setId(null);
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Long id, Categoria datos) {
        validar(datos);
        Categoria categoria = buscarPorId(id);
        String nombre = datos.getNombre().trim();
        categoriaRepository.findByNombre(nombre).ifPresent(otra -> {
            if (!otra.getId().equals(id)) {
                throw ApiException.conflicto("Ya existe una categoría con el nombre " + nombre);
            }
        });
        categoria.setNombre(nombre);
        categoria.setDescripcion(datos.getDescripcion());
        return categoriaRepository.save(categoria);
    }

    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        if (productoRepository.countByCategoriaId(id) > 0) {
            throw ApiException.conflicto("No se puede eliminar la categoría " + categoria.getNombre()
                    + " porque tiene productos asociados");
        }
        categoriaRepository.delete(categoria);
    }

    private void validar(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw ApiException.solicitudInvalida("El nombre de la categoría es obligatorio");
        }
    }
}