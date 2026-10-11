package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.exception.BibliotecaException;

import java.util.List;

/** Contrato de un reporte: cada tipo de reporte lo implementa a su manera (polimorfismo). */
public interface Reporte {

    String getNombre();

    String[] getColumnas();

    /** Indica si el reporte necesita elegir un estudiante. */
    boolean requiereEstudiante();

    /** Genera las filas del reporte. {@code idEstudiante} solo se usa si el reporte lo requiere. */
    List<Object[]> generar(int idEstudiante) throws BibliotecaException;
}
