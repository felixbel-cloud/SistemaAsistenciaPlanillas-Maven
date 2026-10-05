// vista/Login.java
package vista;

import controlador.ControladorLogin;
import javax.swing.*;
import java.awt.*;

public class Login extends JFrame {
    // Componentes
    private JPanel panelPrincipal;
    private JPanel panelFormulario;
    private JPanel panelBotones;
    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    private JLabel lblUsuario;
    private JLabel lblPassword;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnSalir;
    private JLabel lblLogo;
    
    public Login() {
        inicializarComponentes();
        configurarVentana();
        
        // Inicializar controlador
        ControladorLogin controlador = new ControladorLogin(this);
    }
    
    private void inicializarComponentes() {
        // Panel principal con degradado
        panelPrincipal = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(41, 128, 185);
                Color color2 = new Color(109, 213, 250);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelPrincipal.setLayout(new GridBagLayout());
        
        // Panel del formulario
        panelFormulario = new JPanel();
        panelFormulario.setLayout(new GridBagLayout());
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Logo o icono
        lblLogo = new JLabel("🏫", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panelFormulario.add(lblLogo, gbc);
        
        // Título
        lblTitulo = new JLabel("SISTEMA DE ASISTENCIAS", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(41, 128, 185));
        gbc.gridy = 1;
        panelFormulario.add(lblTitulo, gbc);
        
        // Subtítulo
        lblSubtitulo = new JLabel("Instituto Americano", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(Color.GRAY);
        gbc.gridy = 2;
        panelFormulario.add(lblSubtitulo, gbc);
        
        // Etiqueta Usuario
        lblUsuario = new JLabel("Usuario:");
        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panelFormulario.add(lblUsuario, gbc);
        
        // Campo Usuario
        txtUsuario = new JTextField(20);
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 1;
        panelFormulario.add(txtUsuario, gbc);
        
        // Etiqueta Contraseña
        lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 4;
        gbc.gridx = 0;
        panelFormulario.add(lblPassword, gbc);
        
        // Campo Contraseña
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        gbc.gridx = 1;
        panelFormulario.add(txtPassword, gbc);
        
        // Panel de botones
        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(Color.WHITE);
        
        // Botón Ingresar
        btnIngresar = new JButton("Ingresar");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIngresar.setBackground(new Color(46, 204, 113));
        btnIngresar.setForeground(Color.BLACK);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setPreferredSize(new Dimension(120, 40));
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelBotones.add(btnIngresar);
        
        // Botón Salir
        btnSalir = new JButton("Salir");
        btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSalir.setBackground(new Color(231, 76, 60));
        btnSalir.setForeground(Color.BLACK);
        btnSalir.setFocusPainted(false);
        btnSalir.setBorderPainted(false);
        btnSalir.setPreferredSize(new Dimension(120, 40));
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelBotones.add(btnSalir);
        
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        panelFormulario.add(panelBotones, gbc);
        
        // Agregar panel de formulario al panel principal
        panelPrincipal.add(panelFormulario);
    }
    
    private void configurarVentana() {
        this.setTitle("Login - Sistema de Asistencias");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelPrincipal);
        this.setSize(500, 550);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }
    
    // Getters para los componentes
    public JTextField getTxtUsuario() {
        return txtUsuario;
    }
    
    public JPasswordField getTxtPassword() {
        return txtPassword;
    }
    
    public JButton getBtnIngresar() {
        return btnIngresar;
    }
    
    public JButton getBtnSalir() {
        return btnSalir;
    }
}