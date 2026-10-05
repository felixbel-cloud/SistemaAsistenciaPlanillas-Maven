// vista/RegistroAsistencia.java
package vista;

import controlador.ControladorAsistencia;
import modelo.entidades.Personal;
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
    
    public RegistroAsistencia() {
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
        
        boolean resultado = controlador.registrarEntrada(personalSeleccionado.getDni());
        
        if (resultado) {
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
        
        boolean resultado = controlador.registrarSalida(personalSeleccionado.getDni());
        
        if (resultado) {
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
        this.setSize(700, 550);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }
}
