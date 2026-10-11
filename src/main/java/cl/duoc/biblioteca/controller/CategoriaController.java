package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.CategoriaDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.util.Validador;

import java.util.List;

/** Controlador de categorías: valida los datos y delega en el DAO. */
public class CategoriaController extends BaseController {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public CategoriaController(Usuario usuarioActual) {
        super(usuarioActual);
    }

    public List<Categoria> listar() throws BibliotecaException {
        return categoriaDAO.listar();
    }

    public Categoria guardar(Categoria categoria, String nombre) throws BibliotecaException {
        exigirBibliotecario();
        categoria.setNombre(Validador.obligatorio(nombre, "Nombre"));
        if (categoria.getId() == 0) {
            categoriaDAO.insertar(categoria);
        } else {
            categoriaDAO.actualizar(categoria);
        }
        return categoria;
    }

    public void eliminar(Categoria categoria) throws BibliotecaException {
        exigirBibliotecario();
        categoriaDAO.eliminar(categoria.getId());
    }
}
