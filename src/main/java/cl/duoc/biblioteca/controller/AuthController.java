package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.UsuarioDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Rol;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.util.Validador;

/** Controlador del módulo de autenticación. */
public class AuthController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    /** Valida las credenciales contra la base de datos y devuelve el usuario con su rol. */
    public Usuario iniciarSesion(String identificador, String contrasena) throws BibliotecaException {
        String id = Validador.obligatorio(identificador, "Correo o RUT");
        if (contrasena == null || contrasena.isEmpty()) {
            throw new BibliotecaException("Debe ingresar la contraseña.");
        }
        Usuario usuario = usuarioDAO.autenticar(id, contrasena)
                .orElseThrow(() -> new BibliotecaException("Usuario o contraseña incorrectos."));
        if (usuario.getRol() == Rol.ESTUDIANTE && ficha(usuario) == null) {
            throw new BibliotecaException("Su usuario no tiene una ficha de estudiante asociada. Consulte a la biblioteca.");
        }
        return usuario;
    }

    /** Ficha de estudiante asociada a un usuario con rol estudiante (se vinculan por RUT). */
    public Estudiante ficha(Usuario usuario) throws BibliotecaException {
        return estudianteDAO.buscarPorRut(usuario.getRut()).orElse(null);
    }
}
