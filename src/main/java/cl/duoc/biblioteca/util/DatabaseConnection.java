package cl.duoc.biblioteca.util;

import cl.duoc.biblioteca.exception.BibliotecaException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Patrón SINGLETON: garantiza una única instancia que administra una única conexión JDBC
 * a la base de datos MySQL.
 *
 * <p>Como varios hilos comparten esa única conexión (el hilo de la interfaz y los hilos de
 * préstamo), el acceso se protege con un {@link ReentrantLock}. Así, una transacción en curso
 * nunca se mezcla con sentencias de otro hilo.</p>
 */
public final class DatabaseConnection {

    private static volatile DatabaseConnection instancia;

    private final ReentrantLock lock = new ReentrantLock();
    private Connection conexion;

    private DatabaseConnection() {
        // constructor privado: nadie más puede crear instancias
    }

    /** Devuelve la única instancia (doble verificación + volatile = segura entre hilos). */
    public static DatabaseConnection getInstance() {
        if (instancia == null) {
            synchronized (DatabaseConnection.class) {
                if (instancia == null) {
                    instancia = new DatabaseConnection();
                }
            }
        }
        return instancia;
    }

    /** Entrega la conexión, (re)abriéndola con DriverManager.getConnection si hace falta. */
    public synchronized Connection getConnection() throws SQLException {
        if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
            conexion = DriverManager.getConnection(Config.dbUrl(), Config.dbUser(), Config.dbPassword());
        }
        return conexion;
    }

    /** Candado que deben tomar los DAO mientras usan la conexión compartida. */
    public ReentrantLock getLock() {
        return lock;
    }

    /**
     * Ejecuta un bloque dentro de una transacción (commit si termina bien, rollback si falla).
     * Es reentrante: los DAO invocados dentro del bloque reutilizan el mismo candado y conexión.
     */
    public <R> R enTransaccion(TxAction<R> accion) throws BibliotecaException {
        lock.lock();
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            try {
                R resultado = accion.ejecutar();
                con.commit();
                return resultado;
            } catch (BibliotecaException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BibliotecaException("Error de base de datos en la transacción: " + e.getMessage(), e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException ignorado) {
                // la conexión se revalidará en el próximo uso
            }
            lock.unlock();
        }
    }

    /** Cierra la conexión (al salir de la aplicación). */
    public synchronized void close() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException ignorado) {
                // nada que hacer al cerrar
            }
            conexion = null;
        }
    }
}
