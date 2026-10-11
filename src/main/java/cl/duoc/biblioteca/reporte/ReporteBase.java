package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.dao.ReporteDAO;
import cl.duoc.biblioteca.model.Prestamo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Clase abstracta con lo común a los reportes: nombre, acceso a datos y formato de fechas. */
public abstract class ReporteBase implements Reporte {

    protected static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    protected final PrestamoDAO prestamoDAO = new PrestamoDAO();
    protected final ReporteDAO reporteDAO = new ReporteDAO();
    private final String nombre;

    protected ReporteBase(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean requiereEstudiante() {
        return false;
    }

    @Override
    public String toString() {
        return nombre;
    }

    /** Convierte préstamos en filas: libro, estudiante, préstamo, vencimiento, estado y días de atraso. */
    protected List<Object[]> filasDePrestamos(List<Prestamo> prestamos) {
        LocalDate hoy = LocalDate.now();
        List<Object[]> filas = new ArrayList<>();
        for (Prestamo p : prestamos) {
            filas.add(new Object[]{p.getLibroTitulo(), p.getEstudianteNombre(),
                    p.getFechaPrestamo().format(FECHA), p.getFechaVencimiento().format(FECHA),
                    p.getEstado(hoy), p.diasAtraso(hoy)});
        }
        return filas;
    }

    protected static final String[] COLUMNAS_PRESTAMOS =
            {"Libro", "Estudiante", "Fecha préstamo", "Vence", "Estado", "Días de atraso"};
}
