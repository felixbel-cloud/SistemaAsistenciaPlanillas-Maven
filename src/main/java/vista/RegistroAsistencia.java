// vista/RegistroAsistencia.java
package vista;

import com.toedter.calendar.JDateChooser;
import controlador.ControladorAsistencia;
import modelo.entidades.Personal;
import modelo.entidades.Usuario;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistroAsistencia extends JFrame {
    private ControladorAsistencia controlador;
    
    // Componentes
    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JPanel panelFormulario;
    private JPanel panelInformacion;
    private JPanel panelBotones;
    
    private JLabel lblTitulo;
    private JLabel lblDni;
    private JLabel lblNombre;
    private JLabel lblTipo;
    private JLabel lblCargo;
    private JLabel lblHorario;
    
    private JTextField txtDni;
    private JTextField txtNombre;
    private JTextField txtTipo;
    private JTextField txtCargo;
    private JTextField txtHorario;
    
    private JButton btnBuscar;
    private JButton btnRegistrarEntrada;
    private JButton btnRegistrarSalida;
    private JButton btnLimpiar;
    private JButton btnCerrar;
    
    private Personal personalSeleccionado;

    // Modo simulación (solo administrador): registrar con otra fecha y hora
    private static final int ROL_ADMINISTRADOR = 1;
    private static final Color COLOR_TITULO = new Color(41, 128, 185);
    private static final Color COLOR_SIMULACION = new Color(243, 156, 18);
    private final boolean permiteSimulacion;
    private JPanel panelSimulacion;
    private JCheckBox chkSimular;
    private JDateChooser fechaSimulada;
    private JSpinner horaSimulada;
    private JButton btnBorrarSimulados;
    
    public RegistroAsistencia() {
        this(null);
    }

    public RegistroAsistencia(Usuario usuario) {
        this.permiteSimulacion = usuario != null && usuario.getIdRol() == ROL_ADMINISTRADOR;
        controlador = new ControladorAsistencia();
        inicializarComponentes();
        configurarVentana();
    }
    
    private void inicializarComponentes() {
        // Panel principal
        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior con título
        panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(41, 128, 185));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        lblTitulo = new JLabel("REGISTRO DE ASISTENCIA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.BLACK);
        panelSuperior.add(lblTitulo);
        
        // Panel de formulario de búsqueda
        panelFormulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Buscar Personal",
            0,
            0,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        lblDni = new JLabel("DNI:");
        lblDni.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtDni = new JTextField(15);
        txtDni.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        btnBuscar = new JButton("Buscar");
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBuscar.setBackground(new Color(52, 152, 219));
        btnBuscar.setForeground(Color.BLACK);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelFormulario.add(lblDni);
        panelFormulario.add(txtDni);
        panelFormulario.add(btnBuscar);
        
        // Panel de información del personal
        panelInformacion = new JPanel(new GridLayout(4, 2, 10, 10));
        panelInformacion.setBackground(Color.WHITE);
        panelInformacion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Información del Personal",
                0,
                0,
                new Font("Segoe UI", Font.BOLD, 14)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        lblNombre = new JLabel("Nombre Completo:");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtNombre = new JTextField();
        txtNombre.setEditable(false);
        txtNombre.setBackground(Color.LIGHT_GRAY);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        lblTipo = new JLabel("Tipo de Personal:");
        lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTipo = new JTextField();
        txtTipo.setEditable(false);
        txtTipo.setBackground(Color.LIGHT_GRAY);
        txtTipo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        lblCargo = new JLabel("Cargo:");
        lblCargo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtCargo = new JTextField();
        txtCargo.setEditable(false);
        txtCargo.setBackground(Color.LIGHT_GRAY);
        txtCargo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        lblHorario = new JLabel("Horario:");
        lblHorario.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtHorario = new JTextField();
        txtHorario.setEditable(false);
        txtHorario.setBackground(Color.LIGHT_GRAY);
        txtHorario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        panelInformacion.add(lblNombre);
        panelInformacion.add(txtNombre);
        panelInformacion.add(lblTipo);
        panelInformacion.add(txtTipo);
        panelInformacion.add(lblCargo);
        panelInformacion.add(txtCargo);
        panelInformacion.add(lblHorario);
        panelInformacion.add(txtHorario);
        
        // Panel de botones
        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        panelBotones.setBackground(Color.WHITE);
        
        btnRegistrarEntrada = new JButton("Registrar Entrada");
        btnRegistrarEntrada.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegistrarEntrada.setBackground(new Color(46, 204, 113));
        btnRegistrarEntrada.setForeground(Color.BLACK);
        btnRegistrarEntrada.setFocusPainted(false);
        btnRegistrarEntrada.setPreferredSize(new Dimension(180, 45));
        btnRegistrarEntrada.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegistrarEntrada.setEnabled(false);
        
        btnRegistrarSalida = new JButton("Registrar Salida");
        btnRegistrarSalida.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegistrarSalida.setBackground(new Color(230, 126, 34));
        btnRegistrarSalida.setForeground(Color.BLACK);
        btnRegistrarSalida.setFocusPainted(false);
        btnRegistrarSalida.setPreferredSize(new Dimension(180, 45));
        btnRegistrarSalida.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegistrarSalida.setEnabled(false);
        
        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLimpiar.setBackground(new Color(149, 165, 166));
        btnLimpiar.setForeground(Color.BLACK);
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.setPreferredSize(new Dimension(120, 45));
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrar.setBackground(new Color(231, 76, 60));
        btnCerrar.setForeground(Color.BLACK);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setPreferredSize(new Dimension(120, 45));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelBotones.add(btnRegistrarEntrada);
        panelBotones.add(btnRegistrarSalida);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnCerrar);
        
        // Agregar paneles al panel principal
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setBackground(Color.WHITE);
        panelCentro.add(panelFormulario, BorderLayout.NORTH);
        panelCentro.add(panelInformacion, BorderLayout.CENTER);
        if (permiteSimulacion) {
            panelCentro.add(crearPanelSimulacion(), BorderLayout.SOUTH);
        }
        
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
    }
    
    private void configurarEventos() {
        // Evento para buscar personal
        btnBuscar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarPersonal();
            }
        });
        
        // Evento para registrar entrada
        btnRegistrarEntrada.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarEntrada();
            }
        });
        
        // Evento para registrar salida
        btnRegistrarSalida.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarSalida();
            }
        });
        
        // Evento para limpiar
        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarCampos();
            }
        });
        
        // Evento para cerrar
        btnCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Evento Enter en campo DNI
        txtDni.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarPersonal();
            }
        });
    }
    
    private JPanel crearPanelSimulacion() {
        panelSimulacion = new JPanel(new GridBagLayout());
        panelSimulacion.setBackground(Color.WHITE);
        panelSimulacion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_SIMULACION),
                "Modo simulación (solo administrador)",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(156, 100, 12)
            ),
            BorderFactory.createEmptyBorder(6, 10, 8, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        chkSimular = new JCheckBox("Registrar con otra fecha y hora (para demostraciones)");
        chkSimular.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkSimular.setBackground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 5;
        panelSimulacion.add(chkSimular, gbc);

        JLabel lblFecha = new JLabel("Fecha:");
        lblFecha.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.gridx = 0;
        panelSimulacion.add(lblFecha, gbc);

        fechaSimulada = new JDateChooser(new java.util.Date());
        fechaSimulada.setDateFormatString("dd/MM/yyyy");
        fechaSimulada.setPreferredSize(new Dimension(140, 28));
        gbc.gridx = 1;
        panelSimulacion.add(fechaSimulada, gbc);

        JLabel lblHora = new JLabel("Hora:");
        lblHora.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 2;
        panelSimulacion.add(lblHora, gbc);

        java.util.Calendar ocho = java.util.Calendar.getInstance();
        ocho.set(java.util.Calendar.HOUR_OF_DAY, 8);
        ocho.set(java.util.Calendar.MINUTE, 0);
        horaSimulada = new JSpinner(new SpinnerDateModel(ocho.getTime(), null, null, java.util.Calendar.MINUTE));
        horaSimulada.setEditor(new JSpinner.DateEditor(horaSimulada, "HH:mm"));
        horaSimulada.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        horaSimulada.setPreferredSize(new Dimension(80, 28));
        gbc.gridx = 3;
        panelSimulacion.add(horaSimulada, gbc);

        btnBorrarSimulados = new JButton("Borrar registros simulados");
        btnBorrarSimulados.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBorrarSimulados.setFocusPainted(false);
        btnBorrarSimulados.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBorrarSimulados.setToolTipText("Elimina solo los registros marcados como [SIMULADO]");
        gbc.gridx = 4; gbc.weightx = 1; gbc.anchor = GridBagConstraints.EAST;
        panelSimulacion.add(btnBorrarSimulados, gbc);

        JLabel lblNota = new JLabel("Se aplican las mismas reglas de horario, tardanza y horas extra. "
                + "Los registros quedan marcados como [SIMULADO].");
        lblNota.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNota.setForeground(new Color(110, 110, 110));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 5; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST;
        panelSimulacion.add(lblNota, gbc);

        chkSimular.addActionListener(e -> actualizarModoSimulacion());
        btnBorrarSimulados.addActionListener(e -> borrarRegistrosSimulados());
        actualizarModoSimulacion();
        return panelSimulacion;
    }

    private boolean simulacionActiva() {
        return permiteSimulacion && chkSimular != null && chkSimular.isSelected();
    }

    private void actualizarModoSimulacion() {
        boolean activa = simulacionActiva();
        fechaSimulada.setEnabled(activa);
        horaSimulada.setEnabled(activa);
        panelSuperior.setBackground(activa ? COLOR_SIMULACION : COLOR_TITULO);
        lblTitulo.setText(activa ? "REGISTRO DE ASISTENCIA — SIMULACIÓN" : "REGISTRO DE ASISTENCIA");
        panelSimulacion.setBackground(activa ? new Color(255, 248, 225) : Color.WHITE);
        chkSimular.setBackground(panelSimulacion.getBackground());
        int cantidad = controlador.contarRegistrosSimulados();
        btnBorrarSimulados.setText("Borrar registros simulados (" + cantidad + ")");
        btnBorrarSimulados.setEnabled(cantidad > 0);
    }

    // Fecha y hora elegidas en el modo simulación, o null si faltan datos
    private LocalDateTime momentoSimulado() {
        java.util.Date fecha = fechaSimulada.getDate();
        if (fecha == null) {
            JOptionPane.showMessageDialog(this,
                "Seleccione la fecha a simular",
                "Fecha requerida",
                JOptionPane.WARNING_MESSAGE);
            return null;
        }
        LocalDate dia = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        java.util.Date hora = (java.util.Date) horaSimulada.getValue();
        LocalTime t = hora.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
        return LocalDateTime.of(dia, t);
    }

    private void borrarRegistrosSimulados() {
        int cantidad = controlador.contarRegistrosSimulados();
        if (cantidad == 0) {
            actualizarModoSimulacion();
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
            "Se eliminarán " + cantidad + " registro(s) de asistencia marcados como [SIMULADO].\n"
            + "Los registros reales no se modifican.\n\n¿Desea continuar?",
            "Borrar registros simulados",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            int borrados = controlador.eliminarRegistrosSimulados();
            JOptionPane.showMessageDialog(this,
                "Se eliminaron " + borrados + " registro(s) simulados.",
                "Listo",
                JOptionPane.INFORMATION_MESSAGE);
        }
        actualizarModoSimulacion();
    }

    private void buscarPersonal() {
        String dni = txtDni.getText().trim();
        
        if (dni.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor ingrese un DNI",
                "Campo vacío",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (dni.length() != 8) {
            JOptionPane.showMessageDialog(this,
                "El DNI debe tener 8 dígitos",
                "DNI inválido",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        personalSeleccionado = controlador.buscarPersonal(dni);
        
        if (personalSeleccionado != null) {
            mostrarDatosPersonal();
            btnRegistrarEntrada.setEnabled(true);
            btnRegistrarSalida.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this,
                "No se encontró personal con el DNI: " + dni,
                "Personal no encontrado",
                JOptionPane.WARNING_MESSAGE);
            limpiarCampos();
        }
    }
    
    private void mostrarDatosPersonal() {
        txtNombre.setText(personalSeleccionado.getNombreCompleto());
        txtTipo.setText(personalSeleccionado.getTipoPersonal());
        txtCargo.setText(personalSeleccionado.getCargo());
        txtHorario.setText(controlador.obtenerHorarioTexto(personalSeleccionado));
        txtHorario.setToolTipText(txtHorario.getText());
    }
    
    private void registrarEntrada() {
        if (personalSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                "Debe buscar un personal primero",
                "Sin personal",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        boolean resultado;
        if (simulacionActiva()) {
            LocalDateTime momento = momentoSimulado();
            if (momento == null) {
                return;
            }
            resultado = controlador.registrarEntrada(personalSeleccionado.getDni(), momento, true);
            actualizarModoSimulacion();
        } else {
            resultado = controlador.registrarEntrada(personalSeleccionado.getDni());
        }
        
        if (resultado && !simulacionActiva()) {
            limpiarCampos();
        }
    }
    
    private void registrarSalida() {
        if (personalSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                "Debe buscar un personal primero",
                "Sin personal",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        boolean resultado;
        if (simulacionActiva()) {
            LocalDateTime momento = momentoSimulado();
            if (momento == null) {
                return;
            }
            resultado = controlador.registrarSalida(personalSeleccionado.getDni(), momento, true);
            actualizarModoSimulacion();
        } else {
            resultado = controlador.registrarSalida(personalSeleccionado.getDni());
        }
        
        if (resultado && !simulacionActiva()) {
            limpiarCampos();
        }
    }
    
    private void limpiarCampos() {
        txtDni.setText("");
        txtNombre.setText("");
        txtTipo.setText("");
        txtCargo.setText("");
        txtHorario.setText("");
        personalSeleccionado = null;
        btnRegistrarEntrada.setEnabled(false);
        btnRegistrarSalida.setEnabled(false);
        txtDni.requestFocus();
    }
    
    private void configurarVentana() {
        this.setTitle("Registro de Asistencia");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelPrincipal);
        this.setSize(760, permiteSimulacion ? 690 : 550);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }
}
