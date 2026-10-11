package cl.duoc.biblioteca.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Préstamo de un libro a un estudiante (tabla prestamos).
 * En la BD la columna fecha_devolucion guarda la fecha en que el libro debe ser devuelto
 * (fecha de vencimiento), y la columna devuelto indica si ya fue devuelto.
 */
public class Prestamo extends EntidadBase {

    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaVencimiento;
    private boolean devuelto;
    /** Datos de apoyo (JOIN), solo para mostrar. */
    private String estudianteNombre;
    private String libroTitulo;

    public Prestamo() {
    }

    public Prestamo(int idEstudiante, int idLibro, LocalDate fechaPrestamo, LocalDate fechaVencimiento) {
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.devuelto = false;
    }

    /** Un préstamo está atrasado si no se ha devuelto y ya pasó su fecha de vencimiento. */
    public boolean estaAtrasado(LocalDate hoy) {
        return !devuelto && fechaVencimiento != null && hoy.isAfter(fechaVencimiento);
    }

    public int diasAtraso(LocalDate hoy) {
        return estaAtrasado(hoy) ? (int) ChronoUnit.DAYS.between(fechaVencimiento, hoy) : 0;
    }

    public String getEstado(LocalDate hoy) {
        if (devuelto) {
            return "Devuelto";
        }
        return estaAtrasado(hoy) ? "Atrasado" : "Vigente";
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public boolean isDevuelto() {
        return devuelto;
    }

    public void setDevuelto(boolean devuelto) {
        this.devuelto = devuelto;
    }

    public String getEstudianteNombre() {
        return estudianteNombre;
    }

    public void setEstudianteNombre(String estudianteNombre) {
        this.estudianteNombre = estudianteNombre;
    }

    public String getLibroTitulo() {
        return libroTitulo;
    }

    public void setLibroTitulo(String libroTitulo) {
        this.libroTitulo = libroTitulo;
    }

    @Override
    public String descripcion() {
        return "Préstamo #" + id + ": " + libroTitulo + " → " + estudianteNombre;
    }
}
