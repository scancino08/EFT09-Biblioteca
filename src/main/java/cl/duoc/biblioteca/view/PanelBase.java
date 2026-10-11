package cl.duoc.biblioteca.view;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Clase abstracta de la que heredan todas las pantallas (vistas) del sistema. Dibuja el
 * encabezado y ofrece utilidades comunes (tablas, formularios, mensajes). Cada pantalla
 * implementa {@link #cargarDatos()} para refrescar su contenido desde el controlador.
 */
public abstract class PanelBase extends JPanel {

    protected PanelBase(String titulo, String subtitulo) {
        super(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 22f));
        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setForeground(Estilos.TEXTO_SUAVE);

        JPanel cabecera = new JPanel();
        cabecera.setOpaque(false);
        cabecera.setLayout(new javax.swing.BoxLayout(cabecera, javax.swing.BoxLayout.Y_AXIS));
        cabecera.add(lblTitulo);
        cabecera.add(Box.createVerticalStrut(2));
        cabecera.add(lblSub);
        add(cabecera, BorderLayout.NORTH);
    }

    /** Vuelve a leer los datos desde el controlador y refresca la pantalla. */
    public abstract void cargarDatos();

    // ---------- tablas ----------

    /** Modelo de tabla de solo lectura. */
    protected static class ModeloTabla extends DefaultTableModel {
        public ModeloTabla(String... columnas) {
            super(columnas, 0);
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    }

    /** Crea la tabla; los anchos opcionales (en píxeles) se usan como proporción por columna. */
    protected JTable crearTabla(DefaultTableModel modelo, int... anchos) {
        JTable tabla = new JTable(modelo);
        for (int i = 0; i < anchos.length && i < tabla.getColumnModel().getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setShowVerticalLines(false);
        tabla.setAutoCreateRowSorter(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));
        return tabla;
    }

    protected JScrollPane envolver(JTable tabla) {
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createLineBorder(Estilos.BORDE));
        return sp;
    }

    // ---------- formularios ----------

    protected JPanel crearFormulario(String titulo) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        form.setPreferredSize(new Dimension(330, 0));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 15f));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 12, 0);
        form.add(lbl, gbc);
        return form;
    }

    /** Agrega una fila "etiqueta + campo" al formulario (fila 1 en adelante). */
    protected void agregarCampo(JPanel form, int fila, String etiqueta, JComponent campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = fila * 2 - 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 3, 0);
        JLabel l = new JLabel(etiqueta);
        l.setForeground(Estilos.TEXTO_SUAVE);
        form.add(l, gbc);
        gbc.gridy = fila * 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        form.add(campo, gbc);
    }

    /** Agrega un panel de botones al final del formulario y un relleno para empujar todo hacia arriba. */
    protected void agregarBotones(JPanel form, int fila, JButton... botones) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = fila * 2 - 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 0, 0);
        JPanel p = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
        p.setOpaque(false);
        for (JButton b : botones) {
            p.add(b);
        }
        form.add(p, gbc);
        GridBagConstraints relleno = new GridBagConstraints();
        relleno.gridx = 0;
        relleno.gridy = fila * 2;
        relleno.weighty = 1;
        form.add(Box.createVerticalGlue(), relleno);
    }

    // ---------- botones y mensajes ----------

    protected JButton boton(String texto, boolean principal) {
        JButton b = new JButton(texto);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        if (principal) {
            b.setBackground(Estilos.PRIMARIO);
            b.setForeground(Color.WHITE);
            b.setFont(b.getFont().deriveFont(Font.BOLD));
        }
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
    }

    protected JButton botonPeligro(String texto) {
        JButton b = boton(texto, false);
        b.setForeground(Estilos.PELIGRO);
        return b;
    }

    protected void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    protected void info(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    protected boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(this, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
