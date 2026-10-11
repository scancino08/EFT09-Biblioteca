package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Prestamo;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla prestamos. */
public class PrestamoDAO extends BaseDAO implements CrudDAO<Prestamo> {

    private static final String SELECT =
            "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
                    + "e.nombre AS estudiante, l.titulo AS libro "
                    + "FROM prestamos p "
                    + "JOIN estudiantes e ON e.id = p.id_estudiante "
                    + "JOIN libros l ON l.id = p.id_libro ";

    private Prestamo mapear(ResultSet rs) throws java.sql.SQLException {
        Prestamo p = new Prestamo();
        p.setId(rs.getInt("id"));
        p.setIdEstudiante(rs.getInt("id_estudiante"));
        p.setIdLibro(rs.getInt("id_libro"));
        Date prestamo = rs.getDate("fecha_prestamo");
        Date vencimiento = rs.getDate("fecha_devolucion");
        p.setFechaPrestamo(prestamo == null ? null : prestamo.toLocalDate());
        p.setFechaVencimiento(vencimiento == null ? null : vencimiento.toLocalDate());
        p.setDevuelto(rs.getBoolean("devuelto"));
        p.setEstudianteNombre(rs.getString("estudiante"));
        p.setLibroTitulo(rs.getString("libro"));
        return p;
    }

    private List<Prestamo> consultar(String condicion, Object parametro) throws BibliotecaException {
        return ejecutar(con -> {
            List<Prestamo> lista = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(SELECT + condicion)) {
                if (parametro != null) {
                    ps.setObject(1, parametro);
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
    public void insertar(Prestamo p) throws BibliotecaException {
        ejecutar(con -> {
            String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, p.getIdEstudiante());
                ps.setInt(2, p.getIdLibro());
                ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
                ps.setDate(4, Date.valueOf(p.getFechaVencimiento()));
                ps.setBoolean(5, p.isDevuelto());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        p.setId(keys.getInt(1));
                    }
                }
                return null;
            }
        });
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) throws BibliotecaException {
        List<Prestamo> lista = consultar("WHERE p.id = ?", id);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    @Override
    public List<Prestamo> listar() throws BibliotecaException {
        return consultar("ORDER BY p.fecha_prestamo DESC, p.id DESC", null);
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws BibliotecaException {
        return consultar("WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC, p.id DESC", idEstudiante);
    }

    /** Préstamos que aún no han sido devueltos. */
    public List<Prestamo> listarActivos() throws BibliotecaException {
        return consultar("WHERE p.devuelto = FALSE ORDER BY p.fecha_devolucion, p.id", null);
    }

    @Override
    public void actualizar(Prestamo p) throws BibliotecaException {
        ejecutar(con -> {
            String sql = "UPDATE prestamos SET id_estudiante = ?, id_libro = ?, fecha_prestamo = ?, "
                    + "fecha_devolucion = ?, devuelto = ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, p.getIdEstudiante());
                ps.setInt(2, p.getIdLibro());
                ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
                ps.setDate(4, Date.valueOf(p.getFechaVencimiento()));
                ps.setBoolean(5, p.isDevuelto());
                ps.setInt(6, p.getId());
                ps.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void eliminar(int id) throws BibliotecaException {
        ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM prestamos WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                return null;
            }
        });
    }

    /** Marca el préstamo como devuelto solo si seguía pendiente. Devuelve true si cambió. */
    public boolean marcarDevuelto(int idPrestamo) throws BibliotecaException {
        return ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE prestamos SET devuelto = TRUE WHERE id = ? AND devuelto = FALSE")) {
                ps.setInt(1, idPrestamo);
                return ps.executeUpdate() == 1;
            }
        });
    }

    public int contarActivosDeEstudiante(int idEstudiante) throws BibliotecaException {
        return ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND devuelto = FALSE")) {
                ps.setInt(1, idEstudiante);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        });
    }

    public boolean tienePrestamoActivo(int idEstudiante, int idLibro) throws BibliotecaException {
        return ejecutar(con -> {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND id_libro = ? AND devuelto = FALSE")) {
                ps.setInt(1, idEstudiante);
                ps.setInt(2, idLibro);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        });
    }
}
