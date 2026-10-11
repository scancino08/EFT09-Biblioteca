package cl.duoc.biblioteca.model;

/** Libro del inventario (tabla libros). Implementa {@link Prestable}. */
public class Libro extends EntidadBase implements Prestable {

    private String titulo;
    private String autor;
    private String isbn;
    private String editorial;
    private int stock;
    private int idCategoria;
    /** Dato de apoyo (JOIN con categorias), solo para mostrar. */
    private String categoriaNombre;

    public Libro() {
    }

    public Libro(int id, String titulo, String autor, String isbn, String editorial, int stock, int idCategoria) {
        super(id);
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.editorial = editorial;
        this.stock = stock;
        this.idCategoria = idCategoria;
    }

    @Override
    public boolean estaDisponible() {
        return stock > 0;
    }

    @Override
    public void registrarPrestamo() {
        if (!estaDisponible()) {
            throw new IllegalStateException("Sin stock disponible de \"" + titulo + "\"");
        }
        stock--;
    }

    @Override
    public void registrarDevolucion() {
        stock++;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    @Override
    public String descripcion() {
        return titulo + " - " + autor + " (stock: " + stock + ")";
    }
}
