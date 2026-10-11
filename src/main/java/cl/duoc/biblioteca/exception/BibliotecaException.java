package cl.duoc.biblioteca.exception;

/**
 * Excepción de negocio / acceso a datos de la aplicación.
 * Su mensaje está pensado para mostrarse directamente al usuario.
 */
public class BibliotecaException extends Exception {

    public BibliotecaException(String mensaje) {
        super(mensaje);
    }

    public BibliotecaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
