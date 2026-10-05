// Main.java (en la raíz del proyecto src)
package main;

import vista.Login;
import modelo.conexion.ConexionDB;
import javax.swing.UIManager;
import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        try {
            // Establecer Look and Feel del sistema
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Probar conexión a la base de datos
            if (ConexionDB.getConexion() != null) {
                System.out.println("Sistema iniciado correctamente");
                System.out.println("Conexión a base de datos establecida");
                
                // Iniciar la ventana de Login
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        Login login = new Login();
                        login.setVisible(true);
                        login.setLocationRelativeTo(null);
                    }
                });
            } else {
                JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a la base de datos.\n" +
                    "Verifique que MySQL esté ejecutándose y que\n" +
                    "la base de datos 'instituto_americano' exista.",
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error al iniciar la aplicación:\n" + e.getMessage(),
                "Error Crítico",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
