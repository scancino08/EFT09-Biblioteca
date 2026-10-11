package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.reporte.Reporte;
import cl.duoc.biblioteca.reporte.ReporteHistorialEstudiante;
import cl.duoc.biblioteca.reporte.ReporteLibrosEnPrestamo;
import cl.duoc.biblioteca.reporte.ReporteLibrosMasPrestados;
import cl.duoc.biblioteca.reporte.ReportePrestamosAtrasados;

import java.util.List;

/** Controlador de reportes (solo bibliotecarios). */
public class ReporteController extends BaseController {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public ReporteController(Usuario usuarioActual) {
        super(usuarioActual);
    }

    public List<Reporte> reportesDisponibles() {
        return List.of(new ReporteLibrosMasPrestados(), new ReporteHistorialEstudiante(),
                new ReporteLibrosEnPrestamo(), new ReportePrestamosAtrasados());
    }

    public List<Estudiante> estudiantes() throws BibliotecaException {
        exigirBibliotecario();
        return estudianteDAO.listar();
    }

    public List<Object[]> generar(Reporte reporte, Estudiante estudiante) throws BibliotecaException {
        exigirBibliotecario();
        int id = estudiante == null ? 0 : estudiante.getId();
        if (reporte.requiereEstudiante() && estudiante == null) {
            throw new BibliotecaException("Seleccione un estudiante para generar este reporte.");
        }
        return reporte.generar(id);
    }
}
