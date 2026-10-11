package cl.duoc.biblioteca.model;

/** Roles del sistema. El valor de BD es el del ENUM de la tabla usuarios. */
public enum Rol {
    BIBLIOTECARIO("bibliotecario", "Bibliotecario"),
    ESTUDIANTE("estudiante", "Estudiante");

    private final String valorBd;
    private final String etiqueta;

    Rol(String valorBd, String etiqueta) {
        this.valorBd = valorBd;
        this.etiqueta = etiqueta;
    }

    public String getValorBd() {
        return valorBd;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Rol desdeBd(String valor) {
        for (Rol r : values()) {
            if (r.valorBd.equalsIgnoreCase(valor)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + valor);
    }
}
