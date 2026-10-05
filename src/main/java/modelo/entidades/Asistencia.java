// modelo/entidades/Asistencia.java
package modelo.entidades;

import java.sql.Time;
import java.util.Date;

public class Asistencia {
    private int idAsistencia;
    private int idPersonal;
    private Integer idHorario; // Bloque de horario_personal al que corresponde (puede ser null)
    private Date fecha;
    private Time horaEntrada;
    private Time horaSalida;
    private double horasTrabajadas;
    private int minutosExtras;
    private int minutosTardanza;
    private String estado; // PRESENTE, AUSENTE, TARDANZA
    private String observaciones;

    // Campos auxiliares (no se persisten en esta tabla, se llenan por JOIN)
    private String descripcionBloque;
    private String horarioProgramadoTexto;
    
    // Constructor vacío
    public Asistencia() {}
    
    // Constructor completo
    public Asistencia(int idAsistencia, int idPersonal, Date fecha, Time horaEntrada, 
                     Time horaSalida, int minutosExtras, int minutosTardanza, 
                     String estado, String observaciones) {
        this.idAsistencia = idAsistencia;
        this.idPersonal = idPersonal;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.minutosExtras = minutosExtras;
        this.minutosTardanza = minutosTardanza;
        this.estado = estado;
        this.observaciones = observaciones;
    }
    
    // Getters y Setters
    public int getIdAsistencia() {
        return idAsistencia;
    }
    
    public void setIdAsistencia(int idAsistencia) {
        this.idAsistencia = idAsistencia;
    }
    
    public int getIdPersonal() {
        return idPersonal;
    }
    
    public void setIdPersonal(int idPersonal) {
        this.idPersonal = idPersonal;
    }
    
    public Date getFecha() {
        return fecha;
    }
    
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    
    public Time getHoraEntrada() {
        return horaEntrada;
    }
    
    public void setHoraEntrada(Time horaEntrada) {
        this.horaEntrada = horaEntrada;
    }
    
    public Time getHoraSalida() {
        return horaSalida;
    }
    
    public void setHoraSalida(Time horaSalida) {
        this.horaSalida = horaSalida;
    }
    
    public int getMinutosExtras() {
        return minutosExtras;
    }
    
    public void setMinutosExtras(int minutosExtras) {
        this.minutosExtras = minutosExtras;
    }
    
    public int getMinutosTardanza() {
        return minutosTardanza;
    }
    
    public void setMinutosTardanza(int minutosTardanza) {
        this.minutosTardanza = minutosTardanza;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(Integer idHorario) {
        this.idHorario = idHorario;
    }

    public double getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public void setHorasTrabajadas(double horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }

    public String getDescripcionBloque() {
        return descripcionBloque;
    }

    public void setDescripcionBloque(String descripcionBloque) {
        this.descripcionBloque = descripcionBloque;
    }

    public String getHorarioProgramadoTexto() {
        return horarioProgramadoTexto;
    }

    public void setHorarioProgramadoTexto(String horarioProgramadoTexto) {
        this.horarioProgramadoTexto = horarioProgramadoTexto;
    }
}