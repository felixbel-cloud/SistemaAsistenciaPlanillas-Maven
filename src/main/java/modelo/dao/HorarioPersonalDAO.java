package modelo.dao;

import modelo.conexion.ConexionDB;
import modelo.entidades.HorarioPersonal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HorarioPersonalDAO {
    private static final String ORDEN_DIAS = "FIELD(dia_semana, 'LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO')";

    public HorarioPersonalDAO() {
    }

    public List<HorarioPersonal> listarPorPersonal(int idPersonal) {
        String sql = "SELECT * FROM horario_personal WHERE id_personal = ? AND activo = 1 ORDER BY " + ORDEN_DIAS + ", hora_inicio";
        return listar(sql, idPersonal, null);
    }

    public List<HorarioPersonal> listarPorPersonalYDia(int idPersonal, String diaSemana) {
        String sql = "SELECT * FROM horario_personal WHERE id_personal = ? AND dia_semana = ? AND activo = 1 ORDER BY hora_inicio";
        return listar(sql, idPersonal, diaSemana);
    }

    public List<HorarioPersonal> listarTodos() {
        String sql = "SELECT h.* FROM horario_personal h JOIN personal p ON p.id_personal = h.id_personal "
                + "WHERE h.activo = 1 AND p.activo = 1 ORDER BY p.apellidos, p.nombres, " + ORDEN_DIAS + ", h.hora_inicio";
        List<HorarioPersonal> horarios = new ArrayList<>();

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                horarios.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar horarios: " + e.getMessage());
        }
        return horarios;
    }

    public boolean crear(HorarioPersonal horario) {
        String sql = "INSERT INTO horario_personal (id_personal, dia_semana, hora_inicio, hora_fin, descripcion_bloque, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, horario.getIdPersonal());
            ps.setString(2, horario.getDiaSemana());
            ps.setTime(3, horario.getHoraInicio());
            ps.setTime(4, horario.getHoraFin());
            ps.setString(5, horario.getDescripcionBloque());
            ps.setBoolean(6, horario.isActivo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear horario: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(HorarioPersonal horario) {
        String sql = "UPDATE horario_personal SET id_personal = ?, dia_semana = ?, hora_inicio = ?, hora_fin = ?, "
                + "descripcion_bloque = ? WHERE id_horario = ? AND activo = 1";

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, horario.getIdPersonal());
            ps.setString(2, horario.getDiaSemana());
            ps.setTime(3, horario.getHoraInicio());
            ps.setTime(4, horario.getHoraFin());
            ps.setString(5, horario.getDescripcionBloque());
            ps.setInt(6, horario.getIdHorario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar horario: " + e.getMessage());
            return false;
        }
    }

    public boolean desactivar(int idHorario) {
        String sql = "UPDATE horario_personal SET activo = 0 WHERE id_horario = ? AND activo = 1";

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idHorario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al desactivar horario: " + e.getMessage());
            return false;
        }
    }

    private List<HorarioPersonal> listar(String sql, int idPersonal, String diaSemana) {
        List<HorarioPersonal> horarios = new ArrayList<>();

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idPersonal);
            if (diaSemana != null) {
                ps.setString(2, diaSemana);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    horarios.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar horarios: " + e.getMessage());
        }
        return horarios;
    }

    private HorarioPersonal mapear(ResultSet rs) throws SQLException {
        HorarioPersonal horario = new HorarioPersonal();
        horario.setIdHorario(rs.getInt("id_horario"));
        horario.setIdPersonal(rs.getInt("id_personal"));
        horario.setDiaSemana(rs.getString("dia_semana"));
        horario.setHoraInicio(rs.getTime("hora_inicio"));
        horario.setHoraFin(rs.getTime("hora_fin"));
        horario.setDescripcionBloque(rs.getString("descripcion_bloque"));
        horario.setActivo(rs.getBoolean("activo"));
        return horario;
    }
}