package cl.duoc.biblioteca.util;

import java.sql.Connection;
import java.sql.SQLException;

/** Operación JDBC que recibe la conexión compartida y devuelve un resultado. */
@FunctionalInterface
public interface SqlAction<R> {
    R ejecutar(Connection conexion) throws SQLException;
}
