# bn-ms-java-inventario

Microservicio de **catálogo y control de stock** (Capa 3). Lo consultan *órdenes* (para validar stock y obtener el precio) y *picking-despacho* (para descontar stock al despachar).

> Proyecto de la **Evaluación Parcial 2 (JVY0101)**. Forma parte de la arquitectura de 4 microservicios: usuarios, inventario, órdenes y picking-despacho.

## Modelo de datos

```
Categoria 1 ───── N Producto        (@OneToMany en Categoria / @ManyToOne en Producto)
```

- **Categoria**: `id`, `nombre` (único), `descripcion`
- **Producto**: `id`, `sku` (único), `nombre`, `precio` (CLP), `stock`, `categoria`

## Datos de ejemplo (se cargan al iniciar)

| SKU | Producto | Precio | Stock |
|---|---|---|---|
| AUD-001 | Audífonos Bluetooth | 19.990 | 50 |
| CAR-002 | Cargador USB-C 30W | 9.990 | 120 |
| LAM-003 | Lámpara LED de escritorio | 14.990 | 30 |
| POL-004 | Polerón canguro | 24.990 | 15 |
| TAZ-005 | Set de tazas cerámicas | 12.990 | **0** (sin stock a propósito, para probar el error) |

## Endpoints

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/productos` | Lista productos (`?categoriaId=1` para filtrar) | 200 |
| GET | `/api/productos/{id}` | Busca por id | 200, 404 |
| GET | `/api/productos/sku/{sku}` | Busca por SKU | 200, 404 |
| GET | `/api/productos/sku/{sku}/disponibilidad?cantidad=N` | Indica si hay stock suficiente | 200, 400, 404 |
| POST | `/api/productos` | Crea un producto | 201, 400, 404, 409 |
| PUT | `/api/productos/{id}` | Actualiza un producto | 200, 400, 404, 409 |
| DELETE | `/api/productos/{id}` | Elimina un producto | 204, 404 |
| POST | `/api/productos/sku/{sku}/descontar` | Descuenta stock (`{"cantidad": 2}`) | 200, 400, 404, 409 |
| POST | `/api/productos/sku/{sku}/reponer` | Suma stock (`{"cantidad": 10}`) | 200, 400, 404 |
| GET | `/api/categorias` | Lista categorías | 200 |
| GET | `/api/categorias/{id}` | Busca una categoría | 200, 404 |
| POST | `/api/categorias` | Crea una categoría | 201, 400, 409 |
| PUT | `/api/categorias/{id}` | Actualiza una categoría | 200, 400, 404, 409 |
| DELETE | `/api/categorias/{id}` | Elimina una categoría (409 si tiene productos) | 204, 404, 409 |

### Ejemplos

```bash
curl localhost:8082/api/productos/sku/AUD-001/disponibilidad?cantidad=2

curl -X POST localhost:8082/api/productos -H "Content-Type: application/json" \
  -d '{"sku":"MOU-006","nombre":"Mouse inalámbrico","precio":8990,"stock":40,"categoria":{"id":1}}'
```

## Requisitos

- JDK 17 o superior (`java -version`)
- Git
- No hace falta instalar Maven: el proyecto incluye el *Maven Wrapper* (`mvnw` / `mvnw.cmd`)

## Cómo clonar, instalar dependencias y ejecutar

```bash
git clone https://github.com/AlmendraVera/bn-ms-java-inventario.git
cd bn-ms-java-inventario

# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Maven descarga las dependencias automáticamente la primera vez. Cuando termine verás
`Started Application` y el servicio quedará escuchando en **http://localhost:8082**.

Este servicio **no depende de otros microservicios**. Debe estar levantado antes que *ordenes* y *picking-despacho*.

## Base de datos

H2 en memoria (se crea al iniciar y se borra al detener). Consola web: **http://localhost:8082/h2-console**

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:inventariodb;DB_CLOSE_DELAY=-1` |
| User Name | `sa` |
| Password | *(vacía)* |

La configuración está en `src/main/resources/application.properties`.

## Compilar y empaquetar con Maven

```bash
./mvnw clean      # borra la carpeta target/
./mvnw install    # compila, ejecuta los tests e instala el jar en el repositorio local
./mvnw package    # genera target/bn-ms-java-inventario-0.0.1-SNAPSHOT.jar

java -jar target/bn-ms-java-inventario-0.0.1-SNAPSHOT.jar
```

## Estructura (arquitectura en capas)

```
src/main/java/cl/duoc/bn/inventario/
├── controller/   endpoints REST (reciben la petición y devuelven la respuesta HTTP)
├── service/      lógica de negocio y validaciones
├── repository/   acceso a datos (Spring Data JPA)
├── model/        entidades JPA
├── exception/    ApiException + ManejadorExcepciones (respuestas de error en JSON)
├── config/       DatosIniciales: carga categorías y productos de ejemplo
└── Application.java
```

## Formato de los errores

Todos los errores devuelven el mismo JSON:

```json
{
  "timestamp": "2026-10-15T21:03:08",
  "status": 404,
  "error": "Not Found",
  "mensaje": "No existe un recurso con id 99",
  "ruta": "/api/..."
}
```

| Código | Cuándo ocurre |
|---|---|
| 400 | Datos faltantes o inválidos, JSON mal formado, parámetro con tipo incorrecto |
| 404 | El recurso no existe |
| 409 | Conflicto: dato duplicado o acción no permitida en el estado actual |

## Integrantes

- *(Nombre y apellido del integrante 1)*
- *(Nombre y apellido del integrante 2)*
