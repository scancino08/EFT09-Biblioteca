package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.CategoriaController;
import cl.duoc.biblioteca.controller.EstudianteController;
import cl.duoc.biblioteca.controller.LibroController;
import cl.duoc.biblioteca.controller.PrestamoController;
import cl.duoc.biblioteca.controller.ReporteController;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.util.DatabaseConnection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ventana principal (JFrame). Tiene un menú superior y un panel de navegación lateral; las
 * opciones disponibles dependen del rol: el bibliotecario ve todo, el estudiante solo consulta
 * el catálogo y gestiona sus préstamos y devoluciones.
 */
public class MainFrame extends JFrame {

    private final Usuario usuario;
    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final Map<String, PanelBase> pantallas = new LinkedHashMap<>();
    private final ButtonGroup grupoNav = new ButtonGroup();
    private final Map<String, JToggleButton> botonesNav = new LinkedHashMap<>();

    public MainFrame(Usuario usuario, Estudiante ficha) {
        super("Biblioteca Escolar");
        this.usuario = usuario;

        if (usuario.esBibliotecario()) {
            pantallas.put("Libros", new LibrosPanel(new LibroController(usuario), false));
            pantallas.put("Estudiantes", new EstudiantesPanel(new EstudianteController(usuario)));
            pantallas.put("Categorías", new CategoriasPanel(new CategoriaController(usuario)));
            pantallas.put("Préstamos y devoluciones", new PrestamosPanel(new PrestamoController(usuario, null), true));
            pantallas.put("Reportes", new ReportesPanel(new ReporteController(usuario)));
        } else {
            pantallas.put("Catálogo de libros", new LibrosPanel(new LibroController(usuario), true));
            pantallas.put("Mis préstamos", new PrestamosPanel(new PrestamoController(usuario, ficha), false));
        }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
        setJMenuBar(crearMenu());
        add(crearNavegacion(), BorderLayout.WEST);
        for (Map.Entry<String, PanelBase> e : pantallas.entrySet()) {
            contenido.add(e.getValue(), e.getKey());
        }
        add(contenido, BorderLayout.CENTER);

        setMinimumSize(new Dimension(1100, 660));
        setSize(1280, 760);
        setLocationRelativeTo(null);
        irA(pantallas.keySet().iterator().next());
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        JMenuItem cerrar = new JMenuItem("Cerrar sesión");
        cerrar.addActionListener(e -> cerrarSesion());
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> salir());
        archivo.add(cerrar);
        archivo.addSeparator();
        archivo.add(salir);
        barra.add(archivo);

        JMenu ir = new JMenu("Ir a");
        for (String nombre : pantallas.keySet()) {
            JMenuItem item = new JMenuItem(nombre);
            item.addActionListener(e -> irA(nombre));
            ir.add(item);
        }
        barra.add(ir);

        JMenu ayuda = new JMenu("Ayuda");
        JMenuItem acerca = new JMenuItem("Acerca de");
        acerca.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Sistema de Gestión de Biblioteca Escolar\nDesarrollo Orientado a Objetos II\n"
                        + "Java · Swing · MySQL (JDBC) · MVC · DAO · Singleton",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE));
        ayuda.add(acerca);
        barra.add(ayuda);
        return barra;
    }

    private JPanel crearNavegacion() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Estilos.FONDO_NAV);
        nav.setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));
        nav.setPreferredSize(new Dimension(230, 0));

        JLabel marca = new JLabel("📚 Biblioteca");
        marca.setForeground(Color.WHITE);
        marca.setFont(marca.getFont().deriveFont(Font.BOLD, 20f));
        marca.setAlignmentX(LEFT_ALIGNMENT);
        nav.add(marca);
        nav.add(Box.createVerticalStrut(22));

        for (String nombre : pantallas.keySet()) {
            JToggleButton b = new JToggleButton(nombre);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            b.setFocusPainted(false);
            b.setForeground(new Color(0xD7DEE6));
            b.setBackground(Estilos.FONDO_NAV);
            b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            b.putClientProperty("JButton.buttonType", "borderless");
            b.addActionListener(e -> irA(nombre));
            grupoNav.add(b);
            botonesNav.put(nombre, b);
            nav.add(b);
            nav.add(Box.createVerticalStrut(4));
        }

        nav.add(Box.createVerticalGlue());
        JLabel quien = new JLabel("<html><b>" + usuario.getNombre() + "</b><br>"
                + usuario.getRol().getEtiqueta() + "</html>");
        quien.setForeground(new Color(0xB9C4D0));
        quien.setAlignmentX(LEFT_ALIGNMENT);
        nav.add(quien);
        nav.add(Box.createVerticalStrut(10));
        javax.swing.JButton btnCerrar = new javax.swing.JButton("Cerrar sesión");
        btnCerrar.setAlignmentX(LEFT_ALIGNMENT);
        btnCerrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnCerrar.addActionListener(e -> cerrarSesion());
        nav.add(btnCerrar);
        return nav;
    }

    private void irA(String nombre) {
        PanelBase panel = pantallas.get(nombre);
        panel.cargarDatos();
        cartas.show(contenido, nombre);
        JToggleButton b = botonesNav.get(nombre);
        if (b != null) {
            b.setSelected(true);
            for (JToggleButton otro : botonesNav.values()) {
                otro.setForeground(otro == b ? Color.WHITE : new Color(0xD7DEE6));
                otro.setBackground(otro == b ? Estilos.PRIMARIO : Estilos.FONDO_NAV);
                otro.setFont(otro.getFont().deriveFont(otro == b ? Font.BOLD : Font.PLAIN));
            }
        }
    }

    private void cerrarSesion() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    private void salir() {
        if (JOptionPane.showConfirmDialog(this, "¿Desea salir del sistema?", "Salir",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            DatabaseConnection.getInstance().close();
            dispose();
            System.exit(0);
        }
    }
}
