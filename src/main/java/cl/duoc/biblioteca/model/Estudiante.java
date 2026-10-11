package cl.duoc.biblioteca.model;

/** Estudiante de la biblioteca (tabla estudiantes). */
public class Estudiante extends Persona {

    private String curso;

    public Estudiante() {
    }

    public Estudiante(int id, String nombre, String rut, String curso, String correo) {
        super(id, nombre, rut, correo);
        this.curso = curso;
    }

    @Override
    public String getTipo() {
        return "Estudiante";
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    /** Sobrescribe la descripción de Persona (polimorfismo): se muestra en listas desplegables. */
    @Override
    public String descripcion() {
        return nombre + " (" + rut + ") - " + (curso == null ? "" : curso);
    }
}
