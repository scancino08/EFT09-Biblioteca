package cl.duoc.biblioteca.util;

import cl.duoc.biblioteca.exception.BibliotecaException;

/** Bloque de trabajo que se ejecuta dentro de una transacción. */
@FunctionalInterface
public interface TxAction<R> {
    R ejecutar() throws BibliotecaException;
}
