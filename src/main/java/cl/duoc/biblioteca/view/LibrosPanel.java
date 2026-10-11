package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.LibroController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.model.Libro;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/** Pantalla de gestión de libros. En modo solo lectura (estudiantes) funciona como catálogo de consulta. */
public class LibrosPanel extends PanelBase {

    private final LibroController controlador;
    private final boolean soloLectura;

    private final ModeloTabla modelo = new ModeloTabla("ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock");
    private final JTable tabla = crearTabla(modelo, 40, 230, 160, 120, 110, 110, 50);
    private final JTextField txtBuscar = new JTextField();
    private final List<Libro> libros = new ArrayList<>();

    private final JTextField txtTitulo = new JTextField();
    private final JTextField txtAutor = new JTextField();
    private final JTextField txtIsbn = new JTextField();
    private final JTextField txtEditorial = new JTextField();
    private final JTextField txtStock = new JTextField();
    private final JComboBox<Categoria> cmbCategoria = new JComboBox<>();
    private Libro seleccionado = new Libro();

    public LibrosPanel(LibroController controlador, boolean soloLectura) {
        super(soloLectura ? "Catálogo de libros" : "Gestión de libros",
                soloLectura ? "Consulta los libros disponibles en la biblioteca"
                        : "Registra, modifica, elimina y visualiza el inventario");
        this.controlador = controlador;
        this.soloLectura = soloLectura;
        construir();
        cargarDatos();
    }

    private void construir() {
        // Zona central: buscador + tabla
        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        JPanel barra = new JPanel(new BorderLayout(8, 0));
        barra.setOpaque(false);
        barra.add(new JLabel("Buscar:"), BorderLayout.WEST);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Título, autor, ISBN o editorial");
        barra.add(txtBuscar, BorderLayout.CENTER);
        JButton btnBuscar = boton("Buscar", false);
        barra.add(btnBuscar, BorderLayout.EAST);
        centro.add(barra, BorderLayout.NORTH);
        centro.add(envolver(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> cargarDatos());
        txtBuscar.addActionListener(e -> cargarDatos());

        if (soloLectura) {
            return;
        }

        // Formulario lateral
        JPanel form = crearFormulario("Datos del libro");
        agregarCampo(form, 1, "Título", txtTitulo);
        agregarCampo(form, 2, "Autor", txtAutor);
        agregarCampo(form, 3, "ISBN", txtIsbn);
        agregarCampo(form, 4, "Editorial", txtEditorial);
        agregarCampo(form, 5, "Stock disponible", txtStock);
        agregarCampo(form, 6, "Categoría", cmbCategoria);
        JButton btnGuardar = boton("Guardar", true);
        JButton btnNuevo = boton("Nuevo / limpiar", false);
        JButton btnEliminar = botonPeligro("Eliminar");
        agregarBotones(form, 7, btnGuardar, btnNuevo, btnEliminar);
        add(form, BorderLayout.EAST);

        btnGuardar.addActionListener(e -> guardar());
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnEliminar.addActionListener(e -> eliminar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                alSeleccionar();
            }
        });
    }

    @Override
    public void cargarDatos() {
        try {
            libros.clear();
            libros.addAll(controlador.buscar(txtBuscar.getText()));
            modelo.setRowCount(0);
            for (Libro l : libros) {
                modelo.addRow(new Object[]{l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(),
                        l.getEditorial(), l.getCategoriaNombre(), l.getStock()});
            }
            if (!soloLectura) {
                cmbCategoria.removeAllItems();
                for (Categoria c : controlador.categorias()) {
                    cmbCategoria.addItem(c);
                }
                limpiarFormulario();
            }
        } catch (BibliotecaException e) {
            error(e.getMessage());
        }
    }

    private Libro libroDeFila(int filaVista) {
        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        int id = (Integer) modelo.getValueAt(filaModelo, 0);
        for (Libro l : libros) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    private void alSeleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        Libro l = libroDeFila(fila);
        if (l == null) {
            return;
        }
        seleccionado = l;
        txtTitulo.setText(l.getTitulo());
        txtAutor.setText(l.getAutor());
        txtIsbn.setText(l.getIsbn());
        txtEditorial.setText(l.getEditorial());
        txtStock.setText(String.valueOf(l.getStock()));
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getId() == l.getIdCategoria()) {
                cmbCategoria.setSelectedIndex(i);
                break;
            }
        }
    }

    private void limpiarFormulario() {
        seleccionado = new Libro();
        tabla.clearSelection();
        txtTitulo.setText("");
        txtAutor.setText("");
        txtIsbn.setText("");
        txtEditorial.setText("");
        txtStock.setText("");
        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }
    }

    private void guardar() {
        try {
            boolean nuevo = seleccionado.getId() == 0;
            controlador.guardar(seleccionado, txtTitulo.getText(), txtAutor.getText(), txtIsbn.getText(),
                    txtEditorial.getText(), txtStock.getText(), (Categoria) cmbCategoria.getSelectedItem());
            cargarDatos();
            info(nuevo ? "Libro registrado correctamente." : "Libro modificado correctamente.");
        } catch (BibliotecaException e) {
            error(e.getMessage());
            // el objeto pudo quedar con datos a medio validar: se vuelve a leer de la BD
            if (seleccionado.getId() != 0) {
                cargarDatos();
            }
        }
    }

    private void eliminar() {
        if (seleccionado.getId() == 0) {
            error("Seleccione un libro de la tabla para eliminarlo.");
            return;
        }
        if (!confirmar("¿Eliminar el libro \"" + seleccionado.getTitulo() + "\"?")) {
            return;
        }
        try {
            controlador.eliminar(seleccionado);
            cargarDatos();
            info("Libro eliminado.");
        } catch (BibliotecaException e) {
            error(e.getMessage());
        }
    }
}
