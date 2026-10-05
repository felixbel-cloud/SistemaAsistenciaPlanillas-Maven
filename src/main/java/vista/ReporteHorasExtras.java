// vista/ReporteHorasExtras.java
package vista;

import controlador.ControladorReporte;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class ReporteHorasExtras extends JFrame {
    private ControladorReporte controlador;
    
    // Componentes
    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JPanel panelFiltros;
    private JPanel panelTabla;
    private JPanel panelBotones;
    
    private JLabel lblTitulo;
    private JLabel lblFechaInicio;
    private JLabel lblFechaFin;
    
    private JDateChooser dateInicio;
    private JDateChooser dateFin;
    
    private JButton btnGenerar;
    private JButton btnExportar;
    private JButton btnExportarPdf;
    private JButton btnCerrar;
    
    private JTable tablaHorasExtras;
    private JScrollPane scrollPane;
    private DefaultTableModel modeloTabla;
    
    public ReporteHorasExtras() {
        controlador = new ControladorReporte();
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
        panelSuperior.setBackground(new Color(39, 174, 96));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        lblTitulo = new JLabel("REPORTE DE HORAS EXTRAS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.BLACK);
        panelSuperior.add(lblTitulo);
        
        // Panel de filtros
        panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Periodo de Análisis",
            0,
            0,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        lblFechaInicio = new JLabel("Fecha Inicio:");
        lblFechaInicio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        dateInicio = new JDateChooser();
        dateInicio.setDateFormatString("dd/MM/yyyy");
        dateInicio.setPreferredSize(new Dimension(150, 30));
        
        lblFechaFin = new JLabel("Fecha Fin:");
        lblFechaFin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        dateFin = new JDateChooser();
        dateFin.setDateFormatString("dd/MM/yyyy");
        dateFin.setPreferredSize(new Dimension(150, 30));
        dateFin.setDate(new Date());
        
        btnGenerar = new JButton("Generar Reporte");
        btnGenerar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGenerar.setBackground(new Color(46, 204, 113));
        btnGenerar.setForeground(Color.BLACK);
        btnGenerar.setFocusPainted(false);
        btnGenerar.setPreferredSize(new Dimension(160, 35));
        btnGenerar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelFiltros.add(lblFechaInicio);
        panelFiltros.add(dateInicio);
        panelFiltros.add(lblFechaFin);
        panelFiltros.add(dateFin);
        panelFiltros.add(btnGenerar);
        
        // Panel de tabla
        panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBackground(Color.WHITE);
        
        String[] columnas = {"DNI", "Nombre Completo", "Cargo", "Minutos Extras", "Horas Extras", "Monto Estimado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaHorasExtras = new JTable(modeloTabla);
        tablaHorasExtras.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaHorasExtras.setRowHeight(25);
        tablaHorasExtras.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaHorasExtras.getTableHeader().setBackground(new Color(39, 174, 96));
        tablaHorasExtras.getTableHeader().setForeground(Color.BLACK);
        tablaHorasExtras.setSelectionBackground(new Color(39, 174, 96));
        tablaHorasExtras.setSelectionForeground(Color.BLACK);
        
        scrollPane = new JScrollPane(tablaHorasExtras);
        panelTabla.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones
        panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBotones.setBackground(Color.WHITE);
        
        btnExportar = new JButton("Exportar a Excel");
        btnExportar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExportar.setBackground(new Color(39, 174, 96));
        btnExportar.setForeground(Color.BLACK);
        btnExportar.setFocusPainted(false);
        btnExportar.setPreferredSize(new Dimension(150, 35));
        btnExportar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnExportarPdf = new JButton("Exportar a PDF");
        btnExportarPdf.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExportarPdf.setBackground(new Color(192, 57, 43));
        btnExportarPdf.setForeground(Color.BLACK);
        btnExportarPdf.setFocusPainted(false);
        btnExportarPdf.setPreferredSize(new Dimension(150, 35));
        btnExportarPdf.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrar.setBackground(new Color(231, 76, 60));
        btnCerrar.setForeground(Color.BLACK);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setPreferredSize(new Dimension(100, 35));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelBotones.add(btnExportar);
        panelBotones.add(btnExportarPdf);
        panelBotones.add(btnCerrar);
        
        // Agregar paneles
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(Color.WHITE);
        panelNorte.add(panelSuperior, BorderLayout.NORTH);
        panelNorte.add(panelFiltros, BorderLayout.CENTER);
        
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);
        panelPrincipal.add(panelTabla, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar fecha inicial (30 días atrás)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -30);
        dateInicio.setDate(cal.getTime());
        
        // Configurar eventos
        configurarEventos();
    }
    
    private void configurarEventos() {
        btnGenerar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarReporte();
            }
        });
        
        btnExportar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportarReporte();
            }
        });

        btnExportarPdf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportarReportePDF();
            }
        });
        
        btnCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void generarReporte() {
        Date fechaInicio = dateInicio.getDate();
        Date fechaFin = dateFin.getDate();
        
        if (fechaInicio == null || fechaFin == null) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione ambas fechas",
                "Fechas incompletas",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (fechaInicio.after(fechaFin)) {
            JOptionPane.showMessageDialog(this,
                "La fecha de inicio no puede ser mayor a la fecha fin",
                "Fechas inválidas",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        controlador.generarReporteHorasExtras(tablaHorasExtras, fechaInicio, fechaFin);
        
        if (tablaHorasExtras.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No se encontraron horas extras en el periodo seleccionado",
                "Sin resultados",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void exportarReporte() {
        if (tablaHorasExtras.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay datos para exportar",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        util.ExportadorReportes.exportarAExcel(tablaHorasExtras, "Reporte_HorasExtras");
    }

    private void exportarReportePDF() {
        if (tablaHorasExtras.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay datos para exportar",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        util.ExportadorReportes.exportarAPDF(tablaHorasExtras, "Reporte de Horas Extras", "Reporte_HorasExtras");
    }
    
    private void configurarVentana() {
        this.setTitle("Reporte de Horas Extras");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelPrincipal);
        this.setSize(1000, 600);
        this.setLocationRelativeTo(null);
    }
}