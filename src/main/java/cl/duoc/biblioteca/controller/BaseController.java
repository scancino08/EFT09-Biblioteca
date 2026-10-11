package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Usuario;

/**
 * Clase abstracta base de los controladores (MVC). Conoce al usuario con sesión iniciada para
 * hacer cumplir los permisos por rol también en la capa de control, no solo en la interfaz.
 */
public abstract class BaseController {

    protected final Usuario usuarioActual;

    protected BaseController(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    protected void exigirBibliotecario() throws BibliotecaException {
        if (usuarioActual == null || !usuarioActual.esBibliotecario()) {
            throw new BibliotecaException("Esta operación está permitida solo para el rol bibliotecario.");
        }
    }
}
