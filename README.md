# Sistema de Gestión de Biblioteca Escolar

Aplicación de escritorio en **Java** (Swing/`JFrame`) con base de datos **MySQL** (JDBC) para controlar
libros, estudiantes, préstamos y devoluciones de una biblioteca escolar.
Evaluación Final Transversal – Desarrollo Orientado a Objetos II.

## Tecnologías

|Tema|Uso|
|-|-|
|Lenguaje / build|Java 17+, Maven (IntelliJ IDEA)|
|Interfaz|Swing (`JFrame`) con FlatLaf como look \& feel|
|Base de datos|MySQL 8 + MySQL Connector/J (`DriverManager.getConnection`)|
|Patrones|MVC, DAO, Singleton|
|Concurrencia|Hilo independiente para préstamos/devoluciones + `synchronized`|

## 1\. Base de datos

1. Inicia MySQL.
2. Ejecuta **en este orden** los scripts de la carpeta `sql/` (son los entregados en la evaluación, sin modificar):

   1. `Script\_crea\_tablas\_biblioteca.sql`
   2. `Script\_poblado\_tablas\_biblioteca.sql`
3. Ajusta la conexión en `src/main/resources/db.properties` (URL, usuario y contraseña de tu MySQL).
También puedes crear un `db.properties` junto al `.jar`, o usar las variables de entorno `DB\_URL`, `DB\_USER`, `DB\_PASSWORD`.

> La aplicación crea automáticamente al iniciar una tabla complementaria `atrasos` (si no existe) para dejar
> constancia de las devoluciones atrasadas. No modifica ninguna de las 5 tablas oficiales.

## 2\. Ejecutar

**Desde IntelliJ IDEA:** abrir la carpeta como proyecto Maven, esperar la descarga de dependencias y ejecutar `cl.duoc.biblioteca.App`.

**Como ejecutable (.jar):**

```bash
mvn clean package
java -jar target/biblioteca-escolar.jar
```

### Usuarios de prueba (del script de poblado)

|Rol|Correo (o RUT)|Contraseña|
|-|-|-|
|Bibliotecario|`antonia@correo.cl` (`12345678-9`)|`clave123`|
|Estudiante|`carlos@correo.cl` (`98765432-1`)|`clave123`|

Los demás estudiantes del script usan la misma contraseña. Los préstamos del script de poblado son de 2025, por lo que
los no devueltos aparecen como **atrasados** (útil para probar el control de atrasos).

## 3\. Funcionalidades

* **Autenticación:** ingreso por correo o RUT; el rol (`bibliotecario` / `estudiante`) habilita opciones distintas.
* **Libros** (bibliotecario): registrar, modificar, eliminar y buscar; cada libro tiene categoría, autor, editorial, ISBN y stock.
* **Categorías** (bibliotecario): CRUD completo.
* **Estudiantes** (bibliotecario): CRUD; al registrar un estudiante se crea también su usuario de acceso (misma transacción).
* **Préstamos y devoluciones:** verifica disponibilidad, calcula el vencimiento (7 días, configurable), detecta atrasos y deja constancia al devolver con atraso.
Reglas: máximo 3 préstamos simultáneos por estudiante y no se puede pedir dos veces el mismo libro mientras esté vigente.
* **Reportes** (bibliotecario): libros más prestados, historial por estudiante, libros actualmente en préstamo y préstamos atrasados.
* **Rol estudiante:** solo consulta el catálogo y gestiona **sus propios** préstamos y devoluciones (se valida también en el controlador, no solo en la interfaz).

## 4\. Arquitectura

```
cl.duoc.biblioteca
├── App                       punto de entrada
├── model/                    MODELO: EntidadBase (abstracta) → Persona (abstracta) → Usuario, Estudiante
│                             Categoria, Libro (implementa Prestable), Prestamo, Rol
├── dao/                      DAO: CrudDAO<T> (interfaz), BaseDAO (abstracta), un DAO por entidad
├── util/DatabaseConnection   SINGLETON de la conexión (+ candado y transacciones)
├── service/PrestamoService   lógica de préstamo/devolución: métodos synchronized + transacción
├── concurrency/              TareaSegundoPlano: Runnable que corre en un hilo aparte
├── reporte/                  Reporte (interfaz), ReporteBase (abstracta) y 4 reportes
├── controller/               CONTROLADORES: validan, controlan permisos y llaman a DAO/servicio
├── view/                     VISTAS Swing: LoginFrame, MainFrame (menú + navegación), paneles
└── pruebas/PruebaConcurrencia   prueba de consistencia del stock con hilos simultáneos
```

### Cómo se cumple cada requisito técnico

* **POO (herencia, polimorfismo, abstracción, interfaces):** `EntidadBase`/`Persona` son clases abstractas con
métodos abstractos (`descripcion()`, `getTipo()`) que cada subclase implementa; `Prestable`, `CrudDAO<T>` y `Reporte`
son interfaces; los reportes y DAO se usan de forma polimórfica.
* **MVC:** modelo (`model`, `dao`, `service`), vistas (`view`) y controladores (`controller`) separados; las vistas nunca acceden a la BD.
* **DAO:** cada tabla tiene su DAO con las operaciones CRUD; solo los DAO contienen SQL.
* **Singleton:** `DatabaseConnection.getInstance()` entrega la única instancia que administra la única conexión JDBC.
Como esa conexión la usan varios hilos, el acceso se protege con un `ReentrantLock`, de modo que una transacción nunca se mezcla con otra sentencia.
* **Hilo independiente:** registrar un préstamo (validar, descontar stock, guardar el préstamo en el historial del estudiante)
y registrar una devolución se ejecutan en un hilo aparte (`hilo-prestamo` / `hilo-devolucion`); al terminar, el resultado vuelve al
hilo de la interfaz con `SwingUtilities.invokeLater`. Durante la operación se ve una barra de progreso y la ventana no se congela.
* **Sincronización:** `PrestamoService.registrarPrestamo` y `registrarDevolucion` son `synchronized` sobre una única instancia compartida,
y además corren en una transacción (todo o nada). El descuento de stock es condicional en SQL (`... SET stock = stock - 1 WHERE id = ? AND stock > 0`)
como segunda barrera contra el stock negativo.
* **Conexión segura y con manejo de excepciones:** `BibliotecaException` traduce los errores de MySQL (duplicados, claves foráneas, etc.) a mensajes comprensibles.

## 

