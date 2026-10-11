package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Categoria;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla categorias. */
public class CategoriaDAO extends BaseDAO implements CrudDAO<Categoria> {

    @Override
    public void insertar(Categoria c) throws BibliotecaException {
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO categorias (nombre) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getNombre());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        c.setId(keys.getInt(1));
                    }
                }
                return null;
            }
        });
    }

    @Override
    public Optional<Categoria> buscarPorId(int id) throws BibliotecaException {
        return ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT id, nombre FROM categorias WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(new Categoria(rs.getInt("id"), rs.getString("nombre")))
                            : Optional.<Categoria>empty();
                }
            }
        });
    }

    @Override
    public List<Categoria> listar() throws BibliotecaException {
        return ejecutar(con -> {
            List<Categoria> lista = new ArrayList<>();
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, nombre FROM categorias ORDER BY nombre")) {
                while (rs.next()) {
                    lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
                }
            }
            return lista;
        });
    }

    @Override
    public void actualizar(Categoria c) throws BibliotecaException {
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("UPDATE categorias SET nombre = ? WHERE id = ?")) {
                ps.setString(1, c.getNombre());
                ps.setInt(2, c.getId());
                ps.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void eliminar(int id) throws BibliotecaException {
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM categorias WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                return null;
            }
        });
    }
}
