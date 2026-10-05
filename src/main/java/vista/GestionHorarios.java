package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Time;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import modelo.dao.HorarioPersonalDAO;
import modelo.dao.PersonalDAO;
import modelo.entidades.HorarioPersonal;
import modelo.entidades.Personal;

public class GestionHorarios extends JFrame {
    private static final String[] DIAS = {"LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"};
    private final HorarioPersonalDAO horarioDAO = new HorarioPersonalDAO();
    private final PersonalDAO personalDAO = new PersonalDAO();
    private final JComboBox<Personal> selectorPersonal = new JComboBox<>();
    private final JComboBox<Personal> selectorFiltroPersonal = new JComboBox<>();
    private final JComboBox<String> selectorFiltroTipoPersonal = new JComboBox<>(new String[]{"TODOS", "DOCENTE", "ADMINISTRATIVO"});
    private final JComboBox<String> selectorDia = new JComboBox<>(DIAS);
    private final JComboBox<String> selectorFiltroDia = new JComboBox<>(obtenerDiasFiltro());
    private final JTextField horaInicio = new JTextField("08:00");
    private final JTextField horaFin = new JTextField("17:00");
    private final JTextField descripcion = new JTextField();
    private final JTextField filtroCurso = new JTextField();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "ID Personal", "Personal", "Día", "Inicio", "Fin", "Descripción"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JButton guardar = new JButton("Guardar");
    private final JButton btnFiltrar = new JButton("Filtrar");
    private final JButton btnLimpiarFiltro = new JButton("Limpiar");
    private int idHorarioSeleccionado = -1;

    public GestionHorarios() {
        super("Gestión de Horarios");
        construirVentana();
        cargarPersonal();
        cargarHorarios();
    }

    private void construirVentana() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel cabecera = new JPanel(new BorderLayout(8, 8));

        JPanel formulario = new JPanel(new GridLayout(2, 5, 8, 6));
        formulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        formulario.add(new JLabel("Personal"));
        formulario.add(new JLabel("Día"));
        formulario.add(new JLabel("Hora inicio (HH:mm)"));
        formulario.add(new JLabel("Hora fin (HH:mm)"));
        formulario.add(new JLabel("Descripción / curso"));
        formulario.add(selectorPersonal);
        formulario.add(selectorDia);
        formulario.add(horaInicio);
        formulario.add(horaFin);
        formulario.add(descripcion);

        JPanel filtros = new JPanel(new GridLayout(1, 8, 8, 6));
        filtros.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Filtrar horarios"));
        filtros.add(new JLabel("Tipo"));
        filtros.add(selectorFiltroTipoPersonal);
        filtros.add(new JLabel("Docente"));
        filtros.add(selectorFiltroPersonal);
        filtros.add(new JLabel("Día"));
        filtros.add(selectorFiltroDia);
        filtros.add(new JLabel("Curso / bloque"));
        filtros.add(filtroCurso);
        filtros.add(btnFiltrar);
        filtros.add(btnLimpiarFiltro);

        cabecera.add(formulario, BorderLayout.CENTER);
        cabecera.add(filtros, BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                cargarSeleccion();
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton eliminar = new JButton("Desactivar horario");
        JButton nuevo = new JButton("Nuevo");
        JButton cerrar = new JButton("Cerrar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(guardar);
        botones.add(nuevo);
        botones.add(eliminar);
        botones.add(cerrar);
        add(botones, BorderLayout.SOUTH);

        guardar.addActionListener(event -> guardarHorario());
        nuevo.addActionListener(event -> limpiarFormulario());
        eliminar.addActionListener(event -> desactivarHorario());
        cerrar.addActionListener(event -> dispose());
        btnFiltrar.addActionListener(event -> cargarHorarios());
        btnLimpiarFiltro.addActionListener(event -> limpiarFiltros());

        setSize(1000, 520);
        setLocationRelativeTo(null);
    }

    private void cargarPersonal() {
        try {
            List<Personal> personal = personalDAO.listarPersonal();
            selectorPersonal.addItem(null);
            selectorFiltroPersonal.addItem(null);
            for (Personal persona : personal) {
                selectorPersonal.addItem(persona);
                selectorFiltroPersonal.addItem(persona);
            }
        } catch (Exception e) {
            selectorPersonal.addItem(null);
            selectorFiltroPersonal.addItem(null);
        }

        selectorPersonal.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel etiqueta = new JLabel(value == null ? "Seleccione personal" : value.getNombreCompleto());
            if (isSelected) {
                etiqueta.setOpaque(true);
                etiqueta.setBackground(list.getSelectionBackground());
                etiqueta.setForeground(list.getSelectionForeground());
            }
            return etiqueta;
        });

