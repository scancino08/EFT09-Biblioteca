package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.util.DatabaseConnection;
import cl.duoc.biblioteca.util.SqlAction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clase abstracta base de los DAO: obtiene la conexión Singleton, toma el candado compartido
 * y traduce las SQLException a mensajes comprensibles para el usuario.
 */
public abstract class BaseDAO {

    protected final DatabaseConnection db = DatabaseConnection.getInstance();

    /** Ejecuta una acción JDBC con la conexión única, protegida por el candado. */
    protected <R> R ejecutar(SqlAction<R> accion) throws BibliotecaException {
        ReentrantLock lock = db.getLock();
        lock.lock();
        try {
            Connection con = db.getConnection();
            return accion.ejecutar(con);
        } catch (SQLException e) {
            throw traducir(e);
        } finally {
            lock.unlock();
        }
    }

    private BibliotecaException traducir(SQLException e) {
        switch (e.getErrorCode()) {
            case 1062:
                return new BibliotecaException("Ya existe un registro con ese dato único (RUT, ISBN, etc.).", e);
            case 1451:
                return new BibliotecaException(
                        "No se puede eliminar: el registro está siendo utilizado por otros datos (por ejemplo, préstamos o libros).", e);
            case 1452:
                return new BibliotecaException("El registro hace referencia a un dato que no existe.", e);
            default:
                return new BibliotecaException("Error de base de datos: " + e.getMessage(), e);
        }
    }
}
