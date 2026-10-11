package cl.duoc.biblioteca.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Carga la configuración de la aplicación. Orden de prioridad (de mayor a menor):
 * variables de entorno (DB_URL, DB_USER, DB_PASSWORD) &gt; archivo db.properties junto al
 * ejecutable &gt; db.properties incluido dentro del jar.
 */
public final class Config {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Config.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException ignorado) {
            // se usan los valores por defecto
        }
        Path externo = Path.of(System.getProperty("db.config", "db.properties"));
        if (Files.isRegularFile(externo)) {
            try (InputStream in = Files.newInputStream(externo)) {
                PROPS.load(in);
            } catch (IOException ignorado) {
                // se mantiene la configuración interna
            }
        }
    }

    private Config() {
    }

    public static String dbUrl() {
        return desdeEntorno("DB_URL", PROPS.getProperty("db.url",
                "jdbc:mysql://localhost:3306/biblioteca?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8"));
    }

    public static String dbUser() {
        return desdeEntorno("DB_USER", PROPS.getProperty("db.user", "root"));
    }

    public static String dbPassword() {
        return desdeEntorno("DB_PASSWORD", PROPS.getProperty("db.password", ""));
    }

    public static int diasPrestamo() {
        return entero("prestamo.dias", 7);
    }

    public static int maxPrestamosActivos() {
        return entero("prestamo.maxActivosPorEstudiante", 3);
    }

    private static String desdeEntorno(String variable, String porDefecto) {
        String valor = System.getenv(variable);
        return (valor != null && !valor.isEmpty()) ? valor : porDefecto;
    }

    private static int entero(String clave, int porDefecto) {
        try {
            return Integer.parseInt(PROPS.getProperty(clave, String.valueOf(porDefecto)).trim());
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }
}
