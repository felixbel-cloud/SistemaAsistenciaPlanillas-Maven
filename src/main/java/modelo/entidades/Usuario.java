// modelo/entidades/Usuario.java
package modelo.entidades;

public class Usuario {
    private int idUsuario;
    private String nombreUsuario;
    private String contrasena;
    private int idRol;
    private boolean activo;
    
    // Constructor vacío
    public Usuario() {}
    
    // Constructor con parámetros
    public Usuario(int idUsuario, String nombreUsuario, String contrasena, int idRol, boolean activo) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.idRol = idRol;
        this.activo = activo;
    }
    
    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    
    public String getContrasena() {
        return contrasena;
    }
    
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    
    public int getIdRol() {
        return idRol;
    }
    
    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}