// vista/ConsultaAsistencia.java
package vista;

import controlador.ControladorAsistencia;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class ConsultaAsistencia extends JFrame {
    private ControladorAsistencia controlador;
    
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
    
    private JButton btnBuscar;
    private JButton btnExportar;
    private JButton btnExportarPdf;
    private JButton btnCerrar;
    private JButton btnPdfIndividual;
    
    private JTable tablaAsistencia;
    private JScrollPane scrollPane;
    private DefaultTableModel modeloTabla;
    
    public ConsultaAsistencia() {
        controlador = new ControladorAsistencia();
        inicializarComponentes();
        configurarVentana();
        cargarDatosIniciales();
    }
    
    private void inicializarComponentes() {
        // Panel principal
        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior
        panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(52, 152, 219));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        lblTitulo = new JLabel("CONSULTA DE ASISTENCIAS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.BLACK);
        panelSuperior.add(lblTitulo);
        
        // Panel de filtros
        panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Filtros de Búsqueda",
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
        
        btnBuscar = new JButton("Buscar");
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBuscar.setBackground(new Color(46, 204, 113));
        btnBuscar.setForeground(Color.BLACK);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setPreferredSize(new Dimension(120, 35));
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelFiltros.add(lblFechaInicio);
        panelFiltros.add(dateInicio);
        panelFiltros.add(lblFechaFin);
        panelFiltros.add(dateFin);
        panelFiltros.add(btnBuscar);
        
        // Panel de tabla
        panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBackground(Color.WHITE);
        
        String[] columnas = {"ID", "DNI", "Nombre", "Fecha", "Entrada", "Salida", "Tardanza", "Horas Extras", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaAsistencia = new JTable(modeloTabla);
        tablaAsistencia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaAsistencia.setRowHeight(25);
        tablaAsistencia.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaAsistencia.getTableHeader().setBackground(new Color(52, 152, 219));
        tablaAsistencia.getTableHeader().setForeground(Color.BLACK);
        tablaAsistencia.setSelectionBackground(new Color(52, 152, 219));
        tablaAsistencia.setSelectionForeground(Color.BLACK);
        
        scrollPane = new JScrollPane(tablaAsistencia);
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
        ExportacionIndividual.instalar(tablaAsistencia, btnPdfIndividual,
                "Exportar PDF de esta persona", this::exportarIndividual);
        
        // Agregar paneles
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelFiltros, BorderLayout.NORTH);
        
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(Color.WHITE);
        panelCentral.add(panelFiltros, BorderLayout.NORTH);
        panelCentral.add(panelTabla, BorderLayout.CENTER);
        
        panelPrincipal.add(panelCentral, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
    }
    
    private void configurarEventos() {
        btnBuscar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarAsistencias();
            }
        });
        
        btnExportar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportarAExcel();
            }
        });

        btnExportarPdf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportarAPDF();
            }
        });
        
        btnCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void cargarDatosIniciales() {
        // Establecer fecha de inicio como hace 30 días
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, -30);
        dateInicio.setDate(cal.getTime());
        
        // Cargar asistencias del último mes
        buscarAsistencias();
    }
    
    private void buscarAsistencias() {
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
        
        controlador.cargarAsistencias(tablaAsistencia, fechaInicio, fechaFin);
    }
    
    private void exportarAExcel() {
        if (tablaAsistencia.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay datos para exportar",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        util.ExportadorReportes.exportarAExcel(tablaAsistencia, "Consulta de Asistencias", util.ExportadorReportes.describirPeriodo(dateInicio.getDate(), dateFin.getDate()), "Consulta_Asistencias");
    }

    private void exportarIndividual(int fila) {
        util.ExportadorReportes.ReporteIndividual r = new controlador.ControladorReporte().individualAsistencia(
                tablaAsistencia, fila, "Consulta de Asistencias", util.ExportadorReportes.describirPeriodo(dateInicio.getDate(), dateFin.getDate()));
        util.ExportadorReportes.exportarAPDFIndividual(r,
                ExportacionIndividual.nombreArchivo("Asistencias", r.persona.get("Nombre")));
    }

    private void exportarAPDF() {
        if (tablaAsistencia.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay datos para exportar",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        util.ExportadorReportes.exportarAPDF(tablaAsistencia, "Consulta de Asistencias", util.ExportadorReportes.describirPeriodo(dateInicio.getDate(), dateFin.getDate()), "Consulta_Asistencias");
    }
    
    private void configurarVentana() {
        this.setTitle("Consulta de Asistencias");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelPrincipal);
        this.setSize(1100, 650);
        this.setLocationRelativeTo(null);
    }
}