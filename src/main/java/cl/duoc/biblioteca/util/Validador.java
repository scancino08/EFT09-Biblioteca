package cl.duoc.biblioteca.util;

import cl.duoc.biblioteca.exception.BibliotecaException;

import java.util.regex.Pattern;

/** Validaciones de formulario reutilizadas por los controladores. */
public final class Validador {

    private static final Pattern RUT = Pattern.compile("^\\d{7,8}-[\\dkK]$");
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern ISBN = Pattern.compile("^[0-9Xx-]{10,20}$");

    private Validador() {
    }

    public static String obligatorio(String valor, String campo) throws BibliotecaException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new BibliotecaException("El campo \"" + campo + "\" es obligatorio.");
        }
        return valor.trim();
    }

    public static String rut(String valor) throws BibliotecaException {
        String v = obligatorio(valor, "RUT");
        if (!RUT.matcher(v).matches()) {
            throw new BibliotecaException("El RUT debe tener el formato 12345678-9 (sin puntos y con guion).");
        }
        return v;
    }

    public static String correo(String valor) throws BibliotecaException {
        String v = obligatorio(valor, "Correo");
        if (!CORREO.matcher(v).matches()) {
            throw new BibliotecaException("El correo no tiene un formato válido.");
        }
        return v;
    }

    public static String isbn(String valor) throws BibliotecaException {
        String v = obligatorio(valor, "ISBN");
        if (!ISBN.matcher(v).matches()) {
            throw new BibliotecaException("El ISBN debe tener entre 10 y 20 caracteres (dígitos o guiones).");
        }
        return v;
    }

    public static int enteroNoNegativo(String valor, String campo) throws BibliotecaException {
        String v = obligatorio(valor, campo);
        try {
            int n = Integer.parseInt(v);
            if (n < 0) {
                throw new BibliotecaException("El campo \"" + campo + "\" no puede ser negativo.");
            }
            return n;
        } catch (NumberFormatException e) {
            throw new BibliotecaException("El campo \"" + campo + "\" debe ser un número entero.");
        }
    }
}
