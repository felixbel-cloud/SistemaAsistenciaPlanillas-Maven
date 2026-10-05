// modelo/dao/PlanillaDAO.java
package modelo.dao;

import modelo.conexion.ConexionDB;
import modelo.entidades.Planilla;
import modelo.entidades.DetallePlanilla;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanillaDAO {

    private Connection conexion;

    public PlanillaDAO() {
        this.conexion = ConexionDB.getConexion();
    }

    // Crear planilla
    public int crearPlanilla(Planilla planilla) {
        String sql = "INSERT INTO planilla (periodo, fecha_generacion, total_planilla, estado, generado_por) "
                + "VALUES (?, ?, ?, ?, ?)";
        int idGenerado = -1;

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, planilla.getPeriodo());
            ps.setDate(2, new java.sql.Date(planilla.getFechaGeneracion().getTime()));
            ps.setDouble(3, planilla.getTotalPlanilla());
            ps.setString(4, planilla.getEstado());
            ps.setInt(5, planilla.getGeneradoPor());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear planilla: " + e.getMessage());
        }

        return idGenerado;
    }

    // Crear detalle de planilla
    public boolean crearDetallePlanilla(DetallePlanilla detalle) {
        String sql = "INSERT INTO detalle_planilla (id_planilla, id_personal, dias_trabajados, "
                + "horas_extras, monto_horas_extras, dias_ausencia, descuento_ausencias, "
                + "minutos_tardanza, descuento_tardanzas, total_ingresos, total_descuentos, sueldo_neto) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdPlanilla());
            ps.setInt(2, detalle.getIdPersonal());
            ps.setInt(3, detalle.getDiasTrabajados());
            ps.setDouble(4, detalle.getHorasExtras());
            ps.setDouble(5, detalle.getMontoHorasExtras());
            ps.setInt(6, detalle.getDiasAusencia());
            ps.setDouble(7, detalle.getDescuentoAusencias());
            ps.setInt(8, detalle.getMinutosTardanza());
            ps.setDouble(9, detalle.getDescuentoTardanzas());
            ps.setDouble(10, detalle.getTotalIngresos());
            ps.setDouble(11, detalle.getTotalDescuentos());
            ps.setDouble(12, detalle.getSueldoNeto());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear detalle planilla: " + e.getMessage());
            return false;
        }
    }

    // Verificar si existe planilla para el periodo
    public boolean existePlanillaPeriodo(String periodo) {
        String sql = "SELECT COUNT(*) FROM planilla WHERE periodo = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, periodo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar planilla: " + e.getMessage());
        }

        return false;
    }

    // Listar planillas
    public List<Planilla> listarPlanillas() {
        List<Planilla> listaPlanillas = new ArrayList<>();
        String sql = "SELECT * FROM planilla ORDER BY fecha_generacion DESC";

        try (Statement stmt = conexion.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Planilla planilla = new Planilla();
                planilla.setIdPlanilla(rs.getInt("id_planilla"));
                planilla.setPeriodo(rs.getString("periodo"));
                planilla.setFechaGeneracion(rs.getDate("fecha_generacion"));
                planilla.setTotalPlanilla(rs.getDouble("total_planilla"));
                planilla.setEstado(rs.getString("estado"));
                planilla.setGeneradoPor(rs.getInt("generado_por"));
                listaPlanillas.add(planilla);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar planillas: " + e.getMessage());
        }

        return listaPlanillas;
    }

    // Obtener detalles de planilla con datos del personal
    public List<DetallePlanilla> obtenerDetallesPlanilla(int idPlanilla) {
        List<DetallePlanilla> detalles = new ArrayList<>();
        String sql = "SELECT dp.*, p.dni, CONCAT(p.nombres, ' ', p.apellidos) AS nombreCompleto, p.cargo "
                + "FROM detalle_planilla dp "
                + "JOIN personal p ON dp.id_personal = p.id_personal "
                + "WHERE dp.id_planilla = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPlanilla);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetallePlanilla detalle = new DetallePlanilla();
                    detalle.setIdDetalle(rs.getInt("id_detalle"));
                    detalle.setIdPlanilla(rs.getInt("id_planilla"));
                    detalle.setIdPersonal(rs.getInt("id_personal"));
                    detalle.setDiasTrabajados(rs.getInt("dias_trabajados"));
                    detalle.setHorasExtras(rs.getDouble("horas_extras"));
                    detalle.setMontoHorasExtras(rs.getDouble("monto_horas_extras"));
                    detalle.setDiasAusencia(rs.getInt("dias_ausencia"));
                    detalle.setDescuentoAusencias(rs.getDouble("descuento_ausencias"));
                    detalle.setMinutosTardanza(rs.getInt("minutos_tardanza"));
                    detalle.setDescuentoTardanzas(rs.getDouble("descuento_tardanzas"));
                    detalle.setTotalIngresos(rs.getDouble("total_ingresos"));
                    detalle.setTotalDescuentos(rs.getDouble("total_descuentos"));
                    detalle.setSueldoNeto(rs.getDouble("sueldo_neto"));

                    // Nuevos campos del personal
                    detalle.setDni(rs.getString("dni"));
                    detalle.setNombreCompleto(rs.getString("nombreCompleto"));
                    detalle.setCargo(rs.getString("cargo"));

                    detalles.add(detalle);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles: " + e.getMessage());
        }

        return detalles;
    }

    // Actualizar estado de planilla
    public boolean actualizarEstadoPlanilla(int idPlanilla, String estado) {
        String sql = "UPDATE planilla SET estado = ? WHERE id_planilla = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idPlanilla);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado: " + e.getMessage());
            return false;
        }
    }

    // Obtener planilla por ID
    public Planilla obtenerPlanillaPorId(int idPlanilla) {
        Planilla planilla = null;
        String sql = "SELECT * FROM planilla WHERE id_planilla = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPlanilla);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    planilla = new Planilla();
                    planilla.setIdPlanilla(rs.getInt("id_planilla"));
                    planilla.setPeriodo(rs.getString("periodo"));
                    planilla.setFechaGeneracion(rs.getDate("fecha_generacion"));
                    planilla.setTotalPlanilla(rs.getDouble("total_planilla"));
                    planilla.setEstado(rs.getString("estado"));
                    planilla.setGeneradoPor(rs.getInt("generado_por"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener planilla: " + e.getMessage());
        }

        return planilla;
    }

    public boolean actualizarTotalPlanilla(int idPlanilla, double total) {
        String sql = "UPDATE planilla SET total_planilla = ? WHERE id_planilla = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDouble(1, total);
            ps.setInt(2, idPlanilla);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar total de planilla: " + e.getMessage());
            return false;
        }
    }
}
