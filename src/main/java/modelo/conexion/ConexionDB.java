// modelo/conexion/ConexionDB.java
package modelo.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexión única a MySQL.
 * - obtener(): la usan los DAO. Si la conexión se cayó, intenta reconectar; si MySQL no
 *   responde, lanza una SQLException con un mensaje claro y avisa al usuario (una sola vez
 *   cada cierto tiempo, para no llenar la pantalla de mensajes).
 * - getConexion(): igual que antes, devuelve null si no hay conexión (lo usa Main al iniciar).
 * - Transacciones: iniciarTransaccion(), confirmar() y revertir().
 */
public class ConexionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/instituto_americano"
            + "?connectTimeout=5000&socketTimeout=30000";
    private static final String USUARIO = "root";
    // IMPORTANTE: Si tu MySQL tiene otra contraseña (ej: "2024", "root" o vacía ""), cámbiala aquí:
    private static final String PASSWORD = "root";

    public static final String MENSAJE_SIN_CONEXION =
            "No hay conexión con la base de datos.\n"
            + "Verifique que MySQL esté en ejecución y que la base 'instituto_americano' exista.";

    private static Connection conexion = null;
    private static long ultimaValidacion = 0;
    private static long ultimoAviso = 0;

    // Constructor privado para patrón Singleton
    private ConexionDB() {}

    /** Conexión lista para usar, o SQLException con un mensaje claro si MySQL no responde. */
    public static synchronized Connection obtener() throws SQLException {
        try {
            if (conexion == null || conexion.isClosed() || !validaReciente()) {
                cerrarSilencioso();
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                ultimaValidacion = System.currentTimeMillis();
                System.out.println("Conexión exitosa a la base de datos");
            }
            return conexion;
        } catch (ClassNotFoundException e) {
            avisar();
            throw new SQLException("Driver de MySQL no encontrado: " + e.getMessage(), e);
        } catch (SQLException e) {
            conexion = null;
            avisar();
            throw new SQLException(MENSAJE_SIN_CONEXION.replace('\n', ' ') + " (" + e.getMessage() + ")", e);
        }
    }

    // Verifica la conexión como máximo cada 5 segundos (evita una consulta extra en cada operación)
    private static boolean validaReciente() throws SQLException {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimaValidacion < 5000) {
            return true;
        }
        boolean valida = conexion.isValid(2);
        if (valida) {
            ultimaValidacion = ahora;
        }
        return valida;
    }

    private static void avisar() {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimoAviso < 15000 || java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }
        ultimoAviso = ahora;
        Runnable mostrar = () -> javax.swing.JOptionPane.showMessageDialog(null,
                MENSAJE_SIN_CONEXION, "Sin conexión a la base de datos",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            mostrar.run();
        } else {
            javax.swing.SwingUtilities.invokeLater(mostrar);
        }
    }

    private static void cerrarSilencioso() {
        try {
            if (conexion != null) {
                conexion.close();
            }
        } catch (SQLException ignorada) {
            // la conexión ya no servía
        }
        conexion = null;
    }

    /** Devuelve la conexión o null si no se pudo conectar (sin lanzar excepción). */
    public static Connection getConexion() {
        try {
            return obtener();
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return null;
        }
    }

    // ---------- Transacciones ----------

    /** Inicia una transacción: los cambios no se guardan hasta llamar a confirmar(). */
    public static Connection iniciarTransaccion() throws SQLException {
        Connection c = obtener();
        c.setAutoCommit(false);
        return c;
    }

    /** Guarda todos los cambios de la transacción en curso. */
    public static void confirmar() throws SQLException {
        Connection c = obtener();
        try {
            c.commit();
        } finally {
            c.setAutoCommit(true);
        }
    }

    /** Deshace todos los cambios de la transacción en curso. */
    public static void revertir() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                try {
                    conexion.rollback();
                } finally {
                    conexion.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al revertir la transacción: " + e.getMessage());
        }
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
