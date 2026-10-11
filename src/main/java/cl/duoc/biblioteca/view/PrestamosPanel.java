package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.PrestamoController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Prestamo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de préstamos y devoluciones. Registrar un préstamo o una devolución se ejecuta en un
 * hilo independiente: mientras tanto la ventana sigue respondiendo y se muestra una barra de progreso.
 */
public class PrestamosPanel extends PanelBase {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final int COL_ESTADO = 6;

    private final PrestamoController controlador;
    private final boolean esBibliotecario;

    private final JComboBox<Estudiante> cmbEstudiante = new JComboBox<>();
    private final JComboBox<Libro> cmbLibro = new JComboBox<>();
    private final JComboBox<String> cmbFiltro =
            new JComboBox<>(new String[]{"Todos", "Vigentes", "Atrasados", "Devueltos"});
    private final JButton btnPrestar = boton("Registrar préstamo", true);
    private final JButton btnDevolver = boton("Registrar devolución del seleccionado", false);
    private final JProgressBar progreso = new JProgressBar();
    private final JLabel lblEstado = new JLabel(" ");

    private final ModeloTabla modelo =
            new ModeloTabla("ID", "Libro", "Estudiante", "Fecha préstamo", "Vence", "Días atraso", "Estado");
    private final JTable tabla = crearTabla(modelo, 50, 260, 200, 110, 110, 90, 90);
    private final List<Prestamo> prestamos = new ArrayList<>();

    public PrestamosPanel(PrestamoController controlador, boolean esBibliotecario) {
        super("Préstamos y devoluciones",
                esBibliotecario ? "Registra préstamos, controla devoluciones y detecta atrasos"
                        : "Pide libros prestados, devuélvelos y revisa tu historial");
        this.controlador = controlador;
        this.esBibliotecario = esBibliotecario;
        construir();
        cargarDatos();
    }

