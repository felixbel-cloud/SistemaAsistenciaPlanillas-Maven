// vista/ExportacionIndividual.java
package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;

/**
 * Agrega a una tabla de reporte la opción de exportar a PDF solo los datos de la
 * persona seleccionada: con un botón, con doble clic sobre la fila o con clic derecho.
 */
final class ExportacionIndividual {

    private ExportacionIndividual() {
    }

    /** Crea el botón "PDF individual" con el mismo estilo que los demás botones de los reportes. */
    static JButton crearBoton() {
        JButton boton = new JButton("PDF individual");
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(new Color(28, 92, 160));
        boton.setForeground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(150, 35));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setToolTipText("Seleccione una persona en la tabla y exporte solo sus datos a PDF "
                + "(también con doble clic o clic derecho sobre la fila)");
        return boton;
    }

    /**
     * Conecta el botón, el doble clic y el menú de clic derecho de la tabla con la acción
     * que recibe el número de fila seleccionada.
     */
    static void instalar(JTable tabla, JButton boton, String textoMenu, IntConsumer accion) {
        boton.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(tabla),
                        "Primero seleccione una persona haciendo clic en su fila de la tabla.",
                        "Seleccione una persona",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            accion.accept(tabla.convertRowIndexToModel(fila));
        });

        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem(textoMenu);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                accion.accept(tabla.convertRowIndexToModel(fila));
            }
        });
        menu.add(item);

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mostrarMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mostrarMenu(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int fila = tabla.rowAtPoint(e.getPoint());
                    if (fila >= 0) {
                        tabla.setRowSelectionInterval(fila, fila);
                        accion.accept(tabla.convertRowIndexToModel(fila));
                    }
                }
            }

            private void mostrarMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila >= 0) {
                    tabla.setRowSelectionInterval(fila, fila);
                    menu.show(tabla, e.getX(), e.getY());
                }
            }
        });
    }

    /** Nombre de archivo seguro a partir del nombre de la persona. */
    static String nombreArchivo(String prefijo, String nombre) {
        String limpio = java.text.Normalizer.normalize(nombre == null ? "" : nombre, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        return prefijo + (limpio.isEmpty() ? "" : "_" + limpio);
    }
}
