// vista/GestionFeriados.java
package vista;

import com.toedter.calendar.JDateChooser;
import modelo.dao.FeriadoDAO;
import modelo.entidades.Feriado;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/** Mantenimiento de feriados: en esos días no se marcan faltas ni se descuenta al personal. */
public class GestionFeriados extends JFrame {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final FeriadoDAO feriadoDAO = new FeriadoDAO();
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JDateChooser fecha;
    private final JTextField descripcion;
    private List<Feriado> feriados;

    public GestionFeriados() {
        setTitle("Gestión de Feriados");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBackground(Color.WHITE);
        principal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel titulo = new JPanel();
        titulo.setBackground(new Color(142, 68, 173));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel lblTitulo = new JLabel("GESTIÓN DE FERIADOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.add(lblTitulo);
        principal.add(titulo, BorderLayout.NORTH);

        // Formulario
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),
                "Registrar feriado o día no laborable", 0, 0, new Font("Segoe UI", Font.BOLD, 14)));
        JLabel lblFecha = new JLabel("Fecha:");
        lblFecha.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fecha = new JDateChooser(new java.util.Date());
        fecha.setDateFormatString("dd/MM/yyyy");
        fecha.setPreferredSize(new Dimension(140, 30));
        JLabel lblDesc = new JLabel("Descripción:");
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        descripcion = new JTextField(22);
        descripcion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JButton btnAgregar = boton("Agregar", new Color(46, 204, 113));
        form.add(lblFecha);
        form.add(fecha);
        form.add(lblDesc);
        form.add(descripcion);
        form.add(btnAgregar);

        // Tabla
        modelo = new DefaultTableModel(new String[] {"Fecha", "Día", "Descripción"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(25);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(380);

        JLabel nota = new JLabel("<html>En un feriado no se marcan faltas automáticas ni se descuenta al personal administrativo. "
                + "Si alguien trabaja ese día y marca asistencia, sus horas se registran normalmente.</html>");
        nota.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        nota.setForeground(new Color(110, 110, 110));

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setBackground(Color.WHITE);
        centro.add(form, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        centro.add(nota, BorderLayout.SOUTH);
        principal.add(centro, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        botones.setBackground(Color.WHITE);
        JButton btnEliminar = boton("Eliminar seleccionado", new Color(231, 76, 60));
        btnEliminar.setPreferredSize(new Dimension(190, 35));
        JButton btnCerrar = boton("Cerrar", new Color(149, 165, 166));
        botones.add(btnEliminar);
        botones.add(btnCerrar);
        principal.add(botones, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregar());
        descripcion.addActionListener(e -> agregar());
        btnEliminar.addActionListener(e -> eliminar());
        btnCerrar.addActionListener(e -> dispose());

        setContentPane(principal);
        setSize(760, 560);
        setLocationRelativeTo(null);
        cargar();
    }

    private static JButton boton(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(color);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(120, 35));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void cargar() {
        modelo.setRowCount(0);
        feriados = feriadoDAO.listar();
        for (Feriado f : feriados) {
            String dia = f.getFecha().getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "PE"));
            modelo.addRow(new Object[] {
                f.getFecha().format(FORMATO),
                Character.toUpperCase(dia.charAt(0)) + dia.substring(1),
                f.getDescripcion()
            });
        }
    }

    private void agregar() {
        if (fecha.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione la fecha del feriado.", "Fecha requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String desc = descripcion.getText().trim();
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba una descripción (por ejemplo: \"Día del Trabajo\").",
                    "Descripción requerida", JOptionPane.WARNING_MESSAGE);
            descripcion.requestFocus();
            return;
        }
        if (desc.length() > 100) {
            desc = desc.substring(0, 100);
        }
        LocalDate dia = fecha.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (!feriadoDAO.guardar(new Feriado(dia, desc))) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el feriado. Verifique la conexión a MySQL.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Si ese día ya tenía faltas automáticas (por ejemplo, se declaró feriado después), se quitan
        int faltas = feriadoDAO.eliminarFaltasAutomaticas(dia);
        if (faltas > 0) {
            JOptionPane.showMessageDialog(this,
                    "Se quitaron " + faltas + " falta(s) automática(s) registradas el " + dia.format(FORMATO) + ".\n"
                    + "Si ya existía una planilla de ese mes, vuelva a generarla para que se refleje.",
                    "Faltas corregidas", JOptionPane.INFORMATION_MESSAGE);
        }
        descripcion.setText("");
        cargar();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un feriado de la lista.", "Sin selección",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Feriado f = feriados.get(tabla.convertRowIndexToModel(fila));
        int r = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el feriado del " + f.getFecha().format(FORMATO) + " (" + f.getDescripcion() + ")?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            feriadoDAO.eliminar(f.getFecha());
            cargar();
        }
    }
}
