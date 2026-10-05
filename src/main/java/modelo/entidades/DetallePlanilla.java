// modelo/entidades/DetallePlanilla.java
package modelo.entidades;

public class DetallePlanilla {

    private int idDetalle;
    private int idPlanilla;
    private int idPersonal;
    private int diasTrabajados;
    private double horasExtras;
    private double montoHorasExtras;
    private int diasAusencia;
    private double descuentoAusencias;
    private int minutosTardanza;
    private double descuentoTardanzas;
    private double totalIngresos;
    private double totalDescuentos;
    private double sueldoNeto;
    private String dni;
    private String nombreCompleto;
    private String cargo;

    // Constructor vacío
    public DetallePlanilla() {
    }

    // Constructor completo
    public DetallePlanilla(int idDetalle, int idPlanilla, int idPersonal,
            int diasTrabajados, double horasExtras, double montoHorasExtras,
            int diasAusencia, double descuentoAusencias, int minutosTardanza,
            double descuentoTardanzas, double totalIngresos,
            double totalDescuentos, double sueldoNeto) {
        this.idDetalle = idDetalle;
        this.idPlanilla = idPlanilla;
        this.idPersonal = idPersonal;
        this.diasTrabajados = diasTrabajados;
        this.horasExtras = horasExtras;
        this.montoHorasExtras = montoHorasExtras;
        this.diasAusencia = diasAusencia;
        this.descuentoAusencias = descuentoAusencias;
        this.minutosTardanza = minutosTardanza;
        this.descuentoTardanzas = descuentoTardanzas;
        this.totalIngresos = totalIngresos;
        this.totalDescuentos = totalDescuentos;
        this.sueldoNeto = sueldoNeto;
    }

    // Getters y Setters completos
    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdPlanilla() {
        return idPlanilla;
    }

    public void setIdPlanilla(int idPlanilla) {
        this.idPlanilla = idPlanilla;
    }

    public int getIdPersonal() {
        return idPersonal;
    }

    public void setIdPersonal(int idPersonal) {
        this.idPersonal = idPersonal;
    }

    public int getDiasTrabajados() {
        return diasTrabajados;
    }

    public void setDiasTrabajados(int diasTrabajados) {
        this.diasTrabajados = diasTrabajados;
    }

    public double getHorasExtras() {
        return horasExtras;
    }

    public void setHorasExtras(double horasExtras) {
        this.horasExtras = horasExtras;
    }

    public double getMontoHorasExtras() {
        return montoHorasExtras;
    }

    public void setMontoHorasExtras(double montoHorasExtras) {
        this.montoHorasExtras = montoHorasExtras;
    }

    public int getDiasAusencia() {
        return diasAusencia;
    }

    public void setDiasAusencia(int diasAusencia) {
        this.diasAusencia = diasAusencia;
    }

    public double getDescuentoAusencias() {
        return descuentoAusencias;
    }

    public void setDescuentoAusencias(double descuentoAusencias) {
        this.descuentoAusencias = descuentoAusencias;
    }

    public int getMinutosTardanza() {
        return minutosTardanza;
    }

    public void setMinutosTardanza(int minutosTardanza) {
        this.minutosTardanza = minutosTardanza;
    }

    public double getDescuentoTardanzas() {
        return descuentoTardanzas;
    }

    public void setDescuentoTardanzas(double descuentoTardanzas) {
        this.descuentoTardanzas = descuentoTardanzas;
    }

    public double getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(double totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public double getTotalDescuentos() {
        return totalDescuentos;
    }

    public void setTotalDescuentos(double totalDescuentos) {
        this.totalDescuentos = totalDescuentos;
    }

    public double getSueldoNeto() {
        return sueldoNeto;
    }

    public void setSueldoNeto(double sueldoNeto) {
        this.sueldoNeto = sueldoNeto;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}
