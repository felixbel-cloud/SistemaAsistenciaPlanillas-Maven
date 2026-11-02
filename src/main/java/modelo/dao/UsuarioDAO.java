// modelo/dao/UsuarioDAO.java
package modelo.dao;

import modelo.conexion.ConexionDB;
import modelo.entidades.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    
    public UsuarioDAO() {
    }
    
    // Validar login: busca al usuario por nombre y compara la contraseña con su hash.
    // Si la contraseña guardada todavía está en texto plano (bases antiguas), se acepta
    // una única vez y se reemplaza por su hash automáticamente.
    public Usuario validarLogin(String nombreUsuario, String contrasena) {
        Usuario usuario = null;
        String sql = "SELECT * FROM usuario WHERE nombre_usuario = ? AND activo = 1";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String guardada = rs.getString("contrasena");
                    if (!util.Seguridad.verificar(contrasena, guardada)) {
                        return null;
                    }
                    usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setNombreUsuario(rs.getString("nombre_usuario"));
                    usuario.setContrasena(guardada);
                    usuario.setIdRol(rs.getInt("id_rol"));
                    usuario.setActivo(rs.getBoolean("activo"));
                    if (!util.Seguridad.esHash(guardada)) {
                        actualizarHash(usuario.getIdUsuario(), contrasena);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar login: " + e.getMessage());
        }
        
        return usuario;
    }

    // Reemplaza la contraseña en texto plano por su hash
    private void actualizarHash(int idUsuario, String contrasena) {
        String sql = "UPDATE usuario SET contrasena = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setString(1, util.Seguridad.hashContrasena(contrasena));
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al proteger la contraseña: " + e.getMessage());
        }
    }

    // Nunca se guarda una contraseña en texto plano
    private static String protegida(String contrasena) {
        return util.Seguridad.esHash(contrasena) ? contrasena : util.Seguridad.hashContrasena(contrasena);
    }
    
    // Crear usuario
    public boolean crearUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre_usuario, contrasena, id_rol, activo) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, protegida(usuario.getContrasena()));
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
        
        try (Statement stmt = ConexionDB.obtener().createStatement();
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
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, protegida(usuario.getContrasena()));
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
        
        try (PreparedStatement ps = ConexionDB.obtener().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
}
