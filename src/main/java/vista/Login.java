// vista/Login.java
package vista;

import controlador.ControladorLogin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Pantalla de inicio de sesión.
 * Diseño en dos paneles: marca del instituto (izquierda) y formulario (derecha).
 * Los campos y botones se dibujan a mano para que se vean igual en cualquier
 * versión de Windows, independientemente del Look and Feel del sistema.
 */
public class Login extends JFrame {

    // ---------- Paleta ----------
    private static final Color AZUL_OSCURO   = new Color(12, 35, 64);
    private static final Color AZUL          = new Color(28, 92, 160);
    private static final Color AZUL_CLARO    = new Color(41, 128, 185);
    private static final Color DORADO        = new Color(232, 185, 72);
    private static final Color TEXTO         = new Color(30, 41, 59);
    private static final Color TEXTO_SUAVE   = new Color(100, 116, 139);
    private static final Color BORDE         = new Color(214, 222, 232);
    private static final Color FONDO_CAMPO   = new Color(248, 250, 252);
    private static final Color ROJO_SUAVE    = new Color(185, 74, 72);

    private static final String FUENTE = "Segoe UI";

    // ---------- Componentes usados por el controlador ----------
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnSalir;

    private JLabel lblBloqMayus;

    public Login() {
        inicializarComponentes();
        configurarVentana();

        // Inicializar controlador
        new ControladorLogin(this);
    }

    private void inicializarComponentes() {
        JPanel raiz = new JPanel(new GridLayout(1, 2));
        raiz.add(crearPanelMarca());
        raiz.add(crearPanelFormulario());
        setContentPane(raiz);
    }

    // =====================================================================
    // Panel izquierdo: marca
    // =====================================================================
    private JPanel crearPanelMarca() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = suavizar(g);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, AZUL_OSCURO, w, h, AZUL));
                g2.fillRect(0, 0, w, h);

                // Círculos decorativos translúcidos
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fill(new Ellipse2D.Double(-120, -140, 360, 360));
                g2.fill(new Ellipse2D.Double(w - 170, h - 190, 320, 320));
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fill(new Ellipse2D.Double(w - 120, 40, 160, 160));
                g2.dispose();
            }
        };
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Emblema dibujado
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 22, 0);
        panel.add(new Emblema(), gbc);

        JLabel lblInstituto = new JLabel("INSTITUTO AMERICANO");
        lblInstituto.setFont(new Font(FUENTE, Font.BOLD, 24));
        lblInstituto.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(lblInstituto, gbc);

        // Línea dorada
        JPanel linea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = suavizar(g);
                g2.setColor(DORADO);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 3, 3));
                g2.dispose();
            }
        };
        linea.setOpaque(false);
        linea.setPreferredSize(new Dimension(56, 3));
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 14, 0);
        panel.add(linea, gbc);

        JLabel lblSistema = new JLabel("Sistema de Asistencias y Planillas");
        lblSistema.setFont(new Font(FUENTE, Font.PLAIN, 15));
        lblSistema.setForeground(new Color(214, 228, 245));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 36, 0);
        panel.add(lblSistema, gbc);

        // Características
        String[] items = {"Control de asistencia por horario", "Cálculo automático de planillas", "Reportes exportables a Excel y PDF"};
        JPanel lista = new JPanel(new GridLayout(items.length, 1, 0, 10));
        lista.setOpaque(false);
        for (String item : items) {
            JLabel l = new JLabel(item, new IconoCheck(), SwingConstants.LEFT);
            l.setIconTextGap(10);
            l.setFont(new Font(FUENTE, Font.PLAIN, 13));
            l.setForeground(new Color(226, 236, 248));
            lista.add(l);
        }
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(lista, gbc);

        return panel;
    }

    // =====================================================================
    // Panel derecho: formulario
    // =====================================================================
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel lblBienvenido = new JLabel("Bienvenido");
        lblBienvenido.setFont(new Font(FUENTE, Font.BOLD, 28));
        lblBienvenido.setForeground(TEXTO);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(lblBienvenido, gbc);

        JLabel lblIndicacion = new JLabel("Ingresa tus credenciales para continuar");
        lblIndicacion.setFont(new Font(FUENTE, Font.PLAIN, 13));
        lblIndicacion.setForeground(TEXTO_SUAVE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        form.add(lblIndicacion, gbc);

        // Usuario
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        form.add(crearEtiqueta("Usuario"), gbc);

        txtUsuario = new JTextField();
        CampoRedondeado campoUsuario = new CampoRedondeado(txtUsuario, new IconoUsuario(), "Ingresa tu usuario", null);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 18, 0);
        form.add(campoUsuario, gbc);

        // Contraseña
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 6, 0);
        form.add(crearEtiqueta("Contraseña"), gbc);

        txtPassword = new JPasswordField();
        txtPassword.setEchoChar('•');
        BotonOjo ojo = new BotonOjo();
        ojo.addActionListener(e -> {
            ojo.visible = !ojo.visible;
            txtPassword.setEchoChar(ojo.visible ? (char) 0 : '•');
            ojo.setToolTipText(ojo.visible ? "Ocultar contraseña" : "Mostrar contraseña");
            ojo.repaint();
            txtPassword.requestFocusInWindow();
        });
        CampoRedondeado campoPassword = new CampoRedondeado(txtPassword, new IconoCandado(), "Ingresa tu contraseña", ojo);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 6, 0);
        form.add(campoPassword, gbc);

        // Aviso de Bloq Mayús
        lblBloqMayus = new JLabel(" ");
        lblBloqMayus.setFont(new Font(FUENTE, Font.PLAIN, 12));
        lblBloqMayus.setForeground(ROJO_SUAVE);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 2, 16, 0);
        form.add(lblBloqMayus, gbc);
        KeyAdapter revisarMayus = new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { actualizarAvisoMayus(); }
        };
        txtPassword.addKeyListener(revisarMayus);
        txtPassword.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { actualizarAvisoMayus(); }
            @Override public void focusLost(FocusEvent e) { lblBloqMayus.setText(" "); }
        });

        // Botones
        btnIngresar = new BotonPrincipal("Iniciar sesión");
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 10, 0);
        form.add(btnIngresar, gbc);

        btnSalir = new BotonSecundario("Salir");
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(btnSalir, gbc);

        // Pie
        JLabel lblPie = new JLabel("© " + java.time.Year.now() + " Instituto Americano", SwingConstants.CENTER);
        lblPie.setFont(new Font(FUENTE, Font.PLAIN, 11));
        lblPie.setForeground(new Color(148, 163, 184));
        gbc.gridy = 9;
        gbc.insets = new Insets(34, 0, 0, 0);
        form.add(lblPie, gbc);

        // Navegación con Enter: usuario -> contraseña -> ingresar
        txtUsuario.addActionListener(e -> txtPassword.requestFocusInWindow());

        panel.add(form);
        return panel;
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(FUENTE, Font.BOLD, 13));
        l.setForeground(TEXTO);
        return l;
    }

    private void actualizarAvisoMayus() {
        try {
            boolean activo = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
            lblBloqMayus.setText(activo ? "Bloq Mayús está activado" : " ");
        } catch (UnsupportedOperationException ex) {
            lblBloqMayus.setText(" ");
        }
    }

    private void configurarVentana() {
        setTitle("Iniciar sesión - Instituto Americano");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setMinimumSize(new Dimension(820, 520));
        setResizable(false);
        setLocationRelativeTo(null);
        // Enter en el campo de contraseña inicia sesión
        getRootPane().setDefaultButton(btnIngresar);
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { txtUsuario.requestFocusInWindow(); }
        });
    }

    // Getters para los componentes (usados por ControladorLogin)
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

    // =====================================================================
    // Utilidades de dibujo
    // =====================================================================
    private static Graphics2D suavizar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    /** Contenedor redondeado con icono, campo de texto, texto guía y botón opcional a la derecha. */
    private static class CampoRedondeado extends JPanel {
        private final JTextField campo;
        private final String guia;
        private boolean enfocado;

        CampoRedondeado(JTextField campo, Icon icono, String guia, JComponent derecha) {
            super(new BorderLayout(10, 0));
            this.campo = campo;
            this.guia = guia;
            setOpaque(false);
            setBorder(new EmptyBorder(0, 14, 0, derecha != null ? 6 : 14));
            setPreferredSize(new Dimension(320, 46));
            setMinimumSize(new Dimension(320, 46));

            JLabel lblIcono = new JLabel(icono);
            add(lblIcono, BorderLayout.WEST);

            campo.setOpaque(false);
            campo.setBorder(null);
            campo.setFont(new Font(FUENTE, Font.PLAIN, 14));
            campo.setForeground(TEXTO);
            campo.setCaretColor(AZUL);
            campo.setSelectionColor(new Color(190, 215, 245));
            add(new JLayer<>(campo, new javax.swing.plaf.LayerUI<JTextField>() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    super.paint(g, c);
                    if (campo.getDocument().getLength() == 0 && !campo.isFocusOwner()) {
                        Graphics2D g2 = suavizar(g);
                        g2.setFont(campo.getFont());
                        g2.setColor(new Color(148, 163, 184));
                        FontMetrics fm = g2.getFontMetrics();
                        int y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                        g2.drawString(guia, 2, y);
                        g2.dispose();
                    }
                }
            }), BorderLayout.CENTER);

            if (derecha != null) {
                add(derecha, BorderLayout.EAST);
            }

            campo.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { enfocado = true; repaint(); }
                @Override public void focusLost(FocusEvent e) { enfocado = false; repaint(); }
            });
            MouseAdapter enfocar = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { campo.requestFocusInWindow(); }
            };
            addMouseListener(enfocar);
            lblIcono.addMouseListener(enfocar);
            setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            int w = getWidth(), h = getHeight();
            if (enfocado) {
                g2.setColor(new Color(41, 128, 185, 40));
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 14, 14));
            }
            g2.setColor(enfocado ? Color.WHITE : FONDO_CAMPO);
            g2.fill(new RoundRectangle2D.Double(2, 2, w - 4, h - 4, 12, 12));
            g2.setStroke(new BasicStroke(enfocado ? 1.6f : 1f));
            g2.setColor(enfocado ? AZUL_CLARO : BORDE);
            g2.draw(new RoundRectangle2D.Double(2, 2, w - 4, h - 4, 12, 12));
            g2.dispose();
        }
    }

    /** Botón principal relleno con degradado y efecto al pasar el mouse. */
    private static class BotonPrincipal extends JButton {
        private boolean encima;

        BotonPrincipal(String texto) {
            super(texto);
            setFont(new Font(FUENTE, Font.BOLD, 15));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(320, 48));
            setMinimumSize(new Dimension(320, 48));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { encima = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { encima = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            int w = getWidth(), h = getHeight();
            boolean presionado = getModel().isPressed();
            Color c1 = presionado ? AZUL_OSCURO : encima ? new Color(22, 78, 140) : AZUL;
            Color c2 = presionado ? AZUL : encima ? AZUL : AZUL_CLARO;
            // sombra
            g2.setColor(new Color(28, 92, 160, encima ? 60 : 35));
            g2.fill(new RoundRectangle2D.Double(2, 4, w - 4, h - 4, 12, 12));
            g2.setPaint(new GradientPaint(0, 0, c1, w, 0, c2));
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h - 3, 12, 12));
            if (isFocusOwner()) {
                g2.setColor(new Color(255, 255, 255, 120));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Double(3, 3, w - 6, h - 9, 9, 9));
            }
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(getText())) / 2;
            int y = (h - 3 - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    /** Botón secundario con contorno. */
    private static class BotonSecundario extends JButton {
        private boolean encima;

        BotonSecundario(String texto) {
            super(texto);
            setFont(new Font(FUENTE, Font.BOLD, 14));
            setForeground(TEXTO_SUAVE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(320, 42));
            setMinimumSize(new Dimension(320, 42));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { encima = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { encima = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            int w = getWidth(), h = getHeight();
            g2.setColor(encima ? new Color(241, 245, 249) : Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 12, 12));
            g2.setColor(encima || isFocusOwner() ? new Color(148, 163, 184) : BORDE);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 12, 12));
            g2.setFont(getFont());
            g2.setColor(encima ? TEXTO : getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Botón con icono de ojo para mostrar u ocultar la contraseña. */
    private static class BotonOjo extends JButton {
        boolean visible;

        BotonOjo() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(34, 34));
            setToolTipText("Mostrar contraseña");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            int cx = getWidth() / 2, cy = getHeight() / 2;
            g2.setColor(getModel().isRollover() ? AZUL_CLARO : TEXTO_SUAVE);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D ojo = new Path2D.Double();
            ojo.moveTo(cx - 9, cy);
            ojo.quadTo(cx, cy - 8, cx + 9, cy);
            ojo.quadTo(cx, cy + 8, cx - 9, cy);
            g2.draw(ojo);
            g2.fill(new Ellipse2D.Double(cx - 2.6, cy - 2.6, 5.2, 5.2));
            if (!visible) {
                g2.draw(new Line2D.Double(cx - 8, cy + 7, cx + 8, cy - 7));
            }
            g2.dispose();
        }
    }

    /** Emblema del instituto: escudo con edificio y libro. */
    private static class Emblema extends JComponent {
        Emblema() {
            setPreferredSize(new Dimension(110, 110));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            int s = Math.min(getWidth(), getHeight());
            double ox = (getWidth() - s) / 2.0, oy = (getHeight() - s) / 2.0;

            // Anillo exterior
            g2.setColor(new Color(255, 255, 255, 30));
            g2.fill(new Ellipse2D.Double(ox, oy, s, s));
            g2.setColor(new Color(255, 255, 255, 220));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Ellipse2D.Double(ox + 6, oy + 6, s - 12, s - 12));

            // Edificio
            double cx = ox + s / 2.0;
            double base = oy + s * 0.72;
            g2.setColor(Color.WHITE);
            Path2D techo = new Path2D.Double();
            techo.moveTo(cx - s * 0.30, oy + s * 0.42);
            techo.lineTo(cx, oy + s * 0.24);
            techo.lineTo(cx + s * 0.30, oy + s * 0.42);
            techo.closePath();
            g2.fill(techo);
            // Columnas
            double colW = s * 0.06, colH = s * 0.22, inicio = cx - s * 0.24;
            for (int i = 0; i < 4; i++) {
                double x = inicio + i * (s * 0.48 - colW) / 3.0;
                g2.fill(new RoundRectangle2D.Double(x, oy + s * 0.46, colW, colH, 2, 2));
            }
            // Base
            g2.fill(new RoundRectangle2D.Double(cx - s * 0.32, base - s * 0.04, s * 0.64, s * 0.06, 3, 3));

            // Detalle dorado en el frontón
            g2.setColor(DORADO);
            g2.fill(new Ellipse2D.Double(cx - s * 0.04, oy + s * 0.32, s * 0.08, s * 0.08));
            g2.dispose();
        }
    }

    // ---------- Iconos pequeños ----------
    private abstract static class IconoBase implements Icon {
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
    }

    private static class IconoUsuario extends IconoBase {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = suavizar(g);
            g2.setColor(TEXTO_SUAVE);
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(new Ellipse2D.Double(x + 5.5, y + 2, 7, 7));
            g2.draw(new Arc2D.Double(x + 2, y + 10.5, 14, 12, 0, 180, Arc2D.OPEN));
            g2.dispose();
        }
    }

    private static class IconoCandado extends IconoBase {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = suavizar(g);
            g2.setColor(TEXTO_SUAVE);
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(new RoundRectangle2D.Double(x + 3, y + 8, 12, 9, 3, 3));
            g2.draw(new Arc2D.Double(x + 5.5, y + 2, 7, 10, 0, 180, Arc2D.OPEN));
            g2.fill(new Ellipse2D.Double(x + 8, y + 11, 2.2, 2.2));
            g2.dispose();
        }
    }

    private static class IconoCheck implements Icon {
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = suavizar(g);
            g2.setColor(new Color(232, 185, 72, 60));
            g2.fill(new Ellipse2D.Double(x, y, 18, 18));
            g2.setColor(DORADO);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D p = new Path2D.Double();
            p.moveTo(x + 5, y + 9.5);
            p.lineTo(x + 8, y + 12.5);
            p.lineTo(x + 13, y + 6);
            g2.draw(p);
            g2.dispose();
        }
    }
}
