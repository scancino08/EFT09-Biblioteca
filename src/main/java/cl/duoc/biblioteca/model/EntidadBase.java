package cl.duoc.biblioteca.model;

/**
 * Clase abstracta raíz de la jerarquía del modelo: toda entidad persistente tiene un id
 * y debe saber describirse a sí misma (método abstracto, resuelto por polimorfismo).
 */
public abstract class EntidadBase {

    protected int id;

    protected EntidadBase() {
    }

    protected EntidadBase(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /** Texto corto que identifica a la entidad (se usa en listas desplegables y mensajes). */
    public abstract String descripcion();

    @Override
    public String toString() {
        return descripcion();
    }
}
