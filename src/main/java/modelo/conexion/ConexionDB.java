// modelo/conexion/ConexionDB.java
package modelo.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/instituto_americano";
    private static final String USUARIO = "root";
    // IMPORTANTE: Si tu MySQL tiene otra contraseña (ej: "2024", "root" o vacía ""), cámbiala aquí:
    private static final String PASSWORD = "root";
    private static Connection conexion = null;
    
    // Constructor privado para patrón Singleton
    private ConexionDB() {}
    
    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                System.out.println("Conexión exitosa a la base de datos");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver no encontrado - " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
        return conexion;
    }
    
    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
}