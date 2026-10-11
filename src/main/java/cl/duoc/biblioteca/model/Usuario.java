package cl.duoc.biblioteca.model;

/** Cuenta de acceso al sistema (tabla usuarios). Hereda nombre, RUT y correo de Persona. */
public class Usuario extends Persona {

    private String contrasena;
    private Rol rol;

    public Usuario() {
    }

    public Usuario(int id, String nombre, String rut, String correo, String contrasena, Rol rol) {
        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
        this.rol = rol;
    }

    @Override
    public String getTipo() {
        return rol == null ? "Usuario" : rol.getEtiqueta();
    }

    public boolean esBibliotecario() {
        return rol == Rol.BIBLIOTECARIO;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