        selectorFiltroPersonal.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel etiqueta = new JLabel(value == null ? "Todos los docentes" : value.getNombreCompleto());
            if (isSelected) {
                etiqueta.setOpaque(true);
                etiqueta.setBackground(list.getSelectionBackground());
                etiqueta.setForeground(list.getSelectionForeground());
            }
            return etiqueta;
        });

        selectorPersonal.setSelectedIndex(0);
        selectorFiltroPersonal.setSelectedIndex(0);
    }

    private void cargarHorarios() {
        modelo.setRowCount(0);
        Personal docenteFiltro = (Personal) selectorFiltroPersonal.getSelectedItem();
        String tipoFiltro = (String) selectorFiltroTipoPersonal.getSelectedItem();
        String diaFiltro = (String) selectorFiltroDia.getSelectedItem();
        String cursoFiltro = filtroCurso.getText() == null ? "" : filtroCurso.getText().trim();

        try {
            for (HorarioPersonal horario : horarioDAO.listarTodos()) {
                Personal persona = buscarPersonal(horario.getIdPersonal());
                if (persona == null) {
                    continue;
                }

                if (docenteFiltro != null && docenteFiltro.getIdPersonal() != horario.getIdPersonal()) {
                    continue;
                }

                if (!coincideConFiltro(horario, persona, tipoFiltro == null ? "TODOS" : tipoFiltro,
                        docenteFiltro == null ? "" : docenteFiltro.getNombreCompleto(),
                        cursoFiltro,
                        diaFiltro == null ? "" : diaFiltro)) {
                    continue;
                }

                String nombre = persona.getNombreCompleto();
                modelo.addRow(new Object[]{horario.getIdHorario(), horario.getIdPersonal(), nombre,
                    horario.getDiaSemana(), formatoHora(horario.getHoraInicio()), formatoHora(horario.getHoraFin()),
                    horario.getDescripcionBloque()});
            }
        } catch (Exception e) {
            // Silencioso: si la base de datos no está disponible, no se bloquea la pantalla.
        }
    }

    private Personal buscarPersonal(int idPersonal) {
        for (int index = 0; index < selectorPersonal.getItemCount(); index++) {
            Personal persona = selectorPersonal.getItemAt(index);
            if (persona != null && persona.getIdPersonal() == idPersonal) {
                return persona;
            }
        }
        return null;
    }

    private void cargarSeleccion() {
        int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
        idHorarioSeleccionado = (int) modelo.getValueAt(fila, 0);
        selectorPersonal.setSelectedItem(buscarPersonal((int) modelo.getValueAt(fila, 1)));
        selectorDia.setSelectedItem(modelo.getValueAt(fila, 3));
        horaInicio.setText((String) modelo.getValueAt(fila, 4));
        horaFin.setText((String) modelo.getValueAt(fila, 5));
        descripcion.setText(String.valueOf(modelo.getValueAt(fila, 6) == null ? "" : modelo.getValueAt(fila, 6)));
        guardar.setText("Actualizar");
    }

    private void guardarHorario() {
        Personal persona = (Personal) selectorPersonal.getSelectedItem();
        if (persona == null) {
            JOptionPane.showMessageDialog(this, "Registre o seleccione personal activo.", "Datos requeridos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Time inicio = convertirHora(horaInicio.getText());
            Time fin = convertirHora(horaFin.getText());
            if (!inicio.before(fin)) {
                JOptionPane.showMessageDialog(this, "La hora de fin debe ser posterior a la hora de inicio.", "Horario inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (descripcion.getText().trim().length() > 30) {
                JOptionPane.showMessageDialog(this, "La descripción admite hasta 30 caracteres.", "Descripción inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            HorarioPersonal horario = new HorarioPersonal();
            horario.setIdHorario(idHorarioSeleccionado);
            horario.setIdPersonal(persona.getIdPersonal());
            horario.setDiaSemana((String) selectorDia.getSelectedItem());
            horario.setHoraInicio(inicio);
            horario.setHoraFin(fin);
            horario.setDescripcionBloque(descripcion.getText().trim());
            horario.setActivo(true);

            boolean guardado = idHorarioSeleccionado < 0 ? horarioDAO.crear(horario) : horarioDAO.actualizar(horario);
            if (!guardado) {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el horario. Verifique la conexión a MySQL.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            limpiarFormulario();
            cargarHorarios();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Use el formato HH:mm para las horas.", "Hora inválida", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void desactivarHorario() {
        if (idHorarioSeleccionado < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un horario de la tabla.", "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea desactivar este horario?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION && horarioDAO.desactivar(idHorarioSeleccionado)) {
            limpiarFormulario();
            cargarHorarios();
        }
    }

    private void limpiarFormulario() {
        idHorarioSeleccionado = -1;
        tabla.clearSelection();
        selectorPersonal.setSelectedIndex(selectorPersonal.getItemCount() == 0 ? -1 : 0);
        selectorDia.setSelectedIndex(0);
        horaInicio.setText("08:00");
        horaFin.setText("17:00");
        descripcion.setText("");
        guardar.setText("Guardar");
    }

    private void limpiarFiltros() {
        selectorFiltroTipoPersonal.setSelectedIndex(0);
        selectorFiltroPersonal.setSelectedIndex(0);
        selectorFiltroDia.setSelectedIndex(0);
        filtroCurso.setText("");
        cargarHorarios();
    }

    boolean coincideConFiltro(HorarioPersonal horario, Personal persona, String tipoPersonalFiltro, String docenteFiltro, String cursoFiltro, String diaFiltro) {
        String tipoPersonal = persona == null || persona.getTipoPersonal() == null ? "" : persona.getTipoPersonal().trim();
        String tipoBusqueda = tipoPersonalFiltro == null ? "TODOS" : tipoPersonalFiltro.trim();
        if (!"TODOS".equalsIgnoreCase(tipoBusqueda) && !tipoBusqueda.equalsIgnoreCase(tipoPersonal)) {
            return false;
        }

        String docente = persona == null ? "" : persona.getNombreCompleto();
        String docenteBusqueda = docenteFiltro == null ? "" : docenteFiltro.trim();
        String cursoBusqueda = cursoFiltro == null ? "" : cursoFiltro.trim();
        String diaBusqueda = diaFiltro == null ? "" : diaFiltro.trim();

        if (!docenteBusqueda.isEmpty() && !docente.toLowerCase(Locale.ROOT).contains(docenteBusqueda.toLowerCase(Locale.ROOT))) {
            return false;
        }

        if (!cursoBusqueda.isEmpty()) {
            String descripcion = horario.getDescripcionBloque() == null ? "" : horario.getDescripcionBloque();
            if (!descripcion.toLowerCase(Locale.ROOT).contains(cursoBusqueda.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }

        if (!diaBusqueda.isEmpty() && !"TODOS".equalsIgnoreCase(diaBusqueda) && !diaBusqueda.equalsIgnoreCase(horario.getDiaSemana())) {
            return false;
        }

        return true;
    }

    private static String[] obtenerDiasFiltro() {
        String[] dias = new String[DIAS.length + 1];
        dias[0] = "TODOS";
        System.arraycopy(DIAS, 0, dias, 1, DIAS.length);
        return dias;
    }

    private Time convertirHora(String texto) {
        String valor = texto.trim();
        return Time.valueOf(valor.length() == 5 ? valor + ":00" : valor);
    }

    private String formatoHora(Time hora) {
        return hora == null ? "" : String.format("%02d:%02d", hora.toLocalTime().getHour(), hora.toLocalTime().getMinute());
    }
}