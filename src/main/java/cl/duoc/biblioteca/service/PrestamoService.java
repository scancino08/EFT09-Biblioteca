package cl.duoc.biblioteca.service;

import cl.duoc.biblioteca.dao.AtrasoDAO;
import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Prestamo;
import cl.duoc.biblioteca.util.Config;
import cl.duoc.biblioteca.util.DatabaseConnection;

import java.time.LocalDate;

/**
 * Lógica de negocio de préstamos y devoluciones.
 *
 * <p><b>Consistencia del stock:</b> los métodos que modifican el stock son
 * {@code synchronized}: aunque varios hilos pidan el mismo libro a la vez, solo uno a la vez
 * entra a la sección crítica (verificar → descontar stock → registrar préstamo). Además todo se
 * ejecuta en una transacción (todo o nada) y el descuento en SQL es condicional
 * ({@code stock > 0}), como segunda barrera.</p>
 *
 * <p>Se expone una única instancia compartida ({@link #getInstance()}) para que el candado de
 * {@code synchronized} sea el mismo para todos los hilos.</p>
 */
public class PrestamoService {

    private static final PrestamoService INSTANCIA = new PrestamoService();

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final AtrasoDAO atrasoDAO = new AtrasoDAO();
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private PrestamoService() {
    }

    public static PrestamoService getInstance() {
        return INSTANCIA;
    }

    /**
     * Registra un préstamo: valida, descuenta el stock y guarda el préstamo en el historial del
     * estudiante, todo en una sola transacción. Pensado para ejecutarse en un hilo aparte.
     */
    public synchronized Prestamo registrarPrestamo(int idEstudiante, int idLibro) throws BibliotecaException {
        return db.enTransaccion(() -> {
            Estudiante estudiante = estudianteDAO.buscarPorId(idEstudiante)
                    .orElseThrow(() -> new BibliotecaException("El estudiante seleccionado no existe."));
            Libro libro = libroDAO.buscarPorId(idLibro)
                    .orElseThrow(() -> new BibliotecaException("El libro seleccionado no existe."));

            if (!libro.estaDisponible()) {
                throw new BibliotecaException("No hay stock disponible de \"" + libro.getTitulo() + "\".");
            }
            if (prestamoDAO.tienePrestamoActivo(idEstudiante, idLibro)) {
                throw new BibliotecaException(estudiante.getNombre()
                        + " ya tiene un préstamo vigente de \"" + libro.getTitulo() + "\".");
            }
            int maximo = Config.maxPrestamosActivos();
            if (prestamoDAO.contarActivosDeEstudiante(idEstudiante) >= maximo) {
                throw new BibliotecaException(estudiante.getNombre()
                        + " ya alcanzó el máximo de " + maximo + " préstamos simultáneos.");
            }

            // Sección crítica: descontar stock (condicional en SQL) y registrar el préstamo.
            if (!libroDAO.descontarStock(idLibro)) {
                throw new BibliotecaException("No hay stock disponible de \"" + libro.getTitulo() + "\".");
            }
            LocalDate hoy = LocalDate.now();
            Prestamo prestamo = new Prestamo(idEstudiante, idLibro, hoy, hoy.plusDays(Config.diasPrestamo()));
            prestamo.setEstudianteNombre(estudiante.getNombre());
            prestamo.setLibroTitulo(libro.getTitulo());
            prestamoDAO.insertar(prestamo);
            return prestamo;
        });
    }

    /**
     * Registra la devolución: marca el préstamo como devuelto, repone el stock y, si hubo atraso,
     * deja constancia. Devuelve los días de atraso (0 si se devolvió a tiempo).
     */
    public synchronized int registrarDevolucion(int idPrestamo) throws BibliotecaException {
        return db.enTransaccion(() -> {
            Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo)
                    .orElseThrow(() -> new BibliotecaException("El préstamo seleccionado no existe."));
            if (prestamo.isDevuelto()) {
                throw new BibliotecaException("Este préstamo ya fue devuelto.");
            }
            int diasAtraso = prestamo.diasAtraso(LocalDate.now());
            if (!prestamoDAO.marcarDevuelto(idPrestamo)) {
                throw new BibliotecaException("Este préstamo ya fue devuelto.");
            }
            libroDAO.aumentarStock(prestamo.getIdLibro());
            if (diasAtraso > 0) {
                atrasoDAO.registrar(idPrestamo, diasAtraso);
            }
            return diasAtraso;
        });
    }
}
