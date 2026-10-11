package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.ReporteController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.reporte.Reporte;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/** Pantalla de reportes: elige un reporte, opcionalmente un estudiante, y muestra el resultado. */
public class ReportesPanel extends PanelBase {

    private final ReporteController controlador;
    private final JComboBox<Reporte> cmbReporte = new JComboBox<>();
    private final JComboBox<Estudiante> cmbEstudiante = new JComboBox<>();
    private final JLabel lblEstudiante = new JLabel("Estudiante:");
    private final JLabel lblResultado = new JLabel(" ");
    private final ModeloTabla modelo = new ModeloTabla();
    private final JTable tabla = crearTabla(modelo);

    public ReportesPanel(ReporteController controlador) {
        super("Reportes", "Información para la toma de decisiones de la biblioteca");
        this.controlador = controlador;
        construir();
        cargarDatos();
    }

    private void construir() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        barra.add(new JLabel("Reporte:"));
        cmbReporte.setPreferredSize(new java.awt.Dimension(260, 32));
        barra.add(cmbReporte);
        barra.add(lblEstudiante);
        cmbEstudiante.setPreferredSize(new java.awt.Dimension(280, 32));
        barra.add(cmbEstudiante);
        JButton btnGenerar = boton("Generar reporte", true);
        barra.add(btnGenerar);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(barra, BorderLayout.NORTH);
        centro.add(envolver(tabla), BorderLayout.CENTER);
        centro.add(lblResultado, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        cmbReporte.addActionListener(e -> {
            Reporte r = (Reporte) cmbReporte.getSelectedItem();
            boolean pide = r != null && r.requiereEstudiante();
            lblEstudiante.setVisible(pide);
            cmbEstudiante.setVisible(pide);
        });
        btnGenerar.addActionListener(e -> generar());
    }

    @Override
    public void cargarDatos() {
        try {
            cmbReporte.removeAllItems();
            for (Reporte r : controlador.reportesDisponibles()) {
                cmbReporte.addItem(r);
            }
            cmbEstudiante.removeAllItems();
            for (Estudiante e : controlador.estudiantes()) {
                cmbEstudiante.addItem(e);
            }
            cmbReporte.setSelectedIndex(0);
        } catch (BibliotecaException e) {
            error(e.getMessage());
        }
    }

    private void generar() {
        Reporte reporte = (Reporte) cmbReporte.getSelectedItem();
        if (reporte == null) {
            return;
        }
        try {
            Estudiante est = reporte.requiereEstudiante() ? (Estudiante) cmbEstudiante.getSelectedItem() : null;
            List<Object[]> filas = controlador.generar(reporte, est);
            modelo.setColumnIdentifiers(reporte.getColumnas());
            modelo.setRowCount(0);
            for (Object[] fila : filas) {
                modelo.addRow(fila);
            }
            lblResultado.setText(reporte.getNombre() + ": " + filas.size() + " registro(s)");
        } catch (BibliotecaException e) {
            error(e.getMessage());
        }
    }
}
