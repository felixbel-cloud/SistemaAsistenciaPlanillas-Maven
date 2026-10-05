// modelo/dao/UsuarioDAO.java
package modelo.dao;

import modelo.conexion.ConexionDB;
import modelo.entidades.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    private Connection conexion;
    
    public UsuarioDAO() {
        this.conexion = ConexionDB.getConexion();
    }
    
    // Validar login
    public Usuario validarLogin(String nombreUsuario, String contrasena) {
        Usuario usuario = null;
        String sql = "SELECT * FROM usuario WHERE nombre_usuario = ? AND contrasena = ? AND activo = 1";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, contrasena);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setNombreUsuario(rs.getString("nombre_usuario"));
                    usuario.setContrasena(rs.getString("contrasena"));
                    usuario.setIdRol(rs.getInt("id_rol"));
                    usuario.setActivo(rs.getBoolean("activo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar login: " + e.getMessage());
        }
        
        return usuario;
    }
    
    // Crear usuario
    public boolean crearUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre_usuario, contrasena, id_rol, activo) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getContrasena());
            ps.setInt(3, usuario.getIdRol());
            ps.setBoolean(4, usuario.isActivo());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            return false;
        }
    }
    
    // Listar todos los usuarios
    public List<Usuario> listarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNombreUsuario(rs.getString("nombre_usuario"));
                usuario.setContrasena(rs.getString("contrasena"));
                usuario.setIdRol(rs.getInt("id_rol"));
                usuario.setActivo(rs.getBoolean("activo"));
                usuarios.add(usuario);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        
        return usuarios;
    }
    
    // Actualizar usuario
    public boolean actualizarUsuario(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre_usuario = ?, contrasena = ?, id_rol = ?, activo = ? WHERE id_usuario = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getContrasena());
            ps.setInt(3, usuario.getIdRol());
            ps.setBoolean(4, usuario.isActivo());
            ps.setInt(5, usuario.getIdUsuario());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }
    
    // Eliminar usuario (desactivar)
    public boolean eliminarUsuario(int idUsuario) {
        String sql = "UPDATE usuario SET activo = 0 WHERE id_usuario = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
}
