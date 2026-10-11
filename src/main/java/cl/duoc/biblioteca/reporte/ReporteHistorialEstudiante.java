package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.exception.BibliotecaException;

import java.util.List;

public class ReporteHistorialEstudiante extends ReporteBase {

    public ReporteHistorialEstudiante() {
        super("Historial por estudiante");
    }

    @Override
    public String[] getColumnas() {
        return COLUMNAS_PRESTAMOS;
    }

    @Override
    public boolean requiereEstudiante() {
        return true;
    }

    @Override
    public List<Object[]> generar(int idEstudiante) throws BibliotecaException {
        return filasDePrestamos(prestamoDAO.listarPorEstudiante(idEstudiante));
    }
}
