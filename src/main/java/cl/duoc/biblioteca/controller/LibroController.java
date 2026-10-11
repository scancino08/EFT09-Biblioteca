package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.CategoriaDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.exception.BibliotecaException;
import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.util.Validador;

import java.util.List;

/** Controlador de la gestión de libros. Consultar es para todos; modificar, solo bibliotecarios. */
public class LibroController extends BaseController {

    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public LibroController(Usuario usuarioActual) {
        super(usuarioActual);
    }

    public List<Libro> buscar(String texto) throws BibliotecaException {
        return libroDAO.buscar(texto);
    }

    public List<Categoria> categorias() throws BibliotecaException {
        return categoriaDAO.listar();
    }

    /** Crea (id = 0) o modifica un libro a partir de los datos del formulario. */
    public void guardar(Libro libro, String titulo, String autor, String isbn, String editorial,
                        String stock, Categoria categoria) throws BibliotecaException {
        exigirBibliotecario();
        libro.setTitulo(Validador.obligatorio(titulo, "Título"));
        libro.setAutor(Validador.obligatorio(autor, "Autor"));
        libro.setIsbn(Validador.isbn(isbn));
        libro.setEditorial(Validador.obligatorio(editorial, "Editorial"));
        libro.setStock(Validador.enteroNoNegativo(stock, "Stock"));
        if (categoria == null) {
            throw new BibliotecaException("Debe seleccionar una categoría.");
        }
        libro.setIdCategoria(categoria.getId());
        if (libro.getId() == 0) {
            libroDAO.insertar(libro);
        } else {
            libroDAO.actualizar(libro);
        }
    }

    public void eliminar(Libro libro) throws BibliotecaException {
        exigirBibliotecario();
        libroDAO.eliminar(libro.getId());
    }
}
