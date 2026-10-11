package cl.duoc.biblioteca.model;

/** Contrato de todo material que se puede prestar y devolver. */
public interface Prestable {

    boolean estaDisponible();

    /** Descuenta una unidad del stock. */
    void registrarPrestamo();

    /** Devuelve una unidad al stock. */
    void registrarDevolucion();
}
