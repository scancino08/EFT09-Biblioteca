package cl.duoc.biblioteca.view;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import java.awt.Color;

/** Colores y configuración visual compartida por todas las ventanas. */
public final class Estilos {

    public static final Color PRIMARIO = new Color(0x2B5C8A);
    public static final Color PRIMARIO_OSCURO = new Color(0x1E4467);
    public static final Color PELIGRO = new Color(0xB3261E);
    public static final Color OK = new Color(0x1B7F4B);
    public static final Color TEXTO_SUAVE = new Color(0x6B7480);
    public static final Color BORDE = new Color(0xD5DAE0);
    public static final Color FONDO_NAV = new Color(0x1F2D3D);

    private Estilos() {
    }

    public static void aplicarTema() {
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Table.selectionBackground", new Color(0xD8E6F3));
        UIManager.put("Table.selectionForeground", Color.BLACK);
        UIManager.put("Component.focusColor", PRIMARIO);
        UIManager.put("Button.default.background", PRIMARIO);
        FlatLightLaf.setup();
    }
}
