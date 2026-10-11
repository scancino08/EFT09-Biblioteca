package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.exception.BibliotecaException;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Consultas agregadas para los reportes. */
public class ReporteDAO extends BaseDAO {

    /** Los libros con más préstamos registrados (máximo 10). Cada fila: título, autor, categoría, veces. */
    public List<Object[]> librosMasPrestados() throws BibliotecaException {
        return ejecutar(con -> {
            List<Object[]> filas = new ArrayList<>();
            String sql = "SELECT l.titulo, l.autor, COALESCE(c.nombre, '-') AS categoria, COUNT(p.id) AS veces "
                    + "FROM libros l "
                    + "JOIN prestamos p ON p.id_libro = l.id "
                    + "LEFT JOIN categorias c ON c.id = l.id_categoria "
                    + "GROUP BY l.id, l.titulo, l.autor, c.nombre "
                    + "ORDER BY veces DESC, l.titulo "
                    + "LIMIT 10";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    filas.add(new Object[]{rs.getString("titulo"), rs.getString("autor"),
                            rs.getString("categoria"), rs.getInt("veces")});
                }
            }
            return filas;
        });
    }
}
