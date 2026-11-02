// vista/ReporteAsistencia.java
package vista;

import controlador.ControladorReporte;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;

public class ReporteAsistencia extends JFrame {
    private ControladorReporte controlador;
    
    // Componentes
    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JPanel panelFiltros;
    private JPanel panelTabla;
    private JPanel panelBotones;
    
    private JLabel lblTitulo;
    private JLabel lblMes;
    private JLabel lblFechaInicio;
    private JLabel lblFechaFin;
    private JLabel lblTipoPersonal;
    
    private JComboBox<String> cmbMes;
    private JDateChooser dateInicio;
    private JDateChooser dateFin;
    private JComboBox<String> cmbTipoPersonal;
    
    private boolean ajustandoFechas = false;

    private static final String[] MESES = {
        "-- Seleccionar Mes --",
        "Enero", "Febrero", "Marzo", "Abril",
        "Mayo", "Junio", "Julio", "Agosto",
        "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private JButton btnGenerar;
    private JButton btnExportar;
    private JButton btnExportarPdf;
    private JButton btnCerrar;
    private JButton btnPdfIndividual;
    
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
        panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Filtros",
            0,
            0,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        lblMes = new JLabel("Mes:");
        lblMes.setFont(new Font("Segoe UI", Font.BOLD, 13));

        cmbMes = new JComboBox<>(MESES);
        cmbMes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbMes.setPreferredSize(new Dimension(150, 30));

        lblFechaInicio = new JLabel("Fecha Inicio:");
        lblFechaInicio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        dateInicio = new JDateChooser();
        dateInicio.setDateFormatString("dd/MM/yyyy");
        dateInicio.setPreferredSize(new Dimension(135, 30));
        
        lblFechaFin = new JLabel("Fecha Fin:");
        lblFechaFin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        dateFin = new JDateChooser();
        dateFin.setDateFormatString("dd/MM/yyyy");
        dateFin.setPreferredSize(new Dimension(135, 30));
        dateFin.setDate(new Date());
        
        lblTipoPersonal = new JLabel("Tipo:");
        lblTipoPersonal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        cmbTipoPersonal = new JComboBox<>(new String[]{"TODOS", "ADMINISTRATIVO", "DOCENTE"});
        cmbTipoPersonal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbTipoPersonal.setPreferredSize(new Dimension(145, 30));
        
        btnGenerar = new JButton("Generar Reporte");
        btnGenerar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGenerar.setBackground(new Color(46, 204, 113));
        btnGenerar.setForeground(Color.BLACK);
        btnGenerar.setFocusPainted(false);
        btnGenerar.setPreferredSize(new Dimension(150, 35));
        btnGenerar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelFiltros.add(lblMes);
        panelFiltros.add(cmbMes);
        panelFiltros.add(lblFechaInicio);
        panelFiltros.add(dateInicio);
        panelFiltros.add(lblFechaFin);
        panelFiltros.add(dateFin);
        panelFiltros.add(lblTipoPersonal);
        panelFiltros.add(cmbTipoPersonal);
        panelFiltros.add(btnGenerar);
        
        // Panel de tabla
        panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBackground(Color.WHITE);
        panelTabla.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Resultados del Reporte",
            0,
            0,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
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
        tablaReporte.setSelectionBackground(new Color(230, 126, 34));
        tablaReporte.setSelectionForeground(Color.BLACK);
        
        scrollPane = new JScrollPane(tablaReporte);
        panelTabla.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones
        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(Color.WHITE);
        
        btnPdfIndividual = ExportacionIndividual.crearBoton();
        
        btnExportar = new JButton("Exportar a Excel");
        btnExportar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExportar.setBackground(new Color(39, 174, 96));
        btnExportar.setForeground(Color.BLACK);
        btnExportar.setFocusPainted(false);
        btnExportar.setPreferredSize(new Dimension(160, 35));
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
        btnCerrar.setBackground(new Color(149, 165, 166));
        btnCerrar.setForeground(Color.BLACK);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setPreferredSize(new Dimension(100, 35));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelBotones.add(btnPdfIndividual);
        panelBotones.add(btnExportar);
        panelBotones.add(btnExportarPdf);
        panelBotones.add(btnCerrar);

        ExportacionIndividual.instalar(tablaReporte, btnPdfIndividual,
                "Exportar registro de asistencias de esta persona", this::exportarIndividual);
        
        // Agregar paneles al principal
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(Color.WHITE);
        panelNorte.add(panelSuperior, BorderLayout.NORTH);
        panelNorte.add(panelFiltros, BorderLayout.CENTER);
        
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);
        panelPrincipal.add(panelTabla, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar fecha inicial (30 días atrás por defecto)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        dateInicio.setDate(cal.getTime());
        
        // Configurar eventos
        configurarEventos();
    }
    
