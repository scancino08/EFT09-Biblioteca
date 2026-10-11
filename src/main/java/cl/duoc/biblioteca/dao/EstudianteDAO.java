package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla estudiantes. */
public class EstudianteDAO extends BaseDAO implements CrudDAO<Estudiante> {

    private static final String COLUMNAS = "id, nombre, rut, curso, correo";

    private Estudiante mapear(ResultSet rs) throws java.sql.SQLException {
        return new Estudiante(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("curso"), rs.getString("correo"));
    }

    @Override
    public void insertar(Estudiante e) throws BibliotecaException {
        ejecutar(con -> {
            String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, e.getNombre());
                ps.setString(2, e.getRut());
                ps.setString(3, e.getCurso());
                ps.setString(4, e.getCorreo());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        e.setId(keys.getInt(1));
                    }
                }
                return null;
            }
        });
    }

    @Override
    public Optional<Estudiante> buscarPorId(int id) throws BibliotecaException {
        return ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT " + COLUMNAS + " FROM estudiantes WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(mapear(rs)) : Optional.<Estudiante>empty();
                }
            }
        });
    }

    public Optional<Estudiante> buscarPorRut(String rut) throws BibliotecaException {
        return ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT " + COLUMNAS + " FROM estudiantes WHERE rut = ?")) {
                ps.setString(1, rut);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(mapear(rs)) : Optional.<Estudiante>empty();
                }
            }
        });
    }

    @Override
    public List<Estudiante> listar() throws BibliotecaException {
        return ejecutar(con -> {
            List<Estudiante> lista = new ArrayList<>();
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT " + COLUMNAS + " FROM estudiantes ORDER BY nombre")) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        });
    }

    @Override
    public void actualizar(Estudiante e) throws BibliotecaException {
        ejecutar(con -> {
            String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, e.getNombre());
                ps.setString(2, e.getRut());
                ps.setString(3, e.getCurso());
                ps.setString(4, e.getCorreo());
                ps.setInt(5, e.getId());
                ps.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void eliminar(int id) throws BibliotecaException {
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM estudiantes WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                return null;
            }
        });
    }
}
