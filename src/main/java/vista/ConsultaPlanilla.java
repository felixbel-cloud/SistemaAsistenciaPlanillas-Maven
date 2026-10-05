// vista/ConsultaPlanilla.java
package vista;

import controlador.ControladorPlanilla;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConsultaPlanilla extends JFrame {
    private ControladorPlanilla controlador;
    
    // Componentes
    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JPanel panelTablas;
    private JPanel panelBotones;
    
    private JLabel lblTitulo;
    
    private JTable tablaPlanillas;
    private JTable tablaDetalle;
    private JScrollPane scrollPlanillas;
    private JScrollPane scrollDetalle;
    private DefaultTableModel modeloPlanillas;
    private DefaultTableModel modeloDetalle;
    
    private JButton btnVerDetalle;
    private JButton btnExportarDetalle;
    private JButton btnExportarDetallePdf;
    private JButton btnCambiarEstado;
    private JButton btnActualizar;
    private JButton btnCerrar;
    
    private int idPlanillaSeleccionada = -1;
    
    public ConsultaPlanilla() {
        controlador = new ControladorPlanilla();
        inicializarComponentes();
        configurarVentana();
        cargarPlanillas();
    }
    
    private void inicializarComponentes() {
        // Panel principal
        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior
        panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(155, 89, 182));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        lblTitulo = new JLabel("CONSULTA DE PLANILLAS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.BLACK);
        panelSuperior.add(lblTitulo);
        
        // Panel de tablas (dividido)
        panelTablas = new JPanel(new GridLayout(2, 1, 10, 10));
        panelTablas.setBackground(Color.WHITE);
        
        // Tabla de planillas (resumen)
        JPanel panelTablaPlanillas = new JPanel(new BorderLayout());
        panelTablaPlanillas.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Lista de Planillas Generadas",
            0,
            0,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        String[] columnasPlanillas = {"ID", "Periodo", "Fecha Generación", "Total", "Estado"};
        modeloPlanillas = new DefaultTableModel(columnasPlanillas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaPlanillas = new JTable(modeloPlanillas);
        tablaPlanillas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaPlanillas.setRowHeight(25);
        tablaPlanillas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaPlanillas.getTableHeader().setBackground(new Color(155, 89, 182));
        tablaPlanillas.getTableHeader().setForeground(Color.BLACK);
        tablaPlanillas.setSelectionBackground(new Color(155, 89, 182));
        tablaPlanillas.setSelectionForeground(Color.BLACK);
        tablaPlanillas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        scrollPlanillas = new JScrollPane(tablaPlanillas);
        panelTablaPlanillas.add(scrollPlanillas, BorderLayout.CENTER);
        
        // Tabla de detalle de planilla
        JPanel panelTablaDetalle = new JPanel(new BorderLayout());
        panelTablaDetalle.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Detalle de Planilla Seleccionada",
            0,
            0,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        String[] columnasDetalle = {
            "DNI", "Nombre", "Cargo", "Días Trab.", "Horas Extra", "Monto Extra",
            "Días Aus.", "Desc. Aus.", "Min. Tard.", "Desc. Tard.",
            "Total Ing.", "Total Desc.", "Sueldo Neto"
        };
        modeloDetalle = new DefaultTableModel(columnasDetalle, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaDetalle = new JTable(modeloDetalle);
        tablaDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tablaDetalle.setRowHeight(25);
        tablaDetalle.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tablaDetalle.getTableHeader().setBackground(new Color(155, 89, 182));
        tablaDetalle.getTableHeader().setForeground(Color.BLACK);
        tablaDetalle.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Ajustar anchos de columnas
        tablaDetalle.getColumnModel().getColumn(0).setPreferredWidth(80);  // DNI
        tablaDetalle.getColumnModel().getColumn(1).setPreferredWidth(200); // Nombre
        tablaDetalle.getColumnModel().getColumn(2).setPreferredWidth(150); // Cargo
        for (int i = 3; i < columnasDetalle.length; i++) {
            tablaDetalle.getColumnModel().getColumn(i).setPreferredWidth(100);
        }
        
        scrollDetalle = new JScrollPane(tablaDetalle);
        panelTablaDetalle.add(scrollDetalle, BorderLayout.CENTER);
        
        panelTablas.add(panelTablaPlanillas);
        panelTablas.add(panelTablaDetalle);
        
        // Panel de botones
        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setBackground(Color.WHITE);
        
        btnVerDetalle = new JButton("Ver Detalle");
        btnVerDetalle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnVerDetalle.setBackground(new Color(52, 152, 219));
        btnVerDetalle.setForeground(Color.BLACK);
        btnVerDetalle.setFocusPainted(false);
        btnVerDetalle.setPreferredSize(new Dimension(140, 35));
        btnVerDetalle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnExportarDetalle = new JButton("Exportar Detalle");
        btnExportarDetalle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExportarDetalle.setBackground(new Color(39, 174, 96));
        btnExportarDetalle.setForeground(Color.BLACK);
        btnExportarDetalle.setFocusPainted(false);
        btnExportarDetalle.setPreferredSize(new Dimension(160, 35));
        btnExportarDetalle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportarDetalle.setEnabled(false);

        btnExportarDetallePdf = new JButton("Exportar Detalle PDF");
        btnExportarDetallePdf.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExportarDetallePdf.setBackground(new Color(192, 57, 43));
        btnExportarDetallePdf.setForeground(Color.BLACK);
        btnExportarDetallePdf.setFocusPainted(false);
        btnExportarDetallePdf.setPreferredSize(new Dimension(170, 35));
        btnExportarDetallePdf.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportarDetallePdf.setEnabled(false);
        
        btnCambiarEstado = new JButton("Cambiar Estado");
        btnCambiarEstado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCambiarEstado.setBackground(new Color(230, 126, 34));
        btnCambiarEstado.setForeground(Color.BLACK);
        btnCambiarEstado.setFocusPainted(false);
        btnCambiarEstado.setPreferredSize(new Dimension(160, 35));
        btnCambiarEstado.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCambiarEstado.setEnabled(false);
        
        btnActualizar = new JButton("Actualizar");
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnActualizar.setBackground(new Color(149, 165, 166));
        btnActualizar.setForeground(Color.BLACK);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setPreferredSize(new Dimension(120, 35));
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrar.setBackground(new Color(231, 76, 60));
        btnCerrar.setForeground(Color.BLACK);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setPreferredSize(new Dimension(100, 35));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelBotones.add(btnVerDetalle);
        panelBotones.add(btnExportarDetalle);
        panelBotones.add(btnExportarDetallePdf);
        panelBotones.add(btnCambiarEstado);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnCerrar);
        
        // Agregar paneles
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelTablas, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
    }
    
    private void configurarEventos() {
        // Evento de selección en tabla de planillas
        tablaPlanillas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tablaPlanillas.getSelectedRow();
                if (selectedRow >= 0) {
                    btnVerDetalle.setEnabled(true);
                    btnCambiarEstado.setEnabled(true);
                } else {
                    btnVerDetalle.setEnabled(false);
                    btnCambiarEstado.setEnabled(false);
                    btnExportarDetalle.setEnabled(false);
                    btnExportarDetallePdf.setEnabled(false);
                }
            }
        });
        
        btnVerDetalle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verDetallePlanilla();
            }
        });
        
        btnExportarDetalle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportarDetalle();
            }
        });

        btnExportarDetallePdf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportarDetallePDF();
            }
        });
        
        btnCambiarEstado.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cambiarEstadoPlanilla();
            }
        });
        
        btnActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarPlanillas();
                limpiarDetalle();
            }
        });
        
        btnCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void cargarPlanillas() {
        controlador.cargarPlanillas(tablaPlanillas);
    }
    
    private void verDetallePlanilla() {
        int selectedRow = tablaPlanillas.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una planilla",
                "Selección requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        idPlanillaSeleccionada = (int) modeloPlanillas.getValueAt(selectedRow, 0);
        controlador.cargarDetallePlanilla(tablaDetalle, idPlanillaSeleccionada);
        btnExportarDetalle.setEnabled(true);
        btnExportarDetallePdf.setEnabled(true);
    }
    
    private void exportarDetalle() {
        if (tablaDetalle.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay detalle para exportar",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = tablaPlanillas.getSelectedRow();
        String periodo = (String) modeloPlanillas.getValueAt(selectedRow, 1);
        
        util.ExportadorReportes.exportarAExcel(tablaDetalle, "Detalle_Planilla_" + periodo);
    }

    private void exportarDetallePDF() {
        if (tablaDetalle.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay detalle para exportar",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = tablaPlanillas.getSelectedRow();
        String periodo = (String) modeloPlanillas.getValueAt(selectedRow, 1);

        util.ExportadorReportes.exportarAPDF(tablaDetalle, "Detalle de Planilla - Periodo " + periodo,
                "Detalle_Planilla_" + periodo);
    }
    
    private void cambiarEstadoPlanilla() {
        int selectedRow = tablaPlanillas.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una planilla",
                "Selección requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idPlanilla = (int) modeloPlanillas.getValueAt(selectedRow, 0);
        String estadoActual = (String) modeloPlanillas.getValueAt(selectedRow, 4);
        
        String[] opciones = {"GENERADA", "PAGADA", "ANULADA"};
        String nuevoEstado = (String) JOptionPane.showInputDialog(
            this,
            "Estado actual: " + estadoActual + "\nSeleccione el nuevo estado:",
            "Cambiar Estado",
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones,
            estadoActual
        );
        
        if (nuevoEstado != null && !nuevoEstado.equals(estadoActual)) {
            int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de cambiar el estado a: " + nuevoEstado + "?",
                "Confirmar cambio",
                JOptionPane.YES_NO_OPTION);
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                boolean resultado = controlador.cambiarEstadoPlanilla(idPlanilla, nuevoEstado);
                if (resultado) {
                    cargarPlanillas();
                }
            }
        }
    }
    
    private void limpiarDetalle() {
        modeloDetalle.setRowCount(0);
        btnExportarDetalle.setEnabled(false);
        btnExportarDetallePdf.setEnabled(false);
        idPlanillaSeleccionada = -1;
    }
    
    private void configurarVentana() {
        this.setTitle("Consulta de Planillas");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelPrincipal);
        this.setSize(1200, 700);
        this.setLocationRelativeTo(null);
    }
}
