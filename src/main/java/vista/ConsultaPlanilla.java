// vista/ConsultaPlanilla.java
package vista;

import controlador.ControladorPlanilla;
import modelo.dao.PlanillaDAO;
import modelo.entidades.Planilla;
import modelo.entidades.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

public class ConsultaPlanilla extends JFrame {
    private ControladorPlanilla controlador;
    private PlanillaDAO planillaDAO;
    private Usuario usuarioActual;
    
    // Componentes
    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JPanel panelGeneracion;
    private JPanel panelTablas;
    private JPanel panelBotones;
    
    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    
    private JComboBox<String> cmbMes;
    private JComboBox<String> cmbAnio;
    private JLabel lblEstadoPeriodo;
    private JButton btnGenerar;
    private JButton btnEliminarPlanilla;
    
    private JTable tablaPlanillas;
    private JTable tablaDetalle;
    private JScrollPane scrollPlanillas;
    private JScrollPane scrollDetalle;
    private DefaultTableModel modeloPlanillas;
    private DefaultTableModel modeloDetalle;
    
    private JPanel panelTablaDetalle;
    
    private JButton btnVerDetalle;
    private JButton btnExportarDetalle;
    private JButton btnExportarDetallePdf;
    private JButton btnCambiarEstado;
    private JButton btnActualizar;
    private JButton btnCerrar;
    private JButton btnBoletaIndividual;
    
    private int idPlanillaSeleccionada = -1;
    private boolean sincronizandoSeleccion = false;

    private static final String[] MESES = {
        "01 - Enero", "02 - Febrero", "03 - Marzo", "04 - Abril",
        "05 - Mayo", "06 - Junio", "07 - Julio", "08 - Agosto",
        "09 - Septiembre", "10 - Octubre", "11 - Noviembre", "12 - Diciembre"
    };

    public ConsultaPlanilla() {
        this(null, null);
    }

    public ConsultaPlanilla(Usuario usuario) {
        this(usuario, null);
    }

    public ConsultaPlanilla(String periodoASeleccionar) {
        this(null, periodoASeleccionar);
    }

    public ConsultaPlanilla(Usuario usuario, String periodoASeleccionar) {
        this.usuarioActual = usuario;
        this.controlador = new ControladorPlanilla();
        this.planillaDAO = new PlanillaDAO();
        inicializarComponentes();
        configurarVentana();
        cargarPlanillas(periodoASeleccionar);
    }
    
