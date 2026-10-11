package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.UsuarioDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Rol;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.util.DatabaseConnection;
import cl.duoc.biblioteca.util.Validador;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de la gestión de estudiantes. Registrar un estudiante también crea su usuario de
 * acceso (rol estudiante), vinculado por RUT, dentro de una misma transacción.
 */
public class EstudianteController extends BaseController {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public EstudianteController(Usuario usuarioActual) {
        super(usuarioActual);
    }

    public List<Estudiante> listar() throws BibliotecaException {
        exigirBibliotecario();
        return estudianteDAO.listar();
    }

    /**
     * Crea (id = 0) o modifica un estudiante y su usuario de acceso.
     * En una modificación, una contraseña vacía conserva la actual.
     */
    public void guardar(Estudiante estudiante, String nombre, String rut, String curso,
                        String correo, String contrasena) throws BibliotecaException {
        exigirBibliotecario();
        String rutAnterior = estudiante.getRut();
        estudiante.setNombre(Validador.obligatorio(nombre, "Nombre"));
        estudiante.setRut(Validador.rut(rut));
        estudiante.setCurso(Validador.obligatorio(curso, "Curso"));
        estudiante.setCorreo(Validador.correo(correo));
        boolean nuevo = estudiante.getId() == 0;
        String clave = contrasena == null ? "" : contrasena;
        if (nuevo && clave.isEmpty()) {
            throw new BibliotecaException("Debe asignar una contraseña inicial al nuevo estudiante.");
        }

        db.enTransaccion(() -> {
            if (nuevo) {
                estudianteDAO.insertar(estudiante);
                usuarioDAO.insertar(new Usuario(0, estudiante.getNombre(), estudiante.getRut(),
                        estudiante.getCorreo(), clave, Rol.ESTUDIANTE));
            } else {
                estudianteDAO.actualizar(estudiante);
                Optional<Usuario> existente = usuarioDAO.buscarPorRut(rutAnterior);
                if (existente.isPresent()) {
                    Usuario u = existente.get();
                    u.setNombre(estudiante.getNombre());
                    u.setRut(estudiante.getRut());
                    u.setCorreo(estudiante.getCorreo());
                    if (!clave.isEmpty()) {
                        u.setContrasena(clave);
                    }
                    usuarioDAO.actualizar(u);
                } else if (!clave.isEmpty()) {
                    usuarioDAO.insertar(new Usuario(0, estudiante.getNombre(), estudiante.getRut(),
                            estudiante.getCorreo(), clave, Rol.ESTUDIANTE));
                }
            }
            return null;
        });
    }

    public void eliminar(Estudiante estudiante) throws BibliotecaException {
        exigirBibliotecario();
        db.enTransaccion(() -> {
            estudianteDAO.eliminar(estudiante.getId());
            Optional<Usuario> u = usuarioDAO.buscarPorRut(estudiante.getRut());
            if (u.isPresent() && u.get().getRol() == Rol.ESTUDIANTE) {
                usuarioDAO.eliminar(u.get().getId());
            }
            return null;
        });
    }
}
