package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.concurrency.TareaSegundoPlano;
import cl.duoc.biblioteca.controller.AuthController;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.util.DatabaseConnection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Pantalla de autenticación. La validación contra la BD se hace fuera del hilo de la interfaz. */
public class LoginFrame extends JFrame {

    private final AuthController controlador = new AuthController();
    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JLabel lblMensaje = new JLabel(" ");

    public LoginFrame() {
        super("Biblioteca Escolar - Ingreso");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseConnection.getInstance().close();
                dispose();
                System.exit(0);
            }
        });

        JPanel raiz = new JPanel();
        raiz.setLayout(new BoxLayout(raiz, BoxLayout.Y_AXIS));
        raiz.setBorder(BorderFactory.createEmptyBorder(32, 44, 28, 44));

        JLabel titulo = new JLabel("📚 Biblioteca Escolar");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 26f));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Ingresa con tu correo o RUT");
        sub.setForeground(Estilos.TEXTO_SUAVE);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        raiz.add(titulo);
        raiz.add(Box.createVerticalStrut(4));
        raiz.add(sub);
        raiz.add(Box.createVerticalStrut(24));

        raiz.add(etiqueta("Correo o RUT"));
        txtUsuario.putClientProperty("JTextField.placeholderText", "usuario@correo.cl");
        txtUsuario.setAlignmentX(LEFT_ALIGNMENT);
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        raiz.add(txtUsuario);
        raiz.add(Box.createVerticalStrut(14));

        raiz.add(etiqueta("Contraseña"));
        txtClave.putClientProperty("JPasswordField.showRevealButton", true);
        txtClave.setAlignmentX(LEFT_ALIGNMENT);
        txtClave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        raiz.add(txtClave);
        raiz.add(Box.createVerticalStrut(8));

        lblMensaje.setForeground(Estilos.PELIGRO);
        lblMensaje.setAlignmentX(LEFT_ALIGNMENT);
        raiz.add(lblMensaje);
        raiz.add(Box.createVerticalStrut(10));

        btnIngresar.setBackground(Estilos.PRIMARIO);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFont(btnIngresar.getFont().deriveFont(Font.BOLD, 14f));
        btnIngresar.setAlignmentX(LEFT_ALIGNMENT);
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        raiz.add(btnIngresar);

        setContentPane(raiz);
        getRootPane().setDefaultButton(btnIngresar);
        btnIngresar.addActionListener(e -> ingresar());

        setSize(430, 440);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(Estilos.TEXTO_SUAVE);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return l;
    }

    private void ingresar() {
        String id = txtUsuario.getText();
        String clave = new String(txtClave.getPassword());
        lblMensaje.setText(" ");
        btnIngresar.setEnabled(false);
        btnIngresar.setText("Verificando...");

        // Consulta a la BD en un hilo aparte para no congelar la ventana.
        TareaSegundoPlano.lanzar("hilo-login", new TareaSegundoPlano<Object[]>(() -> {
            Usuario u = controlador.iniciarSesion(id, clave);
            Estudiante ficha = u.esBibliotecario() ? null : controlador.ficha(u);
            return new Object[]{u, ficha};
        }, resultado -> {
            Usuario u = (Usuario) resultado[0];
            Estudiante ficha = (Estudiante) resultado[1];
            MainFrame principal = new MainFrame(u, ficha);
            dispose();
            principal.setVisible(true);
        }, mensaje -> {
            btnIngresar.setEnabled(true);
            btnIngresar.setText("Ingresar");
            lblMensaje.setText("<html>" + mensaje + "</html>");
            txtClave.setText("");
            txtClave.requestFocusInWindow();
        }));
    }

    /** Muestra un error grave de arranque (por ejemplo, la BD no responde). */
    public static void errorDeArranque(String detalle) {
        JOptionPane.showMessageDialog(null,
                "No se pudo conectar con la base de datos MySQL.\n\n" + detalle
                        + "\n\nRevisa que MySQL esté iniciado, que hayas ejecutado los scripts SQL y que\n"
                        + "los datos de conexión de db.properties (URL, usuario y contraseña) sean correctos.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
    }
}
