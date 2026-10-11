package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.CategoriaController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Categoria;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/** Pantalla de gestión de categorías de libros. */
public class CategoriasPanel extends PanelBase {

    private final CategoriaController controlador;
    private final ModeloTabla modelo = new ModeloTabla("ID", "Nombre");
    private final JTable tabla = crearTabla(modelo);
    private final List<Categoria> categorias = new ArrayList<>();
    private final JTextField txtNombre = new JTextField();
    private Categoria seleccionada = new Categoria();

    public CategoriasPanel(CategoriaController controlador) {
        super("Categorías", "Clasifica los libros del inventario");
        this.controlador = controlador;
        construir();
        cargarDatos();
    }

    private void construir() {
        add(envolver(tabla), BorderLayout.CENTER);
        var form = crearFormulario("Datos de la categoría");
        agregarCampo(form, 1, "Nombre", txtNombre);
        JButton btnGuardar = boton("Guardar", true);
        JButton btnNuevo = boton("Nueva / limpiar", false);
        JButton btnEliminar = botonPeligro("Eliminar");
        agregarBotones(form, 2, btnGuardar, btnNuevo, btnEliminar);
        add(form, BorderLayout.EAST);

        btnGuardar.addActionListener(e -> guardar());
        btnNuevo.addActionListener(e -> limpiar());
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
            categorias.clear();
            categorias.addAll(controlador.listar());
            modelo.setRowCount(0);
            for (Categoria c : categorias) {
                modelo.addRow(new Object[]{c.getId(), c.getNombre()});
            }
            limpiar();
        } catch (BibliotecaException e) {
            error(e.getMessage());
        }
    }

    private void alSeleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        int id = (Integer) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        for (Categoria c : categorias) {
            if (c.getId() == id) {
                seleccionada = c;
                txtNombre.setText(c.getNombre());
                return;
            }
        }
    }

    private void limpiar() {
        seleccionada = new Categoria();
        tabla.clearSelection();
        txtNombre.setText("");
    }

    private void guardar() {
        try {
            boolean nueva = seleccionada.getId() == 0;
            controlador.guardar(seleccionada, txtNombre.getText());
            cargarDatos();
            info(nueva ? "Categoría registrada." : "Categoría modificada.");
        } catch (BibliotecaException e) {
            error(e.getMessage());
            if (seleccionada.getId() != 0) {
                cargarDatos();
            }
        }
    }

    private void eliminar() {
        if (seleccionada.getId() == 0) {
            error("Seleccione una categoría de la tabla para eliminarla.");
            return;
        }
        if (!confirmar("¿Eliminar la categoría \"" + seleccionada.getNombre() + "\"?")) {
            return;
        }
        try {
            controlador.eliminar(seleccionada);
            cargarDatos();
            info("Categoría eliminada.");
        } catch (BibliotecaException e) {
            error(e.getMessage());
        }
    }
}
