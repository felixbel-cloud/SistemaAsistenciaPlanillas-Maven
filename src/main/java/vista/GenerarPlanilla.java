// vista/GenerarPlanilla.java
package vista;

import controlador.ControladorPlanilla;
import modelo.entidades.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GenerarPlanilla extends JFrame {
    private ControladorPlanilla controlador;
    private Usuario usuarioActual;
    
    // Componentes
    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JPanel panelFormulario;
    private JPanel panelBotones;
    
    private JLabel lblTitulo;
    private JLabel lblPeriodo;
    private JLabel lblInstrucciones;
    
    private JComboBox<String> cmbMes;
    private JComboBox<String> cmbAnio;
    
    private JButton btnGenerar;
    private JButton btnCancelar;
    
    private final String[] meses = {
        "01 - Enero", "02 - Febrero", "03 - Marzo", "04 - Abril",
        "05 - Mayo", "06 - Junio", "07 - Julio", "08 - Agosto",
        "09 - Septiembre", "10 - Octubre", "11 - Noviembre", "12 - Diciembre"
    };
    
    public GenerarPlanilla(Usuario usuario) {
        this.usuarioActual = usuario;
        this.controlador = new ControladorPlanilla();
        inicializarComponentes();
        configurarVentana();
    }
    
    private void inicializarComponentes() {
        // Panel principal
        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior
        panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setBackground(new Color(155, 89, 182));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        lblTitulo = new JLabel("GENERAR PLANILLA MENSUAL");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.BLACK);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        lblInstrucciones = new JLabel("Seleccione el periodo para generar la planilla");
        lblInstrucciones.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInstrucciones.setForeground(Color.BLACK);
        lblInstrucciones.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panelSuperior.add(lblTitulo);
        panelSuperior.add(Box.createRigidArea(new Dimension(0, 10)));
        panelSuperior.add(lblInstrucciones);
        
        // Panel de formulario
        panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Periodo",
                0,
                0,
                new Font("Segoe UI", Font.BOLD, 14)
            ),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        lblPeriodo = new JLabel("Seleccione Mes y Año:");
        lblPeriodo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panelFormulario.add(lblPeriodo, gbc);
        
        // ComboBox Mes
        cmbMes = new JComboBox<>(meses);
        cmbMes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbMes.setPreferredSize(new Dimension(200, 35));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panelFormulario.add(cmbMes, gbc);
        
        // ComboBox Año
        String[] anios = generarAnios();
        cmbAnio = new JComboBox<>(anios);
        cmbAnio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbAnio.setPreferredSize(new Dimension(150, 35));
        gbc.gridx = 1;
        panelFormulario.add(cmbAnio, gbc);
        
        // Panel de botones
        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panelBotones.setBackground(Color.WHITE);
        
        btnGenerar = new JButton("Generar Planilla");
        btnGenerar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnGenerar.setBackground(new Color(46, 204, 113));
        btnGenerar.setForeground(Color.BLACK);
        btnGenerar.setFocusPainted(false);
        btnGenerar.setPreferredSize(new Dimension(180, 50));
        btnGenerar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancelar.setBackground(new Color(231, 76, 60));
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setPreferredSize(new Dimension(150, 50));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelBotones.add(btnGenerar);
        panelBotones.add(btnCancelar);
        
        // Agregar paneles
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
        
        // Establecer periodo actual
        establecerPeriodoActual();
    }
    
    private String[] generarAnios() {
        int anioActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String[] anios = new String[5];
        for (int i = 0; i < 5; i++) {
            anios[i] = String.valueOf(anioActual - 2 + i);
        }
        return anios;
    }
    
    private void establecerPeriodoActual() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int mesActual = cal.get(java.util.Calendar.MONTH);
        cmbMes.setSelectedIndex(mesActual);
        
        int anioActual = cal.get(java.util.Calendar.YEAR);
        cmbAnio.setSelectedItem(String.valueOf(anioActual));
    }
    
    private void configurarEventos() {
        btnGenerar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarPlanilla();
            }
        });
        
        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void generarPlanilla() {
        String mesSeleccionado = cmbMes.getSelectedItem().toString().substring(0, 2);
        String anioSeleccionado = cmbAnio.getSelectedItem().toString();
        String periodo = anioSeleccionado + "-" + mesSeleccionado;
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de generar la planilla del periodo " + periodo + "?\n" +
            "Este proceso calculará automáticamente:\n" +
            "- Días trabajados\n" +
            "- Horas extras\n" +
            "- Tardanzas\n" +
            "- Ausencias\n" +
            "- Descuentos y bonificaciones",
            "Confirmar generación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            // Mostrar indicador de progreso
            JDialog dialogoProgreso = new JDialog(this, "Generando Planilla", true);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.add(new JLabel("Generando planilla, por favor espere..."), BorderLayout.NORTH);
            panel.add(progressBar, BorderLayout.CENTER);
            dialogoProgreso.add(panel);
            dialogoProgreso.setSize(350, 120);
            dialogoProgreso.setLocationRelativeTo(this);
            
            // Ejecutar en hilo separado
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return controlador.generarPlanilla(periodo, usuarioActual.getIdUsuario());
                }
                
                @Override
                protected void done() {
                    dialogoProgreso.dispose();
                    try {
                        Boolean resultado = get();
                        if (resultado) {
                            abrirConsultaPlanilla();
                            dispose();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(GenerarPlanilla.this,
                            "Error al generar planilla: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
            dialogoProgreso.setVisible(true);
        }
    }
    
    void abrirConsultaPlanilla() {
        ConsultaPlanilla ventana = new ConsultaPlanilla();
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    private void configurarVentana() {
        this.setTitle("Generar Planilla");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelPrincipal);
        this.setSize(600, 500);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }
}