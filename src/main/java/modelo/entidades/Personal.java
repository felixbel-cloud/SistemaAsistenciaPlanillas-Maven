// modelo/entidades/Personal.java
package modelo.entidades;

import java.util.Date;

public class Personal {
    private int idPersonal;
    private String dni;
    private String nombres;
    private String apellidos;
    private String tipoPersonal; // ADMINISTRATIVO o DOCENTE
    private String cargo;
    private Date fechaContratacion;
    private double salarioBase;
    private String horarioEntrada;
    private String horarioSalida;
    private boolean activo;
    
    // Constructor vacío
    public Personal() {}
    
    // Constructor completo
    public Personal(int idPersonal, String dni, String nombres, String apellidos, 
                   String tipoPersonal, String cargo, Date fechaContratacion, 
                   double salarioBase, String horarioEntrada, String horarioSalida, 
                   boolean activo) {
        this.idPersonal = idPersonal;
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.tipoPersonal = tipoPersonal;
        this.cargo = cargo;
        this.fechaContratacion = fechaContratacion;
        this.salarioBase = salarioBase;
        this.horarioEntrada = horarioEntrada;
        this.horarioSalida = horarioSalida;
        this.activo = activo;
    }
    
    // Getters y Setters
    public int getIdPersonal() {
        return idPersonal;
    }
    
    public void setIdPersonal(int idPersonal) {
        this.idPersonal = idPersonal;
    }
    
    public String getDni() {
        return dni;
    }
    
    public void setDni(String dni) {
        this.dni = dni;
    }
    
    public String getNombres() {
        return nombres;
    }
    
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }
    
    public String getApellidos() {
        return apellidos;
    }
    
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    
    public String getTipoPersonal() {
        return tipoPersonal;
    }
    
    public void setTipoPersonal(String tipoPersonal) {
        this.tipoPersonal = tipoPersonal;
    }
    
    public String getCargo() {
        return cargo;
    }
    
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
    
    public Date getFechaContratacion() {
        return fechaContratacion;
    }
    
    public void setFechaContratacion(Date fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }
    
    public double getSalarioBase() {
        return salarioBase;
    }
    
    public void setSalarioBase(double salarioBase) {
        this.salarioBase = salarioBase;
    }
    
    public String getHorarioEntrada() {
        return horarioEntrada;
    }
    
    public void setHorarioEntrada(String horarioEntrada) {
        this.horarioEntrada = horarioEntrada;
    }
    
    public String getHorarioSalida() {
        return horarioSalida;
    }
    
    public void setHorarioSalida(String horarioSalida) {
        this.horarioSalida = horarioSalida;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    // Método para obtener nombre completo
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}