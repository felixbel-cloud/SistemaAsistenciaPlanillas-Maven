// vista/ReporteTardanzas.java
package vista;

import controlador.ControladorReporte;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class ReporteTardanzas extends JFrame {
    private ControladorReporte controlador;
    
    // Componentes
    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JPanel panelFiltros;
    private JPanel panelTabla;
    private JPanel panelBotones;private JLabel lblTitulo;
    private JLabel lblFechaInicio;
    private JLabel lblFechaFin;
    
    private JDateChooser dateInicio;
    private JDateChooser dateFin;
    
    private JButton btnGenerar;
    private JButton btnExportar;
    private JButton btnExportarPdf;
    private JButton btnCerrar;
    private JButton btnPdfIndividual;
    private Date ultimoInicio;
    private Date ultimoFin;
    
    private JTable tablaTardanzas;
    private JScrollPane scrollPane;
    private DefaultTableModel modeloTabla;
    
    public ReporteTardanzas() {
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
        
        lblTitulo = new JLabel("REPORTE DE TARDANZAS Y FALTAS");
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
        
        String[] columnas = {"DNI", "Nombre Completo", "Tipo Personal", "Tardanzas", "Faltas", "Minutos Tardanza", "Tiempo Total", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaTardanzas = new JTable(modeloTabla);
        tablaTardanzas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaTardanzas.setRowHeight(25);
        tablaTardanzas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaTardanzas.getTableHeader().setBackground(new Color(230, 126, 34));
        tablaTardanzas.getTableHeader().setForeground(Color.BLACK);
        tablaTardanzas.setSelectionBackground(new Color(230, 126, 34));
        tablaTardanzas.setSelectionForeground(Color.BLACK);
        
        // Resalta en rojo a quien está EN ALERTA (3 o más tardanzas/faltas en el periodo)
        javax.swing.table.DefaultTableCellRenderer renderAlerta = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foco, fila, col);
                boolean alerta = ControladorReporte.ESTADO_ALERTA.equals(t.getValueAt(fila, t.getColumnCount() - 1));
                if (!sel) {
                    c.setBackground(alerta ? new Color(254, 226, 226) : (fila % 2 == 0 ? Color.WHITE : new Color(246, 249, 252)));
                    c.setForeground(alerta ? new Color(153, 27, 27) : Color.BLACK);
                }
                boolean negrita = col == t.getColumnCount() - 1 || (alerta && col == 1);
                c.setFont(t.getFont().deriveFont(negrita ? Font.BOLD : Font.PLAIN));
                setHorizontalAlignment(v instanceof Number || col == t.getColumnCount() - 1 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return c;
            }
        };
        tablaTardanzas.setDefaultRenderer(Object.class, renderAlerta);
        tablaTardanzas.setDefaultRenderer(Integer.class, renderAlerta);

        JLabel lblLeyenda = new JLabel("  En rojo: personal EN ALERTA (" + ControladorReporte.UMBRAL_ALERTA
                + " o más tardanzas y/o faltas en el periodo). Las faltas se detectan automáticamente en bloques ya terminados.");
        lblLeyenda.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblLeyenda.setForeground(new Color(110, 110, 110));
        lblLeyenda.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        panelTabla.add(lblLeyenda, BorderLayout.SOUTH);

        scrollPane = new JScrollPane(tablaTardanzas);
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
        
        btnPdfIndividual = ExportacionIndividual.crearBoton();
        panelBotones.add(btnPdfIndividual);
        panelBotones.add(btnExportar);
        panelBotones.add(btnExportarPdf);
        panelBotones.add(btnCerrar);
        ExportacionIndividual.instalar(tablaTardanzas, btnPdfIndividual,
                "Exportar PDF de esta persona", this::exportarIndividual);
        
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
        
        controlador.generarReporteTardanzas(tablaTardanzas, fechaInicio, fechaFin);
        ultimoInicio = fechaInicio;
        ultimoFin = fechaFin;
        
        if (tablaTardanzas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No se encontraron tardanzas ni faltas en el periodo seleccionado",
                "Sin resultados",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void exportarReporte() {
        if (tablaTardanzas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay datos para exportar",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        util.ExportadorReportes.exportarAExcel(tablaTardanzas, "Reporte de Tardanzas y Faltas", util.ExportadorReportes.describirPeriodo(dateInicio.getDate(), dateFin.getDate()), "Reporte_Tardanzas_Faltas");
    }

    private void exportarIndividual(int fila) {
        if (ultimoInicio == null) {
            return;
        }
        util.ExportadorReportes.ReporteIndividual r = controlador.individualTardanzas(tablaTardanzas, fila,
                ultimoInicio, ultimoFin, util.ExportadorReportes.describirPeriodo(ultimoInicio, ultimoFin));
        util.ExportadorReportes.exportarAPDFIndividual(r,
                ExportacionIndividual.nombreArchivo("Tardanzas_Faltas", r.persona.get("Nombre")));
    }

    private void exportarReportePDF() {
        if (tablaTardanzas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay datos para exportar",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        util.ExportadorReportes.exportarAPDF(tablaTardanzas, "Reporte de Tardanzas y Faltas", util.ExportadorReportes.describirPeriodo(dateInicio.getDate(), dateFin.getDate()), "Reporte_Tardanzas_Faltas");
    }
    
    private void configurarVentana() {
        this.setTitle("Reporte de Tardanzas");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelPrincipal);
        this.setSize(1050, 600);
        this.setLocationRelativeTo(null);
    }
}