    private void inicializarComponentes() {
        // Panel principal
        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior (Encabezado)
        panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setBackground(new Color(155, 89, 182));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        lblTitulo = new JLabel("GESTIÓN DE PLANILLAS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.BLACK);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSubtitulo = new JLabel("Generación de nóminas, consulta y emisión de boletas de pago");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(Color.BLACK);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panelSuperior.add(lblTitulo);
        panelSuperior.add(Box.createRigidArea(new Dimension(0, 5)));
        panelSuperior.add(lblSubtitulo);

        // Panel de Generación / Selección de Periodo (unificado)
        panelGeneracion = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        panelGeneracion.setBackground(Color.WHITE);
        panelGeneracion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 1),
                "Periodo a Gestionar",
                0,
                0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(142, 68, 173)
            ),
            BorderFactory.createEmptyBorder(2, 10, 5, 10)
        ));

        JLabel lblMes = new JLabel("Mes:");
        lblMes.setFont(new Font("Segoe UI", Font.BOLD, 13));

        cmbMes = new JComboBox<>(MESES);
        cmbMes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbMes.setPreferredSize(new Dimension(145, 30));

        JLabel lblAnio = new JLabel("Año:");
        lblAnio.setFont(new Font("Segoe UI", Font.BOLD, 13));

        cmbAnio = new JComboBox<>(new String[]{"2024", "2025", "2026", "2027", "2028"});
        cmbAnio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbAnio.setPreferredSize(new Dimension(85, 30));

        LocalDate hoy = LocalDate.now();
        cmbMes.setSelectedIndex(Math.max(0, Math.min(11, hoy.getMonthValue() - 1)));
        cmbAnio.setSelectedItem(String.valueOf(hoy.getYear()));

        btnGenerar = new JButton("Generar Planilla");
        btnGenerar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGenerar.setBackground(new Color(39, 174, 96));
        btnGenerar.setForeground(Color.BLACK);
        btnGenerar.setFocusPainted(false);
        btnGenerar.setPreferredSize(new Dimension(170, 32));
        btnGenerar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnEliminarPlanilla = new JButton("Eliminar Planilla");
        btnEliminarPlanilla.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEliminarPlanilla.setBackground(new Color(231, 76, 60));
        btnEliminarPlanilla.setForeground(Color.BLACK);
        btnEliminarPlanilla.setFocusPainted(false);
        btnEliminarPlanilla.setPreferredSize(new Dimension(145, 32));
        btnEliminarPlanilla.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminarPlanilla.setEnabled(false);

        lblEstadoPeriodo = new JLabel("Seleccione un periodo");
        lblEstadoPeriodo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstadoPeriodo.setForeground(new Color(52, 73, 94));

        panelGeneracion.add(lblMes);
        panelGeneracion.add(cmbMes);
        panelGeneracion.add(lblAnio);
        panelGeneracion.add(cmbAnio);
        panelGeneracion.add(btnGenerar);
        panelGeneracion.add(btnEliminarPlanilla);
        panelGeneracion.add(Box.createRigidArea(new Dimension(10, 0)));
        panelGeneracion.add(lblEstadoPeriodo);

        // Contenedor Norte (Encabezado + Barra de Periodo)
        JPanel panelNorte = new JPanel(new BorderLayout(5, 5));
        panelNorte.setBackground(Color.WHITE);
        panelNorte.add(panelSuperior, BorderLayout.NORTH);
        panelNorte.add(panelGeneracion, BorderLayout.SOUTH);

        // Panel de tablas (dividido en 2 secciones)
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
        panelTablaDetalle = new JPanel(new BorderLayout());
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
        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        panelBotones.setBackground(Color.WHITE);
        
        btnVerDetalle = new JButton("Ver Detalle");
        btnVerDetalle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnVerDetalle.setBackground(new Color(52, 152, 219));
        btnVerDetalle.setForeground(Color.BLACK);
        btnVerDetalle.setFocusPainted(false);
        btnVerDetalle.setPreferredSize(new Dimension(130, 35));
        btnVerDetalle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnExportarDetalle = new JButton("Exportar Detalle");
        btnExportarDetalle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExportarDetalle.setBackground(new Color(39, 174, 96));
        btnExportarDetalle.setForeground(Color.BLACK);
        btnExportarDetalle.setFocusPainted(false);
        btnExportarDetalle.setPreferredSize(new Dimension(150, 35));
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
        btnCambiarEstado.setPreferredSize(new Dimension(150, 35));
        btnCambiarEstado.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCambiarEstado.setEnabled(false);
        
        btnActualizar = new JButton("Actualizar");
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnActualizar.setBackground(new Color(149, 165, 166));
        btnActualizar.setForeground(Color.BLACK);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setPreferredSize(new Dimension(110, 35));
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrar.setBackground(new Color(231, 76, 60));
        btnCerrar.setForeground(Color.BLACK);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setPreferredSize(new Dimension(95, 35));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnBoletaIndividual = ExportacionIndividual.crearBoton();
        btnBoletaIndividual.setText("Boleta individual");
        btnBoletaIndividual.setPreferredSize(new Dimension(150, 35));
        panelBotones.add(btnVerDetalle);
        panelBotones.add(btnBoletaIndividual);
        panelBotones.add(btnExportarDetalle);
        panelBotones.add(btnExportarDetallePdf);
        ExportacionIndividual.instalar(tablaDetalle, btnBoletaIndividual,
                "Exportar boleta de pago de esta persona", this::exportarBoleta);
        panelBotones.add(btnCambiarEstado);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnCerrar);
        
        // Agregar paneles principales
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);
        panelPrincipal.add(panelTablas, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
    }
    
    private void configurarEventos() {
        // Evento de selección en combos de periodo
        ActionListener listenerCombos = e -> onPeriodoCombosCambiados();
        cmbMes.addActionListener(listenerCombos);
        cmbAnio.addActionListener(listenerCombos);

        // Botón generar / regenerar planilla
        btnGenerar.addActionListener(e -> accionarGenerarPlanilla());

        // Botón eliminar planilla
        btnEliminarPlanilla.addActionListener(e -> eliminarPlanillaSeleccionada());

        // Evento de selección en tabla de planillas: carga automática del detalle
        tablaPlanillas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !sincronizandoSeleccion) {
                int selectedRow = tablaPlanillas.getSelectedRow();
                if (selectedRow >= 0) {
                    mostrarDetalleDeFila(selectedRow);
                    sincronizarCombosConFila(selectedRow);
                } else {
                    limpiarDetalle();
                }
            }
        });

        // Clic directo en tabla de planillas
        tablaPlanillas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int selectedRow = tablaPlanillas.getSelectedRow();
                if (selectedRow >= 0) {
                    mostrarDetalleDeFila(selectedRow);
                    sincronizarCombosConFila(selectedRow);
                }
            }
        });
        
        btnVerDetalle.addActionListener(e -> verDetallePlanilla());
        btnExportarDetalle.addActionListener(e -> exportarDetalle());
        btnExportarDetallePdf.addActionListener(e -> exportarDetallePDF());
        btnCambiarEstado.addActionListener(e -> cambiarEstadoPlanilla());
        btnActualizar.addActionListener(e -> cargarPlanillas(obtenerPeriodoSeleccionado()));
        btnCerrar.addActionListener(e -> dispose());
    }

    public String obtenerPeriodoSeleccionado() {
        if (cmbMes.getSelectedItem() == null || cmbAnio.getSelectedItem() == null) {
            return LocalDate.now().toString().substring(0, 7);
        }
        String mes = ((String) cmbMes.getSelectedItem()).substring(0, 2);
        String anio = (String) cmbAnio.getSelectedItem();
        return anio + "-" + mes;
    }

    private void onPeriodoCombosCambiados() {
        if (sincronizandoSeleccion) return;
        String periodo = obtenerPeriodoSeleccionado();
        actualizarEstadoPeriodo(periodo);

        int fila = buscarFilaPorPeriodo(periodo);
        if (fila >= 0) {
            sincronizandoSeleccion = true;
            tablaPlanillas.setRowSelectionInterval(fila, fila);
            tablaPlanillas.scrollRectToVisible(tablaPlanillas.getCellRect(fila, 0, true));
            mostrarDetalleDeFila(fila);
            sincronizandoSeleccion = false;
        } else {
            tablaPlanillas.clearSelection();
            limpiarDetalle();
        }
    }

    private void sincronizarCombosConFila(int selectedRow) {
        if (selectedRow < 0 || selectedRow >= tablaPlanillas.getRowCount()) return;
        String periodo = String.valueOf(modeloPlanillas.getValueAt(selectedRow, 1));
        sincronizarCombosConPeriodo(periodo);
        actualizarEstadoPeriodo(periodo);
    }

    private void sincronizarCombosConPeriodo(String periodo) {
        if (periodo == null || !periodo.matches("\\d{4}-\\d{2}")) return;
        String anio = periodo.substring(0, 4);
        String mesStr = periodo.substring(5, 7);
        int mesInt = Integer.parseInt(mesStr);

        sincronizandoSeleccion = true;
        cmbAnio.setSelectedItem(anio);
        if (mesInt >= 1 && mesInt <= 12) {
            cmbMes.setSelectedIndex(mesInt - 1);
        }
        sincronizandoSeleccion = false;
    }

    private int buscarFilaPorPeriodo(String periodo) {
        if (periodo == null) return -1;
        for (int i = 0; i < modeloPlanillas.getRowCount(); i++) {
            Object val = modeloPlanillas.getValueAt(i, 1);
            if (val != null && periodo.equals(val.toString())) {
                return i;
            }
        }
        return -1;
    }

    private void actualizarEstadoPeriodo(String periodo) {
        Planilla p = planillaDAO.obtenerPlanillaPorPeriodo(periodo);
        if (p != null) {
            lblEstadoPeriodo.setText("✓ " + periodo + ": " + p.getEstado() + " (Total: S/ " + String.format(java.util.Locale.US, "%.2f", p.getTotalPlanilla()) + ")");
            lblEstadoPeriodo.setForeground(new Color(39, 174, 96));
            btnGenerar.setText("Regenerar Planilla");
            btnGenerar.setBackground(new Color(142, 68, 173));
            btnEliminarPlanilla.setEnabled(!"PAGADA".equalsIgnoreCase(p.getEstado()));
        } else {
            lblEstadoPeriodo.setText("○ " + periodo + ": Pendiente de generación");
            lblEstadoPeriodo.setForeground(new Color(230, 126, 34));
            btnGenerar.setText("Generar Planilla");
            btnGenerar.setBackground(new Color(39, 174, 96));
            btnEliminarPlanilla.setEnabled(false);
        }
    }

    private void accionarGenerarPlanilla() {
        String periodo = obtenerPeriodoSeleccionado();
        if (!controlador.validarPeriodo(periodo)) {
            JOptionPane.showMessageDialog(this,
                "El periodo " + periodo + " no es válido.\n"
                + "Use el formato AAAA-MM, desde 2020-01 hasta el mes actual.",
                "Periodo no válido",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Planilla existente = planillaDAO.obtenerPlanillaPorPeriodo(periodo);
        int idUsuario = usuarioActual != null ? usuarioActual.getIdUsuario() : 1;

        if (existente != null) {
            if ("PAGADA".equalsIgnoreCase(existente.getEstado())) {
                JOptionPane.showMessageDialog(this,
                    "La planilla del periodo " + periodo + " ya está PAGADA y no se puede modificar.",
                    "Planilla Pagada",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            int opcion = JOptionPane.showConfirmDialog(this,
                "Ya existe una planilla generada para el periodo " + periodo + ".\n\n"
                + "¿Desea regenerarla con las asistencias y reglas vigentes?",
                "Regenerar Planilla",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (opcion != JOptionPane.YES_OPTION) {
                return;
            }

            boolean ok = controlador.regenerarPlanilla(periodo, idUsuario);
            if (ok) {
                cargarPlanillas(periodo);
            }
        } else {
            boolean ok = controlador.generarPlanilla(periodo, idUsuario);
            if (ok) {
                cargarPlanillas(periodo);
            }
        }
    }

    private void eliminarPlanillaSeleccionada() {
        int selectedRow = tablaPlanillas.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione la planilla que desea eliminar en la lista superior.",
                "Sin selección",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPlanilla = (int) modeloPlanillas.getValueAt(selectedRow, 0);
        String periodo = String.valueOf(modeloPlanillas.getValueAt(selectedRow, 1));
        String estado = String.valueOf(modeloPlanillas.getValueAt(selectedRow, 4));

        if ("PAGADA".equalsIgnoreCase(estado)) {
            JOptionPane.showMessageDialog(this,
                "No se puede eliminar la planilla del periodo " + periodo + " porque ya está en estado PAGADA.",
                "Acción no permitida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar la planilla del periodo " + periodo + "?\n\n"
            + "Se eliminará todo el detalle de cálculo de este periodo para que pueda volver a generarse desde cero.",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = planillaDAO.eliminarPlanilla(idPlanilla);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                    "La planilla del periodo " + periodo + " fue eliminada correctamente.",
                    "Planilla eliminada",
                    JOptionPane.INFORMATION_MESSAGE);
                cargarPlanillas(null);
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar la planilla.",
                    "Error al eliminar",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void cargarPlanillas() {
        cargarPlanillas(null);
    }

    public void cargarPlanillas(String periodoASeleccionar) {
        controlador.cargarPlanillas(tablaPlanillas);
        if (periodoASeleccionar == null) {
            periodoASeleccionar = obtenerPeriodoSeleccionado();
        }
        seleccionarPeriodo(periodoASeleccionar);
    }

    public void seleccionarPeriodo(String periodoASeleccionar) {
        if (tablaPlanillas.getRowCount() == 0) {
            limpiarDetalle();
            actualizarEstadoPeriodo(obtenerPeriodoSeleccionado());
            return;
        }

        int filaASeleccionar = -1;
        if (periodoASeleccionar != null && !periodoASeleccionar.trim().isEmpty()) {
            filaASeleccionar = buscarFilaPorPeriodo(periodoASeleccionar);
        }

        if (filaASeleccionar >= 0) {
            int viewIndex = tablaPlanillas.convertRowIndexToView(filaASeleccionar);
            tablaPlanillas.setRowSelectionInterval(viewIndex, viewIndex);
            tablaPlanillas.scrollRectToVisible(tablaPlanillas.getCellRect(viewIndex, 0, true));
            mostrarDetalleDeFila(viewIndex);
            sincronizarCombosConPeriodo(periodoASeleccionar);
        } else {
            // Seleccionar la primera fila por defecto
            tablaPlanillas.setRowSelectionInterval(0, 0);
            mostrarDetalleDeFila(0);
            sincronizarCombosConFila(0);
        }
        actualizarEstadoPeriodo(obtenerPeriodoSeleccionado());
    }

    private void mostrarDetalleDeFila(int selectedRow) {
        if (selectedRow < 0 || selectedRow >= tablaPlanillas.getRowCount()) {
            limpiarDetalle();
            return;
        }

        int modelRow = tablaPlanillas.convertRowIndexToModel(selectedRow);
        idPlanillaSeleccionada = (int) modeloPlanillas.getValueAt(modelRow, 0);
        String periodo = String.valueOf(modeloPlanillas.getValueAt(modelRow, 1));
        String total = String.valueOf(modeloPlanillas.getValueAt(modelRow, 3));
        String estado = String.valueOf(modeloPlanillas.getValueAt(modelRow, 4));

        controlador.cargarDetallePlanilla(tablaDetalle, idPlanillaSeleccionada);
        btnVerDetalle.setEnabled(true);
        btnCambiarEstado.setEnabled(true);
        btnEliminarPlanilla.setEnabled(!"PAGADA".equalsIgnoreCase(estado));
        btnExportarDetalle.setEnabled(tablaDetalle.getRowCount() > 0);
        btnExportarDetallePdf.setEnabled(tablaDetalle.getRowCount() > 0);

        if (panelTablaDetalle != null) {
            panelTablaDetalle.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Detalle de Planilla Seleccionada - Periodo: " + periodo + " (" + estado + " - Total: " + total + " - " + tablaDetalle.getRowCount() + " colaboradores)",
                0,
                0,
                new Font("Segoe UI", Font.BOLD, 14)
            ));
            panelTablaDetalle.repaint();
        }
    }
    
    private void verDetallePlanilla() {
        int selectedRow = tablaPlanillas.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una planilla en la lista superior",
                "Selección requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        mostrarDetalleDeFila(selectedRow);
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
        
        util.ExportadorReportes.exportarAExcel(tablaDetalle, "Detalle de Planilla", "Periodo: " + periodo, "Detalle_Planilla_" + periodo);
    }

    private void exportarBoleta(int fila) {
        int selectedRow = tablaPlanillas.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione primero la planilla en la lista superior",
                "Sin planilla",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String periodo = (String) modeloPlanillas.getValueAt(selectedRow, 1);
        util.ExportadorReportes.ReporteIndividual r = new controlador.ControladorReporte()
                .boletaPlanilla(tablaDetalle, fila, periodo);
        util.ExportadorReportes.exportarAPDFIndividual(r,
                ExportacionIndividual.nombreArchivo("Boleta_" + periodo, r.persona.get("Nombre")));
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

        util.ExportadorReportes.exportarAPDF(tablaDetalle, "Detalle de Planilla", "Periodo: " + periodo,
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
                    cargarPlanillas(obtenerPeriodoSeleccionado());
                }
            }
        }
    }
    
    private void limpiarDetalle() {
        modeloDetalle.setRowCount(0);
        btnExportarDetalle.setEnabled(false);
        btnExportarDetallePdf.setEnabled(false);
        btnVerDetalle.setEnabled(false);
        btnCambiarEstado.setEnabled(false);
        btnEliminarPlanilla.setEnabled(false);
        idPlanillaSeleccionada = -1;
        if (panelTablaDetalle != null) {
            panelTablaDetalle.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Detalle de Planilla Seleccionada",
                0,
                0,
                new Font("Segoe UI", Font.BOLD, 14)
            ));
            panelTablaDetalle.repaint();
        }
    }
    
    private void configurarVentana() {
        this.setTitle("Gestión de Planillas - Generación y Consulta");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelPrincipal);
        this.setSize(1220, 720);
        this.setLocationRelativeTo(null);
    }
}
