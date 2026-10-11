package cl.duoc.biblioteca;

import cl.duoc.biblioteca.dao.AtrasoDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.view.Estilos;
import cl.duoc.biblioteca.view.LoginFrame;

import javax.swing.SwingUtilities;

/** Punto de entrada de la aplicación. */
public class App {

    public static void main(String[] args) {
        Estilos.aplicarTema();

        // Verifica la conexión a la BD y prepara la tabla complementaria de atrasos.
        try {
            new AtrasoDAO().crearTablaSiNoExiste();
        } catch (BibliotecaException e) {
            SwingUtilities.invokeLater(() -> {
                LoginFrame.errorDeArranque(e.getMessage());
                System.exit(1);
            });
            return;
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
