// modelo/dao/AsistenciaDAO.java
package modelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import modelo.conexion.ConexionDB;
import modelo.entidades.Asistencia;

public class AsistenciaDAO {
    
    public AsistenciaDAO() {
    }
    
    // Registrar entrada (opcionalmente asociada a un bloque de horario_personal)
    public boolean registrarEntrada(Asistencia asistencia) {
        String sql = "INSERT INTO asistencia (id_personal, id_horario, fecha, hora_entrada, "
                    + "minutos_tardanza, estado, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, asistencia.getIdPersonal());
            if (asistencia.getIdHorario() != null) {
                ps.setInt(2, asistencia.getIdHorario());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setDate(3, new java.sql.Date(asistencia.getFecha().getTime()));
            ps.setTime(4, asistencia.getHoraEntrada());
            ps.setInt(5, asistencia.getMinutosTardanza());
            ps.setString(6, asistencia.getEstado());
            ps.setString(7, asistencia.getObservaciones());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar entrada: " + e.getMessage());
            return false;
        }
    }
    
    // Registrar salida, incluyendo minutos extras y horas trabajadas ya calculadas
    public boolean registrarSalida(int idAsistencia, Time horaSalida, int minutosExtras, double horasTrabajadas) {
        String sql = "UPDATE asistencia SET hora_salida = ?, minutos_extras = ?, horas_trabajadas = ? "
                    + "WHERE id_asistencia = ? AND hora_salida IS NULL";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setTime(1, horaSalida);
            ps.setInt(2, minutosExtras);
            ps.setDouble(3, horasTrabajadas);
            ps.setInt(4, idAsistencia);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar salida: " + e.getMessage());
            return false;
        }
    }

    // Fallback robusto: si el registro abierto no se pudo localizar por ID, se actualiza
    // la última asistencia abierta del personal en la fecha indicada.
    public boolean registrarSalidaPorPersonalFecha(int idPersonal, Date fecha, Time horaSalida,
                                                 int minutosExtras, double horasTrabajadas) {
        String sql = "UPDATE asistencia SET hora_salida = ?, minutos_extras = ?, horas_trabajadas = ? "
                    + "WHERE id_personal = ? AND fecha = ? AND hora_entrada IS NOT NULL "
                    + "AND hora_salida IS NULL ORDER BY hora_entrada DESC LIMIT 1";

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setTime(1, horaSalida);
            ps.setInt(2, minutosExtras);
            ps.setDouble(3, horasTrabajadas);
            ps.setInt(4, idPersonal);
            ps.setDate(5, new java.sql.Date(fecha.getTime()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar salida por personal y fecha: " + e.getMessage());
            return false;
        }
    }

    // Registrar una falta (ausencia) para un bloque de horario que ya pasó y no tuvo marcación
    public boolean registrarFalta(int idPersonal, int idHorario, Date fecha, String observaciones) {
        String sql = "INSERT IGNORE INTO asistencia (id_personal, id_horario, fecha, estado, observaciones) "
                    + "VALUES (?, ?, ?, 'AUSENTE', ?)";

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idPersonal);
            ps.setInt(2, idHorario);
            ps.setDate(3, new java.sql.Date(fecha.getTime()));
            ps.setString(4, observaciones);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar falta: " + e.getMessage());
            return false;
        }
    }
    
    // Verificar si ya existe registro para un bloque de horario específico en una fecha
    public boolean existeRegistroBloque(int idPersonal, Date fecha, Integer idHorario) {
        String sql = "SELECT COUNT(*) FROM asistencia WHERE id_personal = ? AND fecha = ? "
                    + (idHorario != null ? "AND id_horario = ?" : "AND id_horario IS NULL");
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idPersonal);
            ps.setDate(2, new java.sql.Date(fecha.getTime()));
            if (idHorario != null) {
                ps.setInt(3, idHorario);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar registro: " + e.getMessage());
        }
        
        return false;
    }

    // Mantener compatibilidad: verifica si existe cualquier registro ese día (cualquier bloque)
    public boolean existeRegistroHoy(int idPersonal, Date fecha) {
        String sql = "SELECT COUNT(*) FROM asistencia WHERE id_personal = ? AND fecha = ?";

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idPersonal);
            ps.setDate(2, new java.sql.Date(fecha.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar registro: " + e.getMessage());
        }

        return false;
    }
    
    // Obtener el bloque de asistencia del día que quedó abierto (con entrada pero sin salida)
    public Asistencia obtenerBloqueAbierto(int idPersonal, Date fecha) {
        Asistencia asistencia = null;
        String sql = "SELECT * FROM asistencia WHERE id_personal = ? AND fecha = ? "
                    + "AND hora_entrada IS NOT NULL AND hora_salida IS NULL "
                    + "ORDER BY hora_entrada DESC LIMIT 1";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idPersonal);
            ps.setDate(2, new java.sql.Date(fecha.getTime()));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    asistencia = mapearAsistencia(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener bloque abierto: " + e.getMessage());
        }
        
        return asistencia;
    }

    // Última asistencia con entrada y sin salida del personal (de cualquier fecha).
    // Ya no se usa para registrar salidas: solo sirve para consultas/diagnóstico.
    public Asistencia obtenerUltimaAsistenciaAbierta(int idPersonal) {
        Asistencia asistencia = null;
        String sql = "SELECT * FROM asistencia WHERE id_personal = ? "
                    + "AND hora_entrada IS NOT NULL AND hora_salida IS NULL "
                    + "ORDER BY fecha DESC, hora_entrada DESC LIMIT 1";

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idPersonal);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    asistencia = mapearAsistencia(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener última asistencia abierta: " + e.getMessage());
        }

        return asistencia;
    }

    // Mantener compatibilidad: primer registro de asistencia del día (cualquier bloque)
    public Asistencia obtenerAsistenciaDelDia(int idPersonal, Date fecha) {
        Asistencia asistencia = null;
        String sql = "SELECT * FROM asistencia WHERE id_personal = ? AND fecha = ? ORDER BY hora_entrada DESC LIMIT 1";

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idPersonal);
            ps.setDate(2, new java.sql.Date(fecha.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    asistencia = mapearAsistencia(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener asistencia: " + e.getMessage());
        }

        return asistencia;
    }

    // Listar todos los bloques de asistencia registrados en una fecha para un personal
    public List<Asistencia> listarAsistenciasDelDia(int idPersonal, Date fecha) {
        List<Asistencia> lista = new ArrayList<>();
        String sql = "SELECT * FROM asistencia WHERE id_personal = ? AND fecha = ? ORDER BY hora_entrada";

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idPersonal);
            ps.setDate(2, new java.sql.Date(fecha.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAsistencia(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar asistencias del día: " + e.getMessage());
        }

        return lista;
    }
    
    // Listar asistencias por rango de fechas
    public List<Asistencia> listarPorRangoFechas(Date fechaInicio, Date fechaFin) {
        List<Asistencia> listaAsistencias = new ArrayList<>();
        String sql = "SELECT * FROM asistencia WHERE fecha BETWEEN ? AND ? ORDER BY fecha DESC, hora_entrada";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            ps.setDate(2, new java.sql.Date(fechaFin.getTime()));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaAsistencias.add(mapearAsistencia(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar asistencias: " + e.getMessage());
        }
        
        return listaAsistencias;
    }
    
    // Listar asistencias por personal y mes
    public List<Asistencia> listarPorPersonalYMes(int idPersonal, String periodo) {
        List<Asistencia> listaAsistencias = new ArrayList<>();
        String sql = "SELECT * FROM asistencia WHERE id_personal = ? AND DATE_FORMAT(fecha, '%Y-%m') = ? ORDER BY fecha, hora_entrada";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idPersonal);
            ps.setString(2, periodo);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaAsistencias.add(mapearAsistencia(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar asistencias por mes: " + e.getMessage());
        }
        
        return listaAsistencias;
    }
    
    // Actualizar minutos de tardanza
    public boolean actualizarTardanza(int idAsistencia, int minutosTardanza) {
        String sql = "UPDATE asistencia SET minutos_tardanza = ? WHERE id_asistencia = ?";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, minutosTardanza);
            ps.setInt(2, idAsistencia);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar tardanza: " + e.getMessage());
            return false;
        }
    }

    private Asistencia mapearAsistencia(ResultSet rs) throws SQLException {
        Asistencia asistencia = new Asistencia();
        asistencia.setIdAsistencia(rs.getInt("id_asistencia"));
        asistencia.setIdPersonal(rs.getInt("id_personal"));
        int idHorario = rs.getInt("id_horario");
        asistencia.setIdHorario(rs.wasNull() ? null : idHorario);
        asistencia.setFecha(rs.getDate("fecha"));
        asistencia.setHoraEntrada(rs.getTime("hora_entrada"));
        asistencia.setHoraSalida(rs.getTime("hora_salida"));
        asistencia.setHorasTrabajadas(rs.getDouble("horas_trabajadas"));
        asistencia.setMinutosExtras(rs.getInt("minutos_extras"));
        asistencia.setMinutosTardanza(rs.getInt("minutos_tardanza"));
        asistencia.setEstado(rs.getString("estado"));
        asistencia.setObservaciones(rs.getString("observaciones"));
        return asistencia;
    }

    // Cuenta los registros cuyas observaciones empiezan con el prefijo indicado
    public int contarPorPrefijoObservacion(String prefijo) {
        String sql = "SELECT COUNT(*) FROM asistencia WHERE LEFT(observaciones, ?) = ?";
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, prefijo.length());
            ps.setString(2, prefijo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al contar registros: " + e.getMessage());
            return 0;
        }
    }

    // Elimina los registros cuyas observaciones empiezan con el prefijo indicado
    public int eliminarPorPrefijoObservacion(String prefijo) {
        String sql = "DELETE FROM asistencia WHERE LEFT(observaciones, ?) = ?";
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, prefijo.length());
            ps.setString(2, prefijo);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar registros: " + e.getMessage());
            return 0;
        }
    }

    // Cantidad de registros simulados dentro de un periodo (AAAA-MM)
    public int contarSimuladosEnPeriodo(String periodo, String prefijo) {
        String sql = "SELECT COUNT(*) FROM asistencia WHERE DATE_FORMAT(fecha, '%Y-%m') = ? AND LEFT(observaciones, ?) = ?";
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setString(1, periodo);
            ps.setInt(2, prefijo.length());
            ps.setString(3, prefijo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al contar registros simulados: " + e.getMessage());
            return 0;
        }
    }
}
