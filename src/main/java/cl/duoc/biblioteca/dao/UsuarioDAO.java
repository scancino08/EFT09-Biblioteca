package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Rol;
import cl.duoc.biblioteca.model.Usuario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla usuarios. */
public class UsuarioDAO extends BaseDAO implements CrudDAO<Usuario> {

    private static final String COLUMNAS = "id, nombre, rut, correo, `contraseña`, rol";

    private Usuario mapear(ResultSet rs) throws java.sql.SQLException {
        // Se lee por posición (orden de COLUMNAS) para no depender de la codificación del nombre "contraseña".
        return new Usuario(rs.getInt(1), rs.getString(2), rs.getString(3),
                rs.getString(4), rs.getString(5), Rol.desdeBd(rs.getString(6)));
    }

    /**
     * Valida credenciales. Se puede ingresar con correo o RUT. La comparación de la contraseña
     * usa BINARY para distinguir mayúsculas de minúsculas.
     */
    public Optional<Usuario> autenticar(String identificador, String contrasena) throws BibliotecaException {
        return ejecutar(con -> {
            String sql = "SELECT " + COLUMNAS + " FROM usuarios WHERE (correo = ? OR rut = ?) AND BINARY `contraseña` = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, identificador);
                ps.setString(2, identificador);
                ps.setString(3, contrasena);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(mapear(rs)) : Optional.<Usuario>empty();
                }
            }
        });
    }

    public Optional<Usuario> buscarPorRut(String rut) throws BibliotecaException {
        return ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT " + COLUMNAS + " FROM usuarios WHERE rut = ?")) {
                ps.setString(1, rut);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(mapear(rs)) : Optional.<Usuario>empty();
                }
            }
        });
    }

    @Override
    public void insertar(Usuario u) throws BibliotecaException {
        ejecutar(con -> {
            String sql = "INSERT INTO usuarios (nombre, rut, correo, `contraseña`, rol) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, u.getNombre());
                ps.setString(2, u.getRut());
                ps.setString(3, u.getCorreo());
                ps.setString(4, u.getContrasena());
                ps.setString(5, u.getRol().getValorBd());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        u.setId(keys.getInt(1));
                    }
                }
                return null;
            }
        });
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) throws BibliotecaException {
        return ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT " + COLUMNAS + " FROM usuarios WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(mapear(rs)) : Optional.<Usuario>empty();
                }
            }
        });
    }

    @Override
    public List<Usuario> listar() throws BibliotecaException {
        return ejecutar(con -> {
            List<Usuario> lista = new ArrayList<>();
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT " + COLUMNAS + " FROM usuarios ORDER BY nombre")) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        });
    }

    @Override
    public void actualizar(Usuario u) throws BibliotecaException {
        ejecutar(con -> {
            String sql = "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, `contraseña` = ?, rol = ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, u.getNombre());
                ps.setString(2, u.getRut());
                ps.setString(3, u.getCorreo());
                ps.setString(4, u.getContrasena());
                ps.setString(5, u.getRol().getValorBd());
                ps.setInt(6, u.getId());
                ps.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void eliminar(int id) throws BibliotecaException {
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                return null;
            }
        });
    }
}
