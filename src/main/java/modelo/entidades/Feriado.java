// modelo/entidades/Feriado.java
package modelo.entidades;

import java.time.LocalDate;

/** Día feriado o no laborable: no se marcan faltas ni se descuenta ese día. */
public class Feriado {
    private LocalDate fecha;
    private String descripcion;

    public Feriado() {
    }

    public Feriado(LocalDate fecha, String descripcion) {
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
