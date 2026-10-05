// modelo/entidades/Planilla.java
package modelo.entidades;

import java.util.Date;

public class Planilla {
    private int idPlanilla;
    private String periodo; // Formato: YYYY-MM
    private Date fechaGeneracion;
    private double totalPlanilla;
    private String estado; // GENERADA, PAGADA, ANULADA
    private int generadoPor;
    
    // Constructor vacío
    public Planilla() {}
    
    // Constructor completo
    public Planilla(int idPlanilla, String periodo, Date fechaGeneracion, 
                   double totalPlanilla, String estado, int generadoPor) {
        this.idPlanilla = idPlanilla;
        this.periodo = periodo;
        this.fechaGeneracion = fechaGeneracion;
        this.totalPlanilla = totalPlanilla;
        this.estado = estado;
        this.generadoPor = generadoPor;
    }
    
    // Getters y Setters
    public int getIdPlanilla() {
        return idPlanilla;
    }
    
    public void setIdPlanilla(int idPlanilla) {
        this.idPlanilla = idPlanilla;
    }
    
    public String getPeriodo() {
        return periodo;
    }
    
    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }
    
    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }
    
    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }
    
    public double getTotalPlanilla() {
        return totalPlanilla;
    }
    
    public void setTotalPlanilla(double totalPlanilla) {
        this.totalPlanilla = totalPlanilla;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public int getGeneradoPor() {
        return generadoPor;
    }
    
    public void setGeneradoPor(int generadoPor) {
        this.generadoPor = generadoPor;
    }
}