package cl.duoc.biblioteca.pruebas;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Prestamo;
import cl.duoc.biblioteca.service.PrestamoService;
import cl.duoc.biblioteca.util.DatabaseConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prueba de consistencia del stock (Paso 4 de la evaluación).
 *
 * <p>Crea un libro de prueba con 3 unidades y lanza un hilo por cada estudiante registrado,
 * todos intentando pedirlo al mismo tiempo. Si la sincronización funciona, exactamente 3 préstamos
 * deben tener éxito, el resto debe ser rechazado por falta de stock y el stock final debe ser 0
 * (nunca negativo). Al terminar, borra los datos de prueba.</p>
 *
 * <p>Ejecución: {@code java -cp biblioteca-escolar.jar cl.duoc.biblioteca.pruebas.PruebaConcurrencia}</p>
 */
public class PruebaConcurrencia {

    private static final int STOCK_INICIAL = 3;

    public static void main(String[] args) throws Exception {
        LibroDAO libroDAO = new LibroDAO();
        PrestamoDAO prestamoDAO = new PrestamoDAO();
        List<Estudiante> estudiantes = new EstudianteDAO().listar();

        Libro libro = new Libro(0, "LIBRO DE PRUEBA CONCURRENCIA", "Test", "0000000000-TEST", "Test",
                STOCK_INICIAL, 1);
        libroDAO.insertar(libro);
        System.out.println("Libro de prueba creado con stock = " + STOCK_INICIAL
                + "; hilos simultáneos = " + estudiantes.size());

        AtomicInteger exitosos = new AtomicInteger();
        AtomicInteger rechazados = new AtomicInteger();
        List<Integer> prestamosCreados = java.util.Collections.synchronizedList(new ArrayList<>());
        CountDownLatch salida = new CountDownLatch(1);
        List<Thread> hilos = new ArrayList<>();

        try {
            for (Estudiante e : estudiantes) {
                Thread t = new Thread(() -> {
                    try {
                        salida.await(); // todos esperan la señal y arrancan juntos
                        Prestamo p = PrestamoService.getInstance().registrarPrestamo(e.getId(), libro.getId());
                        prestamosCreados.add(p.getId());
                        exitosos.incrementAndGet();
                    } catch (BibliotecaException ex) {
                        rechazados.incrementAndGet();
                        System.out.println("  rechazado (" + e.getNombre() + "): " + ex.getMessage());
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }, "hilo-prueba-" + e.getId());
                hilos.add(t);
                t.start();
            }
            salida.countDown();
            for (Thread t : hilos) {
                t.join();
            }

            int stockFinal = libroDAO.buscarPorId(libro.getId()).orElseThrow().getStock();
            System.out.println();
            System.out.println("Préstamos exitosos : " + exitosos.get() + " (esperado " + Math.min(STOCK_INICIAL, estudiantes.size()) + ")");
            System.out.println("Préstamos rechazados: " + rechazados.get());
            System.out.println("Stock final        : " + stockFinal + " (esperado " + Math.max(0, STOCK_INICIAL - estudiantes.size()) + ")");
            boolean ok = exitosos.get() == Math.min(STOCK_INICIAL, estudiantes.size()) && stockFinal >= 0
                    && stockFinal == STOCK_INICIAL - exitosos.get();
            System.out.println(ok ? "RESULTADO: OK - el stock se mantuvo consistente."
                    : "RESULTADO: FALLÓ - hay inconsistencia de stock.");
            if (!ok) {
                System.exit(1);
            }
        } finally {
            for (int id : prestamosCreados) {
                prestamoDAO.eliminar(id);
            }
            libroDAO.eliminar(libro.getId());
            DatabaseConnection.getInstance().close();
        }
    }
}