    private void construir() {
        // --- Tarjeta superior: nuevo préstamo ---
        JPanel tarjeta = new JPanel(new GridBagLayout());
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        cmbEstudiante.setEnabled(esBibliotecario);
        cmbEstudiante.setPrototypeDisplayValue(null);
        progreso.setIndeterminate(true);
        progreso.setVisible(false);
        progreso.setPreferredSize(new java.awt.Dimension(90, 8));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.insets = new Insets(0, 0, 0, 8);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        tarjeta.add(new JLabel("Estudiante:"), g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 18);
        tarjeta.add(cmbEstudiante, g);
        g.gridx = 2;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0, 0, 0, 8);
        tarjeta.add(new JLabel("Libro:"), g);
        g.gridx = 3;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 18);
        tarjeta.add(cmbLibro, g);
        g.gridx = 4;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0, 0, 0, 10);
        tarjeta.add(btnPrestar, g);
        g.gridx = 5;
        g.insets = new Insets(0, 0, 0, 0);
        tarjeta.add(progreso, g);

        // --- Barra intermedia: filtro y devolución ---
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izq.setOpaque(false);
        izq.add(new JLabel("Mostrar:"));
        izq.add(cmbFiltro);
        JButton btnActualizar = boton("Actualizar", false);
        izq.add(btnActualizar);
        izq.add(lblEstado);
        barra.add(izq, BorderLayout.WEST);
        barra.add(btnDevolver, BorderLayout.EAST);

        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.setOpaque(false);
        norte.add(tarjeta, BorderLayout.NORTH);
        norte.add(barra, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(norte, BorderLayout.NORTH);
        centro.add(envolver(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                                                           boolean foco, int fila, int col) {
                Component c = super.getTableCellRendererComponent(t, valor, sel, foco, fila, col);
                String estado = String.valueOf(t.getValueAt(fila, COL_ESTADO));
                Color color = t.getForeground();
                if ("Atrasado".equals(estado)) {
                    color = Estilos.PELIGRO;
                } else if ("Devuelto".equals(estado)) {
                    color = Estilos.TEXTO_SUAVE;
                }
                c.setForeground(sel ? Color.BLACK : color);
                c.setFont(t.getFont().deriveFont("Atrasado".equals(estado) ? Font.BOLD : Font.PLAIN));
                return c;
            }
        });

        btnPrestar.addActionListener(e -> registrarPrestamo());
        btnDevolver.addActionListener(e -> registrarDevolucion());
        btnActualizar.addActionListener(e -> cargarDatos());
        cmbFiltro.addActionListener(e -> mostrarPrestamos());
    }

    @Override
    public void cargarDatos() {
        try {
            Object estudianteElegido = cmbEstudiante.getSelectedItem();
            cmbEstudiante.removeAllItems();
            for (Estudiante e : controlador.estudiantesDisponibles()) {
                cmbEstudiante.addItem(e);
                if (estudianteElegido instanceof Estudiante
                        && ((Estudiante) estudianteElegido).getId() == e.getId()) {
                    cmbEstudiante.setSelectedItem(e);
                }
            }
            cmbLibro.removeAllItems();
            for (Libro l : controlador.libros()) {
                if (l.estaDisponible()) {
                    cmbLibro.addItem(l);
                }
            }
            prestamos.clear();
            prestamos.addAll(controlador.listarPrestamos());
            mostrarPrestamos();
        } catch (BibliotecaException e) {
            error(e.getMessage());
        }
    }

    private void mostrarPrestamos() {
        LocalDate hoy = LocalDate.now();
        String filtro = (String) cmbFiltro.getSelectedItem();
        modelo.setRowCount(0);
        int atrasados = 0;
        for (Prestamo p : prestamos) {
            String estado = p.getEstado(hoy);
            if (p.estaAtrasado(hoy)) {
                atrasados++;
            }
            boolean visible = "Todos".equals(filtro)
                    || ("Vigentes".equals(filtro) && "Vigente".equals(estado))
                    || ("Atrasados".equals(filtro) && "Atrasado".equals(estado))
                    || ("Devueltos".equals(filtro) && "Devuelto".equals(estado));
            if (visible) {
                modelo.addRow(new Object[]{p.getId(), p.getLibroTitulo(), p.getEstudianteNombre(),
                        p.getFechaPrestamo().format(FECHA), p.getFechaVencimiento().format(FECHA),
                        p.diasAtraso(hoy), estado});
            }
        }
        lblEstado.setText(atrasados > 0 ? "  ⚠ " + atrasados + " préstamo(s) atrasado(s)" : "  Sin atrasos");
        lblEstado.setForeground(atrasados > 0 ? Estilos.PELIGRO : Estilos.OK);
    }

    private void ocupado(boolean valor, String mensaje) {
        btnPrestar.setEnabled(!valor);
        btnDevolver.setEnabled(!valor);
        progreso.setVisible(valor);
        if (valor) {
            lblEstado.setForeground(Estilos.TEXTO_SUAVE);
            lblEstado.setText("  " + mensaje);
        }
    }

    private void registrarPrestamo() {
        Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem();
        Libro libro = (Libro) cmbLibro.getSelectedItem();
        if (estudiante == null || libro == null) {
            error("Seleccione un estudiante y un libro disponible.");
            return;
        }
        ocupado(true, "Registrando préstamo...");
        // El trabajo se hace en otro hilo; estos callbacks vuelven al hilo de la interfaz.
        controlador.registrarPrestamoAsync(estudiante.getId(), libro.getId(),
                prestamo -> {
                    ocupado(false, "");
                    cargarDatos();
                    info("Préstamo registrado.\n\nLibro: " + prestamo.getLibroTitulo()
                            + "\nEstudiante: " + prestamo.getEstudianteNombre()
                            + "\nDebe devolverlo antes del " + prestamo.getFechaVencimiento().format(FECHA) + ".");
                },
                mensaje -> {
                    ocupado(false, "");
                    cargarDatosSilencioso();
                    error(mensaje);
                });
    }

    private void registrarDevolucion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            error("Seleccione en la tabla el préstamo que se devuelve.");
            return;
        }
        int id = (Integer) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        Prestamo prestamo = prestamos.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
        if (prestamo == null) {
            return;
        }
        if (prestamo.isDevuelto()) {
            error("Ese préstamo ya fue devuelto.");
            return;
        }
        ocupado(true, "Registrando devolución...");
        controlador.registrarDevolucionAsync(prestamo,
                dias -> {
                    ocupado(false, "");
                    cargarDatos();
                    if (dias > 0) {
                        info("Devolución registrada con " + dias + " día(s) de atraso.\n"
                                + "Se dejó constancia del atraso.");
                    } else {
                        info("Devolución registrada a tiempo. El stock fue repuesto.");
                    }
                },
                mensaje -> {
                    ocupado(false, "");
                    cargarDatosSilencioso();
                    error(mensaje);
                });
    }

    private void cargarDatosSilencioso() {
        try {
            prestamos.clear();
            prestamos.addAll(controlador.listarPrestamos());
            mostrarPrestamos();
        } catch (BibliotecaException ignorado) {
            // el mensaje de error original ya se mostrará al usuario
        }
    }
}
