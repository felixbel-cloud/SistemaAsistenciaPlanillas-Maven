// vista/MenuPrincipal.java
package vista;

import modelo.entidades.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuPrincipal extends JFrame {
    private Usuario usuarioActual;
    
    // Componentes
    private JMenuBar menuBar;
    private JMenu menuAsistencia;
    private JMenu menuPlanilla;
    private JMenu menuReportes;
    private JMenu menuMantenimiento;
    private JMenu menuSistema;
    
    private JMenuItem itemRegistrarAsistencia;
    private JMenuItem itemConsultarAsistencia;
    private JMenuItem itemGenerarPlanilla;
    private JMenuItem itemConsultarPlanilla;
    private JMenuItem itemReporteAsistencia;
    private JMenuItem itemReporteTardanzas;
    private JMenuItem itemReporteHorasExtras;
    private JMenuItem itemGestionPersonal;
    private JMenuItem itemGestionHorarios;
    private JMenuItem itemGestionFeriados;
    private JMenuItem itemGestionUsuarios;
    private JMenuItem itemCerrarSesion;
    private JMenuItem itemSalir;
    
    private JPanel panelPrincipal;
    private JPanel panelBienvenida;
    private JPanel panelAccesos;
    private JLabel lblBienvenida;
    private JLabel lblUsuario;
    private JLabel lblRol;
    private JLabel lblFecha;
    
    public MenuPrincipal(Usuario usuario) {
        this.usuarioActual = usuario;
        inicializarComponentes();
        aplicarPermisosPorRol();
        configurarVentana();
        configurarEventos();
    }

    static boolean puedeAcceder(int idRol, String modulo) {
        switch (modulo) {
            case "REGISTRO_ASISTENCIA":
                return idRol == 1 || idRol == 3;
            case "CONSULTA_ASISTENCIA":
                return idRol == 1 || idRol == 2;
            case "GESTION_PERSONAL":
            case "GESTION_HORARIOS":
            case "GENERAR_PLANILLA":
            case "CONSULTAR_PLANILLA":
            case "GESTION_USUARIOS":
            case "REPORTE_ASISTENCIA":
            case "REPORTE_TARDANZAS":
            case "REPORTE_HORAS_EXTRAS":
                return idRol == 1 || idRol == 2;
            default:
                return false;
        }
    }

    static boolean esMenuVisibleParaRol(int idRol, String menu) {
        switch (menu) {
            case "REGISTRO_ASISTENCIA":
            case "CONSULTA_ASISTENCIA":
                return puedeAcceder(idRol, menu);
            case "GENERAR_PLANILLA":
            case "CONSULTAR_PLANILLA":
                return puedeAcceder(idRol, menu);
            case "REPORTE_ASISTENCIA":
            case "REPORTE_TARDANZAS":
            case "REPORTE_HORAS_EXTRAS":
                return puedeAcceder(idRol, menu);
            case "GESTION_PERSONAL":
            case "GESTION_HORARIOS":
            case "GESTION_USUARIOS":
                return puedeAcceder(idRol, menu);
            case "ASISTENCIA":
                return puedeAcceder(idRol, "REGISTRO_ASISTENCIA") || puedeAcceder(idRol, "CONSULTA_ASISTENCIA");
            case "PLANILLA":
                return puedeAcceder(idRol, "GENERAR_PLANILLA") || puedeAcceder(idRol, "CONSULTAR_PLANILLA");
            case "REPORTES":
                return puedeAcceder(idRol, "REPORTE_ASISTENCIA") || puedeAcceder(idRol, "REPORTE_TARDANZAS") || puedeAcceder(idRol, "REPORTE_HORAS_EXTRAS");
            case "MANTENIMIENTO":
                return puedeAcceder(idRol, "GESTION_PERSONAL") || puedeAcceder(idRol, "GESTION_HORARIOS") || puedeAcceder(idRol, "GESTION_USUARIOS");
            default:
                return false;
        }
    }

    static boolean puedeMostrarTarjeta(int idRol, String textoBoton) {
        switch (textoBoton) {
            case "Registrar Asistencia":
                return puedeAcceder(idRol, "REGISTRO_ASISTENCIA");
            case "Consultar Asistencia":
                return puedeAcceder(idRol, "CONSULTA_ASISTENCIA");
            case "Gestión de Planillas":
            case "Generar Planilla":
            case "Consultar Planilla":
                return puedeAcceder(idRol, "GENERAR_PLANILLA") || puedeAcceder(idRol, "CONSULTAR_PLANILLA");
            case "Gestión Personal":
                return puedeAcceder(idRol, "GESTION_PERSONAL");
            case "Gestión de Horarios":
                return puedeAcceder(idRol, "GESTION_HORARIOS");
            case "Reportes":
            case "Reporte de Tardanzas":
            case "Reporte de Horas Extras":
                return puedeAcceder(idRol, "REPORTE_ASISTENCIA")
                        || puedeAcceder(idRol, "REPORTE_TARDANZAS")
                        || puedeAcceder(idRol, "REPORTE_HORAS_EXTRAS");
            case "Gestión de Feriados":
                return puedeAcceder(idRol, "GESTION_HORARIOS");
            case "Gestión de Usuarios":
                return puedeAcceder(idRol, "GESTION_USUARIOS");
            default:
                return false;
        }
    }

    private void aplicarPermisosPorRol() {
        itemRegistrarAsistencia.setVisible(puedeAcceder(usuarioActual.getIdRol(), "REGISTRO_ASISTENCIA"));
        itemRegistrarAsistencia.setEnabled(puedeAcceder(usuarioActual.getIdRol(), "REGISTRO_ASISTENCIA"));
        itemConsultarAsistencia.setVisible(puedeAcceder(usuarioActual.getIdRol(), "CONSULTA_ASISTENCIA"));
        itemConsultarAsistencia.setEnabled(puedeAcceder(usuarioActual.getIdRol(), "CONSULTA_ASISTENCIA"));
        itemGenerarPlanilla.setVisible(puedeAcceder(usuarioActual.getIdRol(), "GENERAR_PLANILLA"));
        itemGenerarPlanilla.setEnabled(puedeAcceder(usuarioActual.getIdRol(), "GENERAR_PLANILLA"));
        itemConsultarPlanilla.setVisible(puedeAcceder(usuarioActual.getIdRol(), "CONSULTAR_PLANILLA"));
        itemConsultarPlanilla.setEnabled(puedeAcceder(usuarioActual.getIdRol(), "CONSULTAR_PLANILLA"));
        itemReporteAsistencia.setVisible(puedeAcceder(usuarioActual.getIdRol(), "REPORTE_ASISTENCIA"));
        itemReporteAsistencia.setEnabled(puedeAcceder(usuarioActual.getIdRol(), "REPORTE_ASISTENCIA"));
        itemReporteTardanzas.setVisible(puedeAcceder(usuarioActual.getIdRol(), "REPORTE_TARDANZAS"));
        itemReporteTardanzas.setEnabled(puedeAcceder(usuarioActual.getIdRol(), "REPORTE_TARDANZAS"));
        itemReporteHorasExtras.setVisible(puedeAcceder(usuarioActual.getIdRol(), "REPORTE_HORAS_EXTRAS"));
        itemReporteHorasExtras.setEnabled(puedeAcceder(usuarioActual.getIdRol(), "REPORTE_HORAS_EXTRAS"));
        itemGestionPersonal.setVisible(puedeAcceder(usuarioActual.getIdRol(), "GESTION_PERSONAL"));
        itemGestionPersonal.setEnabled(puedeAcceder(usuarioActual.getIdRol(), "GESTION_PERSONAL"));
        itemGestionHorarios.setVisible(puedeAcceder(usuarioActual.getIdRol(), "GESTION_HORARIOS"));
        itemGestionHorarios.setEnabled(puedeAcceder(usuarioActual.getIdRol(), "GESTION_HORARIOS"));
        itemGestionFeriados.setVisible(puedeAcceder(usuarioActual.getIdRol(), "GESTION_HORARIOS"));
        itemGestionFeriados.setEnabled(puedeAcceder(usuarioActual.getIdRol(), "GESTION_HORARIOS"));
        itemGestionUsuarios.setVisible(puedeAcceder(usuarioActual.getIdRol(), "GESTION_USUARIOS"));
        itemGestionUsuarios.setEnabled(puedeAcceder(usuarioActual.getIdRol(), "GESTION_USUARIOS"));

        menuAsistencia.setVisible(esMenuVisibleParaRol(usuarioActual.getIdRol(), "ASISTENCIA"));
        menuPlanilla.setVisible(esMenuVisibleParaRol(usuarioActual.getIdRol(), "PLANILLA"));
        menuReportes.setVisible(esMenuVisibleParaRol(usuarioActual.getIdRol(), "REPORTES"));
        menuMantenimiento.setVisible(esMenuVisibleParaRol(usuarioActual.getIdRol(), "MANTENIMIENTO"));
    }
    
    private void inicializarComponentes() {
        // Panel principal
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(236, 240, 241));
        
        // Panel superior de bienvenida
        panelBienvenida = new JPanel();
        panelBienvenida.setLayout(new BoxLayout(panelBienvenida, BoxLayout.Y_AXIS));
        panelBienvenida.setBackground(new Color(41, 128, 185));
        panelBienvenida.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        lblBienvenida = new JLabel("INSTITUTO AMERICANO");
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblBienvenida.setForeground(Color.BLACK);
        lblBienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        lblUsuario = new JLabel("Usuario: " + usuarioActual.getNombreUsuario());
        lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblUsuario.setForeground(Color.BLACK);
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblRol = new JLabel("Rol: " + obtenerNombreRol(usuarioActual.getIdRol()));
        lblRol.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblRol.setForeground(Color.WHITE);
        lblRol.setOpaque(true);
        lblRol.setBackground(obtenerColorRol(usuarioActual.getIdRol()));
        lblRol.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 2),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        lblFecha = new JLabel("Fecha: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()));
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFecha.setForeground(Color.BLACK);
        lblFecha.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panelBienvenida.add(lblBienvenida);
        panelBienvenida.add(Box.createRigidArea(new Dimension(0, 10)));
        panelBienvenida.add(lblUsuario);
        panelBienvenida.add(Box.createRigidArea(new Dimension(0, 5)));
        panelBienvenida.add(lblRol);
        panelBienvenida.add(Box.createRigidArea(new Dimension(0, 5)));
        panelBienvenida.add(lblFecha);
        
        // Panel de accesos rápidos adaptado a los permisos del rol
        java.util.List<String[]> tarjetasDisponibles = new java.util.ArrayList<>();
        tarjetasDisponibles.add(new String[]{"Registrar Asistencia", "👤"});
        tarjetasDisponibles.add(new String[]{"Consultar Asistencia", "📋"});
        tarjetasDisponibles.add(new String[]{"Gestión de Planillas", "💰"});
        tarjetasDisponibles.add(new String[]{"Gestión Personal", "👥"});
        tarjetasDisponibles.add(new String[]{"Gestión de Horarios", "📅"});
        tarjetasDisponibles.add(new String[]{"Reportes", "📊"});

        int idRol = (usuarioActual != null) ? usuarioActual.getIdRol() : 1;
        java.util.List<String[]> tarjetasRol = new java.util.ArrayList<>();
        for (String[] t : tarjetasDisponibles) {
            if (puedeMostrarTarjeta(idRol, t[0])) {
                tarjetasRol.add(t);
            }
        }

        if (tarjetasRol.size() == 1) {
            panelAccesos = new JPanel(new BorderLayout());
            panelAccesos.setBackground(new Color(236, 240, 241));
            panelAccesos.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

            String[] t = tarjetasRol.get(0);
            JButton boton = crearBotonAccesoPrincipal(t[0], t[1], obtenerColorPorModulo(t[0]));
            panelAccesos.add(boton, BorderLayout.CENTER);
        } else if (tarjetasRol.size() == 5) {
            // Distribución equilibrada para 5 tarjetas: 3 arriba y 2 abajo,
            // llenando todo el ancho sin dejar ningún hueco vacío.
            panelAccesos = new JPanel(new GridLayout(2, 1, 15, 15));
            panelAccesos.setBackground(new Color(236, 240, 241));
            panelAccesos.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

            JPanel fila1 = new JPanel(new GridLayout(1, 3, 15, 15));
            fila1.setBackground(new Color(236, 240, 241));
            for (int i = 0; i < 3; i++) {
                String[] t = tarjetasRol.get(i);
                agregarBotonAcceso(fila1, t[0], t[1], obtenerColorPorModulo(t[0]));
            }

            JPanel fila2 = new JPanel(new GridLayout(1, 2, 15, 15));
            fila2.setBackground(new Color(236, 240, 241));
            for (int i = 3; i < 5; i++) {
                String[] t = tarjetasRol.get(i);
                agregarBotonAcceso(fila2, t[0], t[1], obtenerColorPorModulo(t[0]));
            }

            panelAccesos.add(fila1);
            panelAccesos.add(fila2);
        } else {
            int cols = (tarjetasRol.size() <= 4) ? 2 : 3;
            int filas = (int) Math.ceil((double) tarjetasRol.size() / cols);
            panelAccesos = new JPanel(new GridLayout(filas, cols, 15, 15));
            panelAccesos.setBackground(new Color(236, 240, 241));
            panelAccesos.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

            for (String[] t : tarjetasRol) {
                agregarBotonAcceso(panelAccesos, t[0], t[1], obtenerColorPorModulo(t[0]));
            }
        }
        
        panelPrincipal.add(panelBienvenida, BorderLayout.NORTH);
        panelPrincipal.add(panelAccesos, BorderLayout.CENTER);
        
        // Crear barra de menú
        crearMenuBar();
    }
    
    private Color obtenerColorPorModulo(String texto) {
        switch (texto) {
            case "Registrar Asistencia":
                return new Color(52, 152, 219);
            case "Consultar Asistencia":
                return new Color(22, 160, 133);
            case "Gestión de Planillas":
            case "Generar Planilla":
            case "Consultar Planilla":
                return new Color(155, 89, 182);
            case "Reportes":
                return new Color(230, 126, 34);
            case "Reporte de Tardanzas":
                return new Color(192, 57, 43);
            case "Reporte de Horas Extras":
                return new Color(39, 174, 96);
            case "Gestión Personal":
                return new Color(241, 196, 15);
            case "Gestión de Horarios":
                return new Color(142, 68, 173);
            case "Gestión de Feriados":
                return new Color(26, 188, 156);
            case "Gestión de Usuarios":
                return new Color(52, 73, 94);
            default:
                return new Color(189, 195, 199);
        }
    }

    private void aplicarEstiloDeBoton(JButton boton, String texto, Color color) {
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setBackground(color);
        boton.setForeground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if ("Gestión de Horarios".equals(texto)) {
            boton.setPreferredSize(new Dimension(200, 180));
        } else {
            boton.setPreferredSize(new Dimension(200, 120));
        }
    }

    private JButton crearBotonAccesoPrincipal(String texto, String icono, Color color) {
        JButton boton = new JButton("<html><center><span style='font-size:42px;'>" + icono + "</span><br><br><span style='font-size:24px; font-weight:bold;'>" + texto + "</span><br><br><span style='font-size:14px; font-weight:normal; color:#1a252f;'>Haga clic aquí para registrar entradas y salidas</span></center></html>");
        boton.setFont(new Font("Segoe UI", Font.BOLD, 22));
        boton.setBackground(color);
        boton.setForeground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirVentanaSegunBoton(texto);
            }
        });
        return boton;
    }

    private JButton crearBotonAcceso(String texto, String icono, Color color) {
        JButton boton = new JButton("<html><center><font face='Segoe UI Emoji' size='+1'>" + icono + "</font><br>" + texto + "</center></html>");
        aplicarEstiloDeBoton(boton, texto, color);
        boton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirVentanaSegunBoton(texto);
            }
        });
        return boton;
    }

    private void agregarBotonAcceso(JPanel panel, String texto, String icono, Color color) {
        panel.add(crearBotonAcceso(texto, icono, color));
    }

    private void abrirVentanaSegunBoton(String texto) {
        switch (texto) {
            case "Registrar Asistencia":
                if (!puedeAcceder(usuarioActual.getIdRol(), "REGISTRO_ASISTENCIA")) {
                    mostrarSinPermiso();
                    return;
                }
                abrirRegistroAsistencia();
                break;
            case "Consultar Asistencia":
                if (!puedeAcceder(usuarioActual.getIdRol(), "CONSULTA_ASISTENCIA")) {
                    mostrarSinPermiso();
                    return;
                }
                abrirConsultaAsistencia();
                break;
            case "Gestión de Planillas":
            case "Generar Planilla":
            case "Consultar Planilla":
                if (!puedeAcceder(usuarioActual.getIdRol(), "GENERAR_PLANILLA")
                        && !puedeAcceder(usuarioActual.getIdRol(), "CONSULTAR_PLANILLA")) {
                    mostrarSinPermiso();
                    return;
                }
                abrirPlanillas();
                break;
            case "Reportes":
                if (!puedeAcceder(usuarioActual.getIdRol(), "REPORTE_ASISTENCIA")) {
                    mostrarSinPermiso();
                    return;
                }
                abrirReportes();
                break;
            case "Reporte de Tardanzas":
                if (!puedeAcceder(usuarioActual.getIdRol(), "REPORTE_TARDANZAS")) {
                    mostrarSinPermiso();
                    return;
                }
                abrirReporteTardanzas();
                break;
            case "Reporte de Horas Extras":
                if (!puedeAcceder(usuarioActual.getIdRol(), "REPORTE_HORAS_EXTRAS")) {
                    mostrarSinPermiso();
                    return;
                }
                abrirReporteHorasExtras();
                break;
            case "Gestión Personal":
                if (!puedeAcceder(usuarioActual.getIdRol(), "GESTION_PERSONAL")) {
                    mostrarSinPermiso();
                    return;
                }
                abrirGestionPersonal();
                break;
            case "Gestión de Horarios":
                if (!puedeAcceder(usuarioActual.getIdRol(), "GESTION_HORARIOS")) {
                    mostrarSinPermiso();
                    return;
                }
                abrirGestionHorarios();
                break;
            case "Gestión de Feriados":
                if (!puedeAcceder(usuarioActual.getIdRol(), "GESTION_HORARIOS")) {
                    mostrarSinPermiso();
                    return;
                }
                new GestionFeriados().setVisible(true);
                break;
            case "Gestión de Usuarios":
                if (!puedeAcceder(usuarioActual.getIdRol(), "GESTION_USUARIOS")) {
                    mostrarSinPermiso();
                    return;
                }
                abrirGestionUsuarios();
                break;
        }
    }

    private void mostrarSinPermiso() {
        JOptionPane.showMessageDialog(this,
                "No tiene permisos para acceder a este módulo.",
                "Acceso denegado",
                JOptionPane.WARNING_MESSAGE);
    }
    
    private void crearMenuBar() {
        menuBar = new JMenuBar();
        
        // Menú Asistencia
        menuAsistencia = new JMenu("Asistencia");
        itemRegistrarAsistencia = new JMenuItem("Registrar Asistencia");
        itemConsultarAsistencia = new JMenuItem("Consultar Asistencia");
        menuAsistencia.add(itemRegistrarAsistencia);
        menuAsistencia.add(itemConsultarAsistencia);
        
        // Menú Planilla
        menuPlanilla = new JMenu("Planilla");
        itemGenerarPlanilla = new JMenuItem("Generar Planilla");
        itemConsultarPlanilla = new JMenuItem("Consultar Planilla");
        menuPlanilla.add(itemGenerarPlanilla);
        menuPlanilla.add(itemConsultarPlanilla);
        
        // Menú Reportes
        menuReportes = new JMenu("Reportes");
        itemReporteAsistencia = new JMenuItem("Reporte de Asistencia");
        itemReporteTardanzas = new JMenuItem("Reporte de Tardanzas");
        itemReporteHorasExtras = new JMenuItem("Reporte de Horas Extras");
        menuReportes.add(itemReporteAsistencia);
        menuReportes.add(itemReporteTardanzas);
        menuReportes.add(itemReporteHorasExtras);
        
        // Menú Mantenimiento
        menuMantenimiento = new JMenu("Mantenimiento");
        itemGestionPersonal = new JMenuItem("Gestión de Personal");
        itemGestionHorarios = new JMenuItem("Gestión de Horarios");
        itemGestionUsuarios = new JMenuItem("Gestión de Usuarios");
        menuMantenimiento.add(itemGestionPersonal);
        menuMantenimiento.add(itemGestionHorarios);
        itemGestionFeriados = new JMenuItem("Gestión de Feriados");
        menuMantenimiento.add(itemGestionFeriados);
        menuMantenimiento.add(itemGestionUsuarios);
        
        // Menú Sistema
        menuSistema = new JMenu("Sistema");
        itemCerrarSesion = new JMenuItem("Cerrar Sesión");
        itemSalir = new JMenuItem("Salir");
        menuSistema.add(itemCerrarSesion);
        menuSistema.addSeparator();
        menuSistema.add(itemSalir);
        
        menuBar.add(menuAsistencia);
        menuBar.add(menuPlanilla);
        menuBar.add(menuReportes);
        menuBar.add(menuMantenimiento);
        menuBar.add(menuSistema);
        
        this.setJMenuBar(menuBar);
    }
    
    private void configurarEventos() {
        // Eventos del menú Asistencia
        itemRegistrarAsistencia.addActionListener(e -> abrirRegistroAsistencia());
        itemConsultarAsistencia.addActionListener(e -> abrirConsultaAsistencia());
        
        // Eventos del menú Planilla
        itemGenerarPlanilla.addActionListener(e -> abrirGenerarPlanilla());
        itemConsultarPlanilla.addActionListener(e -> abrirConsultaPlanilla());
        
        // Eventos del menú Reportes
        itemReporteAsistencia.addActionListener(e -> abrirReportes());
        itemReporteTardanzas.addActionListener(e -> abrirReporteTardanzas());
        itemReporteHorasExtras.addActionListener(e -> abrirReporteHorasExtras());
        
        // Eventos del menú Mantenimiento
        itemGestionPersonal.addActionListener(e -> abrirGestionPersonal());
        itemGestionHorarios.addActionListener(e -> abrirGestionHorarios());
        itemGestionFeriados.addActionListener(e -> new GestionFeriados().setVisible(true));
        itemGestionUsuarios.addActionListener(e -> abrirGestionUsuarios());
        
        // Eventos del menú Sistema
        itemCerrarSesion.addActionListener(e -> cerrarSesion());
        itemSalir.addActionListener(e -> salirAplicacion());
    }
    
    private void abrirRegistroAsistencia() {
        RegistroAsistencia ventana = new RegistroAsistencia(usuarioActual);
        ventana.setVisible(true);
    }
    
    private void abrirConsultaAsistencia() {
        ConsultaAsistencia ventana = new ConsultaAsistencia();
        ventana.setVisible(true);
    }
    
    private void abrirGenerarPlanilla() {
        abrirPlanillas();
    }
    
    private void abrirConsultaPlanilla() {
        abrirPlanillas();
    }

    private void abrirPlanillas() {
        ConsultaPlanilla ventana = new ConsultaPlanilla(usuarioActual);
        ventana.setVisible(true);
    }
    
    private void abrirReportes() {
        ReporteAsistencia ventana = new ReporteAsistencia();
        ventana.setVisible(true);
    }
    
    private void abrirReporteTardanzas() {
        ReporteTardanzas ventana = new ReporteTardanzas();
        ventana.setVisible(true);
    }
    
    private void abrirReporteHorasExtras() {
        ReporteHorasExtras ventana = new ReporteHorasExtras();
        ventana.setVisible(true);
    }
    
    private void abrirGestionPersonal() {
        GestionPersonal ventana = new GestionPersonal();
        ventana.setVisible(true);
    }

    private void abrirGestionHorarios() {
        GestionHorarios ventana = new GestionHorarios();
        ventana.setVisible(true);
    }
    
    private void abrirGestionUsuarios() {
        JOptionPane.showMessageDialog(this, 
            "Módulo de Gestión de Usuarios en desarrollo", 
            "Información", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de cerrar sesión?",
            "Confirmar cierre de sesión",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            Login login = new Login();
            login.setVisible(true);
            this.dispose();
        }
    }
    
    private void salirAplicacion() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de salir de la aplicación?",
            "Confirmar salida",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    private String obtenerNombreRol(int idRol) {
        switch (idRol) {
            case 1:
                return "ADMINISTRADOR";
            case 2:
                return "RECURSOS_HUMANOS";
            case 3:
                return "OPERADOR";
            default:
                return "SIN_ROL";
        }
    }

    private Color obtenerColorRol(int idRol) {
        switch (idRol) {
            case 1:
                return new Color(185, 0, 0);
            case 2:
                return new Color(0, 98, 181);
            case 3:
                return new Color(0, 128, 64);
            default:
                return Color.BLACK;
        }
    }

    private void configurarVentana() {
        this.setTitle("Sistema de Asistencias y Planillas - Instituto Americano");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelPrincipal);
        this.setSize(1000, 700);
        this.setLocationRelativeTo(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
}
