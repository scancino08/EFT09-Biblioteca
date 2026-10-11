package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.exception.BibliotecaException;

import java.util.List;
import java.util.Optional;

/** Contrato CRUD que implementan todos los DAO (patrón DAO). */
public interface CrudDAO<T> {

    /** Inserta la entidad y le asigna el id generado por la BD. */
    void insertar(T entidad) throws BibliotecaException;

    Optional<T> buscarPorId(int id) throws BibliotecaException;

    List<T> listar() throws BibliotecaException;

    void actualizar(T entidad) throws BibliotecaException;

    void eliminar(int id) throws BibliotecaException;
}
