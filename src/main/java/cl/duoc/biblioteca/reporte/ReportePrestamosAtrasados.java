package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Prestamo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportePrestamosAtrasados extends ReporteBase {

    public ReportePrestamosAtrasados() {
        super("Préstamos atrasados");
    }

    @Override
    public String[] getColumnas() {
        return COLUMNAS_PRESTAMOS;
    }

    @Override
    public List<Object[]> generar(int idEstudiante) throws BibliotecaException {
        LocalDate hoy = LocalDate.now();
        List<Prestamo> atrasados = new ArrayList<>();
        for (Prestamo p : prestamoDAO.listarActivos()) {
            if (p.estaAtrasado(hoy)) {
                atrasados.add(p);
            }
        }
        return filasDePrestamos(atrasados);
    }
}
