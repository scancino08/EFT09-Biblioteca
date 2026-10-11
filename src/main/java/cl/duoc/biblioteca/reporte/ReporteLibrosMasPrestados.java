package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.exception.BibliotecaException;

import java.util.List;

public class ReporteLibrosMasPrestados extends ReporteBase {

    public ReporteLibrosMasPrestados() {
        super("Libros más prestados");
    }

    @Override
    public String[] getColumnas() {
        return new String[]{"Título", "Autor", "Categoría", "Veces prestado"};
    }

    @Override
    public List<Object[]> generar(int idEstudiante) throws BibliotecaException {
        return reporteDAO.librosMasPrestados();
    }
}
