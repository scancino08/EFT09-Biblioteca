package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Libro;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla libros. */
public class LibroDAO extends BaseDAO implements CrudDAO<Libro> {

    private static final String SELECT =
            "SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, l.id_categoria, c.nombre AS categoria "
                    + "FROM libros l LEFT JOIN categorias c ON c.id = l.id_categoria ";

    private Libro mapear(ResultSet rs) throws java.sql.SQLException {
        Libro l = new Libro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"), rs.getString("isbn"),
                rs.getString("editorial"), rs.getInt("stock"), rs.getInt("id_categoria"));
        l.setCategoriaNombre(rs.getString("categoria"));
        return l;
    }

    @Override
    public void insertar(Libro l) throws BibliotecaException {
        ejecutar(con -> {
            String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, l.getTitulo());
                ps.setString(2, l.getAutor());
                ps.setString(3, l.getIsbn());
                ps.setString(4, l.getEditorial());
                ps.setInt(5, l.getStock());
                ps.setInt(6, l.getIdCategoria());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        l.setId(keys.getInt(1));
                    }
                }
                return null;
            }
        });
    }

    @Override
    public Optional<Libro> buscarPorId(int id) throws BibliotecaException {
        return ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE l.id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(mapear(rs)) : Optional.<Libro>empty();
                }
            }
        });
    }

    @Override
    public List<Libro> listar() throws BibliotecaException {
        return buscar("");
    }

    /** Busca por título, autor, ISBN o editorial (texto vacío = todos). */
    public List<Libro> buscar(String texto) throws BibliotecaException {
        return ejecutar(con -> {
            List<Libro> lista = new ArrayList<>();
            String sql = SELECT + "WHERE l.titulo LIKE ? OR l.autor LIKE ? OR l.isbn LIKE ? OR l.editorial LIKE ? "
                    + "ORDER BY l.titulo";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
                for (int i = 1; i <= 4; i++) {
                    ps.setString(i, patron);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapear(rs));
                    }
                }
            }
            return lista;
        });
    }

    @Override
    public void actualizar(Libro l) throws BibliotecaException {
        ejecutar(con -> {
            String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, stock = ?, id_categoria = ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, l.getTitulo());
                ps.setString(2, l.getAutor());
                ps.setString(3, l.getIsbn());
                ps.setString(4, l.getEditorial());
                ps.setInt(5, l.getStock());
                ps.setInt(6, l.getIdCategoria());
                ps.setInt(7, l.getId());
                ps.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void eliminar(int id) throws BibliotecaException {
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM libros WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                return null;
            }
        });
    }

    /**
     * Descuenta una unidad de stock solo si queda stock (condición atómica en SQL).
     * Devuelve true si se pudo descontar; es la segunda barrera contra el stock negativo,
     * además del synchronized del servicio.
     */
    public boolean descontarStock(int idLibro) throws BibliotecaException {
        return ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0")) {
                ps.setInt(1, idLibro);
                return ps.executeUpdate() == 1;
            }
        });
    }

    public void aumentarStock(int idLibro) throws BibliotecaException {
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("UPDATE libros SET stock = stock + 1 WHERE id = ?")) {
                ps.setInt(1, idLibro);
                ps.executeUpdate();
                return null;
            }
        });
    }
}
