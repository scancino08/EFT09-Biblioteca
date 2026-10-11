package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.concurrency.TareaSegundoPlano;
import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Prestamo;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.service.PrestamoService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador de préstamos y devoluciones. El bibliotecario opera sobre cualquier estudiante;
 * un estudiante solo sobre sí mismo. Las operaciones que modifican datos se ejecutan en un hilo
 * independiente para no bloquear la interfaz.
 */
public class PrestamoController extends BaseController {

    private final Estudiante estudianteActual; // null si el usuario es bibliotecario
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final PrestamoService servicio = PrestamoService.getInstance();

    public PrestamoController(Usuario usuarioActual, Estudiante estudianteActual) {
        super(usuarioActual);
        this.estudianteActual = estudianteActual;
    }

    /** Estudiantes a los que se puede prestar: todos (bibliotecario) o solo uno mismo. */
    public List<Estudiante> estudiantesDisponibles() throws BibliotecaException {
        if (usuarioActual.esBibliotecario()) {
            return estudianteDAO.listar();
        }
        List<Estudiante> propio = new ArrayList<>();
        propio.add(estudianteActual);
        return propio;
    }

    public List<Libro> libros() throws BibliotecaException {
        return libroDAO.listar();
    }

    /** Todos los préstamos (bibliotecario) o solo los del estudiante (historial propio). */
    public List<Prestamo> listarPrestamos() throws BibliotecaException {
        return usuarioActual.esBibliotecario()
                ? prestamoDAO.listar()
                : prestamoDAO.listarPorEstudiante(estudianteActual.getId());
    }

    /** Registra un préstamo en un hilo independiente; el resultado vuelve al hilo de la interfaz. */
    public void registrarPrestamoAsync(int idEstudiante, int idLibro,
                                       Consumer<Prestamo> alExito, Consumer<String> alError) {
        TareaSegundoPlano<Prestamo> tarea = new TareaSegundoPlano<>(() -> {
            validarEstudiante(idEstudiante);
            return servicio.registrarPrestamo(idEstudiante, idLibro);
        }, alExito, alError);
        TareaSegundoPlano.lanzar("hilo-prestamo", tarea);
    }

    /** Registra una devolución en un hilo independiente. Devuelve los días de atraso (0 = a tiempo). */
    public void registrarDevolucionAsync(Prestamo prestamo, Consumer<Integer> alExito, Consumer<String> alError) {
        TareaSegundoPlano<Integer> tarea = new TareaSegundoPlano<>(() -> {
            validarEstudiante(prestamo.getIdEstudiante());
            return servicio.registrarDevolucion(prestamo.getId());
        }, alExito, alError);
        TareaSegundoPlano.lanzar("hilo-devolucion", tarea);
    }

    private void validarEstudiante(int idEstudiante) throws BibliotecaException {
        if (!usuarioActual.esBibliotecario()
                && (estudianteActual == null || estudianteActual.getId() != idEstudiante)) {
            throw new BibliotecaException("Un estudiante solo puede operar con sus propios préstamos.");
        }
    }
}
