package cl.duoc.bn.inventario.controller;

import cl.duoc.bn.inventario.model.Producto;
import cl.duoc.bn.inventario.service.ProductoService;
import cl.duoc.bn.inventario.service.ProductoService.Disponibilidad;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    /** Cuerpo de las operaciones de stock: {"cantidad": 3} */
    public record CantidadRequest(Integer cantidad) {
    }

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /** GET /api/productos  o  GET /api/productos?categoriaId=1 */
    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) Long categoriaId) {
        return productoService.listar(categoriaId);
    }

    @GetMapping("/{id}")
    public Producto buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    @GetMapping("/sku/{sku}")
    public Producto buscarPorSku(@PathVariable String sku) {
        return productoService.buscarPorSku(sku.toUpperCase());
    }

    /** Valida stock: GET /api/productos/sku/AUD-001/disponibilidad?cantidad=2 */
    @GetMapping("/sku/{sku}/disponibilidad")
    public Disponibilidad disponibilidad(@PathVariable String sku, @RequestParam int cantidad) {
        return productoService.consultarDisponibilidad(sku, cantidad);
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        Producto creado = productoService.crear(producto);
        return ResponseEntity.created(URI.create("/api/productos/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /** Descuenta stock (lo usa picking-despacho). */
    @PostMapping("/sku/{sku}/descontar")
    public Producto descontar(@PathVariable String sku, @RequestBody CantidadRequest peticion) {
        return productoService.descontar(sku, peticion.cantidad());
    }

    @PostMapping("/sku/{sku}/reponer")
    public Producto reponer(@PathVariable String sku, @RequestBody CantidadRequest peticion) {
        return productoService.reponer(sku, peticion.cantidad());
    }
}