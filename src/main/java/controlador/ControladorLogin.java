// controlador/ControladorLogin.java
package controlador;

import modelo.dao.UsuarioDAO;
import modelo.entidades.Usuario;
import vista.Login;
import vista.MenuPrincipal;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControladorLogin implements ActionListener {
    private Login vista;
    private UsuarioDAO usuarioDAO;
    
    public ControladorLogin(Login vista) {
        this.vista = vista;
        this.usuarioDAO = new UsuarioDAO();
        
        // Registrar eventos
        this.vista.getBtnIngresar().addActionListener(this);
        this.vista.getBtnSalir().addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.getBtnIngresar()) {
            iniciarSesion();
        } else if (e.getSource() == vista.getBtnSalir()) {
            cerrarAplicacion();
        }
    }
    
    private void iniciarSesion() {
        String nombreUsuario = vista.getTxtUsuario().getText().trim();
        String contrasena = new String(vista.getTxtPassword().getPassword());
        
        // Validar campos vacíos
        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(vista, 
                "Por favor complete todos los campos", 
                "Campos vacíos", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validar credenciales
        Usuario usuario = usuarioDAO.validarLogin(nombreUsuario, contrasena);
        
        if (usuario != null) {
            if (usuario.getIdRol() != 1 && usuario.getIdRol() != 2 && usuario.getIdRol() != 3) {
                JOptionPane.showMessageDialog(vista,
                    "El usuario no tiene un rol válido para ingresar al sistema.",
                    "Rol no autorizado",
                    JOptionPane.ERROR_MESSAGE);
                vista.getTxtPassword().setText("");
                vista.getTxtUsuario().requestFocus();
                return;
            }

            JOptionPane.showMessageDialog(vista, 
                "¡Bienvenido " + usuario.getNombreUsuario() + "!", 
                "Inicio de sesión exitoso", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Abrir menú principal y pasar el usuario
            MenuPrincipal menuPrincipal = new MenuPrincipal(usuario);
            menuPrincipal.setVisible(true);
            vista.dispose();
        } else {
            JOptionPane.showMessageDialog(vista, 
                "Usuario o contraseña incorrectos", 
                "Error de autenticación", 
                JOptionPane.ERROR_MESSAGE);
            vista.getTxtPassword().setText("");
            vista.getTxtUsuario().requestFocus();
        }
    }
    
    private void cerrarAplicacion() {
        int confirmacion = JOptionPane.showConfirmDialog(vista, 
            "¿Está seguro de que desea salir?", 
            "Confirmar salida", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}