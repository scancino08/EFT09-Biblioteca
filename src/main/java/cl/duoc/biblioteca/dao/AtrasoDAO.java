package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.exception.BibliotecaException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * Constancia de atrasos. Los scripts oficiales no tienen una tabla para esto, así que la
 * aplicación crea (si no existe) la tabla complementaria "atrasos" al iniciar.
 * No modifica ninguna de las 5 tablas oficiales.
 */
public class AtrasoDAO extends BaseDAO {

    public void crearTablaSiNoExiste() throws BibliotecaException {
        ejecutar(con -> {
            try (Statement st = con.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS atrasos ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "id_prestamo INT NOT NULL, "
                        + "dias_atraso INT NOT NULL, "
                        + "fecha_registro DATE NOT NULL, "
                        + "FOREIGN KEY (id_prestamo) REFERENCES prestamos(id) ON DELETE CASCADE)");
            }
            return null;
        });
    }

    /** Deja constancia de que un préstamo fue devuelto con atraso. */
    public void registrar(int idPrestamo, int diasAtraso) throws BibliotecaException {
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO atrasos (id_prestamo, dias_atraso, fecha_registro) VALUES (?, ?, ?)")) {
                ps.setInt(1, idPrestamo);
                ps.setInt(2, diasAtraso);
                ps.setDate(3, Date.valueOf(LocalDate.now()));
                ps.executeUpdate();
                return null;
            }
        });
    }
}
