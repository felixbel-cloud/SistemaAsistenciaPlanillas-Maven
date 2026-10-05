// vista/ReporteAsistencia.java
package vista;

import controlador.ControladorReporte;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Map;

public class ReporteAsistencia extends JFrame {
    private ControladorReporte controlador;
    
    // Componentes
    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JPanel panelFiltros;
    private JPanel panelEstadisticas;
    private JPanel panelTabla;
    private JPanel panelBotones;
    
    private JLabel lblTitulo;
    private JLabel lblFechaInicio;
    private JLabel lblFechaFin;
    private JLabel lblTipoPersonal;
    
    private JDateChooser dateInicio;
    private JDateChooser dateFin;
    private JComboBox<String> cmbTipoPersonal;
    
    private JLabel lblTotalRegistros;
    private JLabel lblTotalPresentes;
    private JLabel lblTotalTardanzas;
    private JLabel lblTotalAusencias;
    private JLabel lblPorcentajeAsistencia;
    
    private JButton btnGenerar;
    private JButton btnExportar;
    private JButton btnExportarPdf;
    private JButton btnCerrar;
    
    private JTable tablaReporte;
    private JScrollPane scrollPane;
    private DefaultTableModel modeloTabla;
    
    public ReporteAsistencia() {
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
        panelSuperior.setBackground(new Color(230, 126, 34));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        lblTitulo = new JLabel("REPORTE DE ASISTENCIAS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.BLACK);
        panelSuperior.add(lblTitulo);
        
        // Panel de filtros
        panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Filtros",
            0,
            0,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        lblFechaInicio = new JLabel("Fecha Inicio:");
        lblFechaInicio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        dateInicio = new JDateChooser();
        dateInicio.setDateFormatString("dd/MM/yyyy");
        dateInicio.setPreferredSize(new Dimension(140, 30));
        
        lblFechaFin = new JLabel("Fecha Fin:");
        lblFechaFin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        dateFin = new JDateChooser();
        dateFin.setDateFormatString("dd/MM/yyyy");
        dateFin.setPreferredSize(new Dimension(140, 30));
        dateFin.setDate(new Date());
        
        lblTipoPersonal = new JLabel("Tipo:");
        lblTipoPersonal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        cmbTipoPersonal = new JComboBox<>(new String[]{"TODOS", "ADMINISTRATIVO", "DOCENTE"});
        cmbTipoPersonal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbTipoPersonal.setPreferredSize(new Dimension(150, 30));
        
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
        panelFiltros.add(lblTipoPersonal);
        panelFiltros.add(cmbTipoPersonal);
        panelFiltros.add(btnGenerar);
        
        // Panel de estadísticas
        panelEstadisticas = new JPanel(new GridLayout(1, 5, 15, 10));
        panelEstadisticas.setBackground(Color.WHITE);
        panelEstadisticas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        lblTotalRegistros = crearEtiquetaEstadistica("Total Registros", "0", new Color(52, 152, 219));
        lblTotalPresentes = crearEtiquetaEstadistica("Presentes", "0", new Color(46, 204, 113));
        lblTotalTardanzas = crearEtiquetaEstadistica("Tardanzas", "0", new Color(230, 126, 34));
        lblTotalAusencias = crearEtiquetaEstadistica("Ausencias", "0", new Color(231, 76, 60));
        lblPorcentajeAsistencia = crearEtiquetaEstadistica("% Asistencia", "0%", new Color(155, 89, 182));
        
        panelEstadisticas.add(lblTotalRegistros);
        panelEstadisticas.add(lblTotalPresentes);
        panelEstadisticas.add(lblTotalTardanzas);
        panelEstadisticas.add(lblTotalAusencias);
        panelEstadisticas.add(lblPorcentajeAsistencia);
        
        // Panel de tabla
        panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBackground(Color.WHITE);
        
        String[] columnas = {"DNI", "Nombre", "Tipo", "Fecha", "Entrada", "Salida", "Estado", "Tardanza", "Horas Extras"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaReporte = new JTable(modeloTabla);
        tablaReporte.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaReporte.setRowHeight(25);
        tablaReporte.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaReporte.getTableHeader().setBackground(new Color(230, 126, 34));
        tablaReporte.getTableHeader().setForeground(Color.BLACK);
        
        scrollPane = new JScrollPane(tablaReporte);
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
        panelNorte.add(panelEstadisticas, BorderLayout.SOUTH);
        
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
    
    private JLabel crearEtiquetaEstadistica(String titulo, String valor, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(Color.BLACK);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValor.setForeground(Color.BLACK);
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(lblTitulo);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(lblValor);
        
        // Guardar referencia al JLabel de valor para poder actualizarlo
        JLabel contenedor = new JLabel();
        contenedor.setLayout(new BorderLayout());
        contenedor.add(panel);
        contenedor.putClientProperty("valorLabel", lblValor);
        
        return contenedor;
    }
    
    private void actualizarEstadistica(JLabel contenedor, String nuevoValor) {
        JLabel lblValor = (JLabel) contenedor.getClientProperty("valorLabel");
        if (lblValor != null) {
            lblValor.setText(nuevoValor);
        }
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
        
        String tipoPersonal = cmbTipoPersonal.getSelectedItem().toString();
        
        // Generar reporte
        controlador.generarReporteAsistencia(tablaReporte, fechaInicio, fechaFin, tipoPersonal);
        
        // Actualizar estadísticas
        Map<String, Object> resumen = controlador.generarResumenEstadistico(fechaInicio, fechaFin);
        
        actualizarEstadistica(lblTotalRegistros, resumen.get("totalRegistros").toString());
        actualizarEstadistica(lblTotalPresentes, resumen.get("totalPresentes").toString());
        actualizarEstadistica(lblTotalTardanzas, resumen.get("totalTardanzas").toString());
        actualizarEstadistica(lblTotalAusencias, resumen.get("totalAusencias").toString());
        actualizarEstadistica(lblPorcentajeAsistencia, 
            String.format("%.1f%%", resumen.get("porcentajeAsistencia")));
    }
    
    private void exportarReporte() {
        if (tablaReporte.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay datos para exportar",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        util.ExportadorReportes.exportarAExcel(tablaReporte, "Reporte_Asistencias");
    }

    private void exportarReportePDF() {
        if (tablaReporte.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay datos para exportar",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        util.ExportadorReportes.exportarAPDF(tablaReporte, "Reporte de Asistencias", "Reporte_Asistencias");
    }
    
    private void configurarVentana() {
        this.setTitle("Reporte de Asistencias");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelPrincipal);
        this.setSize(1200, 700);
        this.setLocationRelativeTo(null);
    }
}