package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.EstudianteController;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Estudiante;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/** Pantalla de gestión de estudiantes (y de sus cuentas de acceso). */
public class EstudiantesPanel extends PanelBase {

    private final EstudianteController controlador;
    private final ModeloTabla modelo = new ModeloTabla("ID", "Nombre", "RUT", "Curso", "Correo");
    private final JTable tabla = crearTabla(modelo, 40, 200, 110, 110, 190);
    private final List<Estudiante> estudiantes = new ArrayList<>();

    private final JTextField txtNombre = new JTextField();
    private final JTextField txtRut = new JTextField();
    private final JTextField txtCurso = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private Estudiante seleccionado = new Estudiante();

    public EstudiantesPanel(EstudianteController controlador) {
        super("Gestión de estudiantes", "Registra estudiantes y sus cuentas de acceso al sistema");
        this.controlador = controlador;
        construir();
        cargarDatos();
    }

    private void construir() {
        add(envolver(tabla), BorderLayout.CENTER);

        var form = crearFormulario("Datos del estudiante");
        txtRut.putClientProperty("JTextField.placeholderText", "12345678-9");
        txtClave.putClientProperty("JTextField.placeholderText", "Vacío = mantener la actual");
        txtClave.putClientProperty("JPasswordField.showRevealButton", true);
        agregarCampo(form, 1, "Nombre completo", txtNombre);
        agregarCampo(form, 2, "RUT", txtRut);
        agregarCampo(form, 3, "Curso", txtCurso);
        agregarCampo(form, 4, "Correo", txtCorreo);
        agregarCampo(form, 5, "Contraseña de acceso", txtClave);
        JButton btnGuardar = boton("Guardar", true);
        JButton btnNuevo = boton("Nuevo / limpiar", false);
        JButton btnEliminar = botonPeligro("Eliminar");
        agregarBotones(form, 6, btnGuardar, btnNuevo, btnEliminar);
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
            estudiantes.clear();
            estudiantes.addAll(controlador.listar());
            modelo.setRowCount(0);
            for (Estudiante e : estudiantes) {
                modelo.addRow(new Object[]{e.getId(), e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo()});
            }
            limpiarFormulario();
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
        for (Estudiante e : estudiantes) {
            if (e.getId() == id) {
                seleccionado = e;
                txtNombre.setText(e.getNombre());
                txtRut.setText(e.getRut());
                txtCurso.setText(e.getCurso());
                txtCorreo.setText(e.getCorreo());
                txtClave.setText("");
                return;
            }
        }
    }

    private void limpiarFormulario() {
        seleccionado = new Estudiante();
        tabla.clearSelection();
        txtNombre.setText("");
        txtRut.setText("");
        txtCurso.setText("");
        txtCorreo.setText("");
        txtClave.setText("");
    }

    private void guardar() {
        try {
            boolean nuevo = seleccionado.getId() == 0;
            controlador.guardar(seleccionado, txtNombre.getText(), txtRut.getText(), txtCurso.getText(),
                    txtCorreo.getText(), new String(txtClave.getPassword()));
            cargarDatos();
            info(nuevo ? "Estudiante registrado con su usuario de acceso." : "Estudiante modificado correctamente.");
        } catch (BibliotecaException e) {
            error(e.getMessage());
            if (seleccionado.getId() != 0) {
                cargarDatos();
            }
        }
    }

    private void eliminar() {
        if (seleccionado.getId() == 0) {
            error("Seleccione un estudiante de la tabla para eliminarlo.");
            return;
        }
        if (!confirmar("¿Eliminar a " + seleccionado.getNombre() + " y su usuario de acceso?")) {
            return;
        }
        try {
            controlador.eliminar(seleccionado);
            cargarDatos();
            info("Estudiante eliminado.");
        } catch (BibliotecaException e) {
            error(e.getMessage());
        }
    }
}
