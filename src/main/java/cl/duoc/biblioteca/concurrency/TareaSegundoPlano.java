package cl.duoc.biblioteca.concurrency;

import javax.swing.SwingUtilities;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Tarea ejecutable en un hilo independiente (Runnable). Hace el trabajo pesado fuera del hilo
 * de la interfaz (EDT) y, al terminar, devuelve el resultado o el error al EDT con
 * {@link SwingUtilities#invokeLater(Runnable)}, para que la ventana nunca se congele.
 */
public class TareaSegundoPlano<T> implements Runnable {

    private final Callable<T> trabajo;
    private final Consumer<T> alExito;
    private final Consumer<String> alError;

    public TareaSegundoPlano(Callable<T> trabajo, Consumer<T> alExito, Consumer<String> alError) {
        this.trabajo = trabajo;
        this.alExito = alExito;
        this.alError = alError;
    }

    @Override
    public void run() {
        try {
            T resultado = trabajo.call();
            SwingUtilities.invokeLater(() -> alExito.accept(resultado));
        } catch (Exception e) {
            String mensaje = e.getMessage() != null ? e.getMessage() : e.toString();
            SwingUtilities.invokeLater(() -> alError.accept(mensaje));
        }
    }

    /** Crea y lanza el hilo con un nombre descriptivo. */
    public static Thread lanzar(String nombreHilo, Runnable tarea) {
        Thread hilo = new Thread(tarea, nombreHilo);
        hilo.setDaemon(true);
        hilo.start();
        return hilo;
    }
}
