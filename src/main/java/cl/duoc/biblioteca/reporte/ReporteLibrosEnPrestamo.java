package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.exception.BibliotecaException;

import java.util.List;

public class ReporteLibrosEnPrestamo extends ReporteBase {

    public ReporteLibrosEnPrestamo() {
        super("Libros actualmente en préstamo");
    }

    @Override
    public String[] getColumnas() {
        return COLUMNAS_PRESTAMOS;
    }

    @Override
    public List<Object[]> generar(int idEstudiante) throws BibliotecaException {
        return filasDePrestamos(prestamoDAO.listarActivos());
    }
}