    private void configurarEventos() {
        cmbMes.addActionListener(e -> onMesSeleccionado());

        dateInicio.getDateEditor().addPropertyChangeListener("date", e -> verificarSiFechasCoincidenConMes());
        dateFin.getDateEditor().addPropertyChangeListener("date", e -> verificarSiFechasCoincidenConMes());

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

    private void onMesSeleccionado() {
        if (ajustandoFechas) return;
        int index = cmbMes.getSelectedIndex();
        if (index <= 0) {
            return;
        }

        int anio;
        if (dateFin.getDate() != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(dateFin.getDate());
            anio = c.get(Calendar.YEAR);
        } else if (dateInicio.getDate() != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(dateInicio.getDate());
            anio = c.get(Calendar.YEAR);
        } else {
            anio = LocalDate.now().getYear();
        }

        int mes = index; // 1 = Enero, ..., 12 = Diciembre
        YearMonth ym = YearMonth.of(anio, mes);
        LocalDate primerDia = ym.atDay(1);
        LocalDate ultimoDia = ym.atEndOfMonth();

        ajustandoFechas = true;
        try {
            dateInicio.setDate(java.sql.Date.valueOf(primerDia));
            dateFin.setDate(java.sql.Date.valueOf(ultimoDia));
        } finally {
            ajustandoFechas = false;
        }
    }

    private void verificarSiFechasCoincidenConMes() {
        if (ajustandoFechas) return;
        Date dIni = dateInicio.getDate();
        Date dFin = dateFin.getDate();
        if (dIni == null || dFin == null) {
            ajustandoFechas = true;
            cmbMes.setSelectedIndex(0);
            ajustandoFechas = false;
            return;
        }

        Calendar cIni = Calendar.getInstance();
        cIni.setTime(dIni);
        Calendar cFin = Calendar.getInstance();
        cFin.setTime(dFin);

        int anioIni = cIni.get(Calendar.YEAR);
        int anioFin = cFin.get(Calendar.YEAR);
        int mesIni = cIni.get(Calendar.MONTH);
        int mesFin = cFin.get(Calendar.MONTH);
        int diaIni = cIni.get(Calendar.DAY_OF_MONTH);
        int diaFin = cFin.get(Calendar.DAY_OF_MONTH);

        if (anioIni == anioFin && mesIni == mesFin && diaIni == 1) {
            YearMonth ym = YearMonth.of(anioIni, mesIni + 1);
            if (diaFin == ym.lengthOfMonth()) {
                ajustandoFechas = true;
                cmbMes.setSelectedIndex(mesIni + 1);
                ajustandoFechas = false;
                return;
            }
        }

        ajustandoFechas = true;
        cmbMes.setSelectedIndex(0);
        ajustandoFechas = false;
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
                "La fecha de inicio no puede ser posterior a la fecha de fin",
                "Rango de fechas inválido",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String tipoPersonal = cmbTipoPersonal.getSelectedItem().toString();
        
        // Generar reporte
        controlador.generarReporteAsistencia(tablaReporte, fechaInicio, fechaFin, tipoPersonal);
        
        boolean hayDatos = tablaReporte.getRowCount() > 0;
        if (!hayDatos) {
            JOptionPane.showMessageDialog(this,
                "No se encontraron registros para los filtros seleccionados",
                "Sin resultados",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private String obtenerMesNombre() {
        if (cmbMes.getSelectedIndex() > 0) {
            return cmbMes.getSelectedItem().toString();
        }
        Date ini = dateInicio.getDate();
        if (ini != null) {
            Calendar cIni = Calendar.getInstance();
            cIni.setTime(ini);
            int mIni = cIni.get(Calendar.MONTH) + 1; // 1..12
            Date fin = dateFin.getDate();
            if (fin != null) {
                Calendar cFin = Calendar.getInstance();
                cFin.setTime(fin);
                int mFin = cFin.get(Calendar.MONTH) + 1;
                if (mIni == mFin && mIni >= 1 && mIni < MESES.length) {
                    return MESES[mIni];
                } else if (mIni >= 1 && mIni < MESES.length && mFin >= 1 && mFin < MESES.length) {
                    return MESES[mIni] + "_" + MESES[mFin];
                }
            }
            if (mIni >= 1 && mIni < MESES.length) {
                return MESES[mIni];
            }
        }
        return "";
    }
    
    private void exportarReporte() {
        if (tablaReporte.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay datos para exportar. Primero haga clic en 'Generar Reporte'.",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String mes = obtenerMesNombre();
        String nombreBase = "Reporte_Asistencias" + (mes.isEmpty() ? "" : "_" + mes);
        util.ExportadorReportes.exportarAExcel(tablaReporte, "Reporte de Asistencias", subtituloReporte(), nombreBase);
    }

    private void exportarIndividual(int fila) {
        util.ExportadorReportes.ReporteIndividual r = controlador.individualAsistencia(
                tablaReporte, fila, "Reporte de Asistencias", util.ExportadorReportes.describirPeriodo(dateInicio.getDate(), dateFin.getDate()));
        String mes = obtenerMesNombre();
        String prefijo = "Reporte_Asistencias" + (mes.isEmpty() ? "" : "_" + mes);
        util.ExportadorReportes.exportarAPDFIndividual(r,
                ExportacionIndividual.nombreArchivo(prefijo, r.persona.get("Nombre")));
    }

    private void exportarReportePDF() {
        if (tablaReporte.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay datos para exportar. Primero haga clic en 'Generar Reporte'.",
                "Tabla vacía",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String mes = obtenerMesNombre();
        String nombreBase = "Reporte_Asistencias" + (mes.isEmpty() ? "" : "_" + mes);
        util.ExportadorReportes.exportarAPDF(tablaReporte, "Reporte de Asistencias", subtituloReporte(), nombreBase);
    }

    private String subtituloReporte() {
        String periodo = util.ExportadorReportes.describirPeriodo(dateInicio.getDate(), dateFin.getDate());
        Object tipo = cmbTipoPersonal.getSelectedItem();
        if (tipo != null && !"TODOS".equals(tipo)) {
            String t = tipo.toString().charAt(0) + tipo.toString().substring(1).toLowerCase();
            return (periodo != null ? periodo + "  -  " : "") + "Personal " + t;
        }
        return periodo != null ? periodo : "";
    }
    
    private void configurarVentana() {
        this.setTitle("Reporte de Asistencias");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelPrincipal);
        this.setSize(1220, 700);
        this.setLocationRelativeTo(null);
    }
}
