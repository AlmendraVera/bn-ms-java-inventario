package cl.duoc.bn.inventario.config;

import cl.duoc.bn.inventario.model.Categoria;
import cl.duoc.bn.inventario.model.Producto;
import cl.duoc.bn.inventario.repository.CategoriaRepository;
import cl.duoc.bn.inventario.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner cargarDatos(CategoriaRepository categorias, ProductoRepository productos) {
        return args -> {
            if (categorias.count() > 0) {
                return;
            }
            Categoria ropa = new Categoria();
            ropa.setNombre("Ropa");
            Categoria tecnologia = new Categoria();
            tecnologia.setNombre("Tecnología");
            Categoria hogar = new Categoria();
            hogar.setNombre("Hogar");
            categorias.save(ropa);
            categorias.save(tecnologia);
            categorias.save(hogar);

            crear(productos, "POL-001", "Polera básica", 10, ropa);
            crear(productos, "ZAP-001", "Zapatillas urbanas", 25, ropa);
            crear(productos, "AUD-001", "Audífonos bluetooth", 40, tecnologia);
            crear(productos, "CAR-001", "Cargador USB-C", 60, tecnologia);
            crear(productos, "LAM-001", "Lámpara de escritorio", 15, hogar);
        };
    }

    private void crear(ProductoRepository repo, String sku, String nombre, int stock, Categoria categoria) {
        Producto p = new Producto();
        p.setSku(sku);
        p.setNombre(nombre);
        p.setStock(stock);
        p.setCategoria(categoria);
        repo.save(p);
    }
}