// modelo/dao/FeriadoDAO.java
package modelo.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelo.conexion.ConexionDB;
import modelo.entidades.Feriado;

public class FeriadoDAO {

    /** Todos los feriados, del más reciente al más antiguo. */
    public List<Feriado> listar() {
        List<Feriado> lista = new ArrayList<>();
        String sql = "SELECT fecha, descripcion FROM feriado ORDER BY fecha DESC";
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Feriado(rs.getDate("fecha").toLocalDate(), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar feriados: " + e.getMessage());
        }
        return lista;
    }

    /** Fechas feriadas dentro de un rango (incluye ambos extremos). */
    public Set<LocalDate> fechasEntre(LocalDate inicio, LocalDate fin) {
        Set<LocalDate> fechas = new HashSet<>();
        String sql = "SELECT fecha FROM feriado WHERE fecha BETWEEN ? AND ?";
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(inicio));
            ps.setDate(2, java.sql.Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    fechas.add(rs.getDate("fecha").toLocalDate());
                }
            }
        } catch (SQLException e) {
            // Si la tabla aún no existe (base antigua sin migrar), se trabaja sin feriados
            System.err.println("Error al consultar feriados: " + e.getMessage());
        }
        return fechas;
    }

    /** Descripción del feriado de esa fecha, o null si no es feriado. */
    public String descripcion(LocalDate fecha) {
        String sql = "SELECT descripcion FROM feriado WHERE fecha = ?";
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar feriado: " + e.getMessage());
            return null;
        }
    }

    /** Registra (o actualiza la descripción de) un feriado. */
    public boolean guardar(Feriado feriado) {
        String sql = "INSERT INTO feriado (fecha, descripcion) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion)";
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(feriado.getFecha()));
            ps.setString(2, feriado.getDescripcion());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar feriado: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(LocalDate fecha) {
        String sql = "DELETE FROM feriado WHERE fecha = ?";
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fecha));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar feriado: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina las faltas automáticas (AUSENTE sin hora de entrada) registradas en una fecha:
     * se usa al declarar feriado un día que ya tenía faltas generadas.
     */
    public int eliminarFaltasAutomaticas(LocalDate fecha) {
        String sql = "DELETE FROM asistencia WHERE fecha = ? AND estado = 'AUSENTE' AND hora_entrada IS NULL";
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fecha));
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar faltas del feriado: " + e.getMessage());
            return 0;
        }
    }
}
