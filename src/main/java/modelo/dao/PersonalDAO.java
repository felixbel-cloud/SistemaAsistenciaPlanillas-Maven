// modelo/dao/PersonalDAO.java
package modelo.dao;

import modelo.conexion.ConexionDB;
import modelo.entidades.Personal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonalDAO {
    
    public PersonalDAO() {
    }
    
    // Crear personal
    public boolean crearPersonal(Personal personal) {
        String sql = "INSERT INTO personal (dni, nombres, apellidos, tipo_personal, cargo, " +
                    "fecha_contratacion, salario_base, horario_entrada, horario_salida, activo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setString(1, personal.getDni());
            ps.setString(2, personal.getNombres());
            ps.setString(3, personal.getApellidos());
            ps.setString(4, personal.getTipoPersonal());
            ps.setString(5, personal.getCargo());
            ps.setDate(6, new java.sql.Date(personal.getFechaContratacion().getTime()));
            ps.setDouble(7, personal.getSalarioBase());
            ps.setString(8, personal.getHorarioEntrada());
            ps.setString(9, personal.getHorarioSalida());
            ps.setBoolean(10, personal.isActivo());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear personal: " + e.getMessage());
            return false;
        }
    }
    
    // Listar todo el personal activo
    public List<Personal> listarPersonal() {
        return listarPersonal(false);
    }

    // Listar todo el personal, con opción de incluir los inactivos/dados de baja
    public List<Personal> listarPersonal(boolean incluirInactivos) {
        List<Personal> listaPersonal = new ArrayList<>();
        String sql = incluirInactivos
                ? "SELECT * FROM personal ORDER BY activo DESC, id_personal"
                : "SELECT * FROM personal WHERE activo = 1 ORDER BY id_personal";
        
        try (Statement stmt = ConexionDB.obtener().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Personal personal = new Personal();
                personal.setIdPersonal(rs.getInt("id_personal"));
                personal.setDni(rs.getString("dni"));
                personal.setNombres(rs.getString("nombres"));
                personal.setApellidos(rs.getString("apellidos"));
                personal.setTipoPersonal(rs.getString("tipo_personal"));
                personal.setCargo(rs.getString("cargo"));
                personal.setFechaContratacion(rs.getDate("fecha_contratacion"));
                personal.setSalarioBase(rs.getDouble("salario_base"));
                personal.setHorarioEntrada(rs.getString("horario_entrada"));
                personal.setHorarioSalida(rs.getString("horario_salida"));
                personal.setActivo(rs.getBoolean("activo"));
                listaPersonal.add(personal);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar personal: " + e.getMessage());
        }
        
        return listaPersonal;
    }
    
    // Buscar personal por ID
    public Personal buscarPorId(int idPersonal) {
        Personal personal = null;
        String sql = "SELECT * FROM personal WHERE id_personal = ?";

        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idPersonal);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    personal = new Personal();
                    personal.setIdPersonal(rs.getInt("id_personal"));
                    personal.setDni(rs.getString("dni"));
                    personal.setNombres(rs.getString("nombres"));
                    personal.setApellidos(rs.getString("apellidos"));
                    personal.setTipoPersonal(rs.getString("tipo_personal"));
                    personal.setCargo(rs.getString("cargo"));
                    personal.setFechaContratacion(rs.getDate("fecha_contratacion"));
                    personal.setSalarioBase(rs.getDouble("salario_base"));
                    personal.setHorarioEntrada(rs.getString("horario_entrada"));
                    personal.setHorarioSalida(rs.getString("horario_salida"));
                    personal.setActivo(rs.getBoolean("activo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar personal por id: " + e.getMessage());
        }

        return personal;
    }

    // Buscar personal por DNI
    public Personal buscarPorDni(String dni) {
        Personal personal = null;
        String sql = "SELECT * FROM personal WHERE dni = ? AND activo = 1";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setString(1, dni);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    personal = new Personal();
                    personal.setIdPersonal(rs.getInt("id_personal"));
                    personal.setDni(rs.getString("dni"));
                    personal.setNombres(rs.getString("nombres"));
                    personal.setApellidos(rs.getString("apellidos"));
                    personal.setTipoPersonal(rs.getString("tipo_personal"));
                    personal.setCargo(rs.getString("cargo"));
                    personal.setFechaContratacion(rs.getDate("fecha_contratacion"));
                    personal.setSalarioBase(rs.getDouble("salario_base"));
                    personal.setHorarioEntrada(rs.getString("horario_entrada"));
                    personal.setHorarioSalida(rs.getString("horario_salida"));
                    personal.setActivo(rs.getBoolean("activo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar personal: " + e.getMessage());
        }
        
        return personal;
    }

    // Buscar personal por DNI (incluyendo inactivos/dados de baja)
    public Personal buscarPorDniIncluyendoInactivos(String dni) {
        Personal personal = null;
        String sql = "SELECT * FROM personal WHERE dni = ?";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setString(1, dni);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    personal = new Personal();
                    personal.setIdPersonal(rs.getInt("id_personal"));
                    personal.setDni(rs.getString("dni"));
                    personal.setNombres(rs.getString("nombres"));
                    personal.setApellidos(rs.getString("apellidos"));
                    personal.setTipoPersonal(rs.getString("tipo_personal"));
                    personal.setCargo(rs.getString("cargo"));
                    personal.setFechaContratacion(rs.getDate("fecha_contratacion"));
                    personal.setSalarioBase(rs.getDouble("salario_base"));
                    personal.setHorarioEntrada(rs.getString("horario_entrada"));
                    personal.setHorarioSalida(rs.getString("horario_salida"));
                    personal.setActivo(rs.getBoolean("activo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar personal por DNI: " + e.getMessage());
        }
        
        return personal;
    }
    
    // Actualizar personal
    public boolean actualizarPersonal(Personal personal) {
        String sql = "UPDATE personal SET dni = ?, nombres = ?, apellidos = ?, tipo_personal = ?, " +
                    "cargo = ?, fecha_contratacion = ?, salario_base = ?, horario_entrada = ?, " +
                    "horario_salida = ?, activo = ? WHERE id_personal = ?";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setString(1, personal.getDni());
            ps.setString(2, personal.getNombres());
            ps.setString(3, personal.getApellidos());
            ps.setString(4, personal.getTipoPersonal());
            ps.setString(5, personal.getCargo());
            ps.setDate(6, new java.sql.Date(personal.getFechaContratacion().getTime()));
            ps.setDouble(7, personal.getSalarioBase());
            ps.setString(8, personal.getHorarioEntrada());
            ps.setString(9, personal.getHorarioSalida());
            ps.setBoolean(10, personal.isActivo());
            ps.setInt(11, personal.getIdPersonal());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar personal: " + e.getMessage());
            return false;
        }
    }
    
    // Eliminar personal (desactivar)
    public boolean eliminarPersonal(int idPersonal) {
        String sql = "UPDATE personal SET activo = 0 WHERE id_personal = ?";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idPersonal);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar personal: " + e.getMessage());
            return false;
        }
    }
    
    // Listar por tipo de personal
    public List<Personal> listarPorTipo(String tipoPersonal) {
        List<Personal> listaPersonal = new ArrayList<>();
        String sql = "SELECT * FROM personal WHERE tipo_personal = ? AND activo = 1";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setString(1, tipoPersonal);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Personal personal = new Personal();
                    personal.setIdPersonal(rs.getInt("id_personal"));
                    personal.setDni(rs.getString("dni"));
                    personal.setNombres(rs.getString("nombres"));
                    personal.setApellidos(rs.getString("apellidos"));
                    personal.setTipoPersonal(rs.getString("tipo_personal"));
                    personal.setCargo(rs.getString("cargo"));
                    personal.setFechaContratacion(rs.getDate("fecha_contratacion"));
                    personal.setSalarioBase(rs.getDouble("salario_base"));
                    personal.setHorarioEntrada(rs.getString("horario_entrada"));
                    personal.setHorarioSalida(rs.getString("horario_salida"));
                    personal.setActivo(rs.getBoolean("activo"));
                    listaPersonal.add(personal);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar por tipo: " + e.getMessage());
        }
        
        return listaPersonal;
    }
}
