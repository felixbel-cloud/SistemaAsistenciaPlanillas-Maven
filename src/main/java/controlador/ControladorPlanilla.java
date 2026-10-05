// controlador/ControladorPlanilla.java
package controlador;

import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import modelo.dao.AsistenciaDAO;
import modelo.dao.PersonalDAO;
import modelo.dao.PlanillaDAO;
import modelo.entidades.Asistencia;
import modelo.entidades.DetallePlanilla;
import modelo.entidades.Personal;
import modelo.entidades.Planilla;

public class ControladorPlanilla {

    private PlanillaDAO planillaDAO;
    private PersonalDAO personalDAO;
    private AsistenciaDAO asistenciaDAO;

    private static final double TARIFA_HORA_EXTRA = 1.25; // 25% adicional
    private static final int DIAS_MES = 30;
    private static final int HORAS_DIA = 8;

    public ControladorPlanilla() {
        this.planillaDAO = new PlanillaDAO();
        this.personalDAO = new PersonalDAO();
        this.asistenciaDAO = new AsistenciaDAO();
    }

    // Generar planilla del mes
    public boolean generarPlanilla(String periodo, int idUsuario) {
        try {
            // Verificar si ya existe planilla para el periodo
            if (planillaDAO.existePlanillaPeriodo(periodo)) {
                JOptionPane.showMessageDialog(null,
                        "Ya existe una planilla generada para el periodo: " + periodo,
                        "Planilla existente",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Obtener todo el personal activo
            List<Personal> listaPersonal = personalDAO.listarPersonal();

            if (listaPersonal.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "No hay personal registrado",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Crear planilla principal
            Planilla planilla = new Planilla();
            planilla.setPeriodo(periodo);
            planilla.setFechaGeneracion(new Date());
            planilla.setTotalPlanilla(0.0);
            planilla.setEstado("GENERADA");
            planilla.setGeneradoPor(idUsuario);

            // Antes de calcular la planilla, se detectan e insertan automáticamente
            // las faltas de los bloques de horario del periodo que no tuvieron marcación
            marcarFaltasDelPeriodo(periodo);

            int idPlanilla = planillaDAO.crearPlanilla(planilla);

            if (idPlanilla == -1) {
                JOptionPane.showMessageDialog(null,
                        "Error al crear la planilla",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            double totalGeneral = 0.0;

            // Generar detalle para cada personal
            for (Personal personal : listaPersonal) {
                DetallePlanilla detalle = calcularDetallePlanilla(
                        idPlanilla, personal, periodo);

                if (planillaDAO.crearDetallePlanilla(detalle)) {
                    totalGeneral += detalle.getSueldoNeto();
                } else {
                    System.err.println("Error al crear detalle para personal: "
                            + personal.getNombreCompleto());
                }
            }

            // Actualizar total de la planilla
            planilla.setIdPlanilla(idPlanilla);
            planilla.setTotalPlanilla(totalGeneral);
            planillaDAO.actualizarTotalPlanilla(idPlanilla, totalGeneral);

            JOptionPane.showMessageDialog(null,
                    "Planilla generada exitosamente\n"
                    + "Periodo: " + periodo + "\n"
                    + "Total: S/ " + String.format("%.2f", totalGeneral),
                    "Generación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al generar planilla: " + e.getMessage(),
                    "Error del sistema",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Marca automáticamente como AUSENTE los bloques de horario vencidos del periodo
    // (YYYY-MM) que no tengan ningún registro de asistencia
    private void marcarFaltasDelPeriodo(String periodo) {
        try {
            String[] partes = periodo.split("-");
            int anio = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(anio, mes - 1, 1, 0, 0, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            Date fechaInicio = cal.getTime();

            cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
            Date fechaFin = cal.getTime();

            ControladorAsistencia controladorAsistencia = new ControladorAsistencia();
            controladorAsistencia.generarFaltasAutomaticas(fechaInicio, fechaFin);
        } catch (Exception e) {
            System.err.println("Error al detectar faltas del periodo: " + e.getMessage());
        }
    }

    // Calcular detalle de planilla para un personal
    private DetallePlanilla calcularDetallePlanilla(int idPlanilla, Personal personal, String periodo) {
        List<Asistencia> asistencias = asistenciaDAO.listarPorPersonalYMes(
                personal.getIdPersonal(), periodo);

        DetallePlanilla detalle = calcularDetallePorTipo(personal, asistencias);
        detalle.setIdPlanilla(idPlanilla);
        detalle.setIdPersonal(personal.getIdPersonal());
        return detalle;
    }

    static DetallePlanilla calcularDetallePorTipo(Personal personal, List<Asistencia> asistencias) {
        DetallePlanilla detalle = new DetallePlanilla();
        if (personal == null) {
            return detalle;
        }

        detalle.setIdPersonal(personal.getIdPersonal());

        java.util.Set<String> diasConAsistencia = new java.util.HashSet<>();
        java.util.Set<String> diasConFalta = new java.util.HashSet<>();
        int totalMinutosTardanza = 0;
        int totalMinutosExtras = 0;
        double totalHorasTrabajadas = 0.0;

        for (Asistencia asistencia : asistencias) {
            if (asistencia == null || asistencia.getFecha() == null) {
                continue;
            }

            String claveFecha = asistencia.getFecha().toString();

            if ("PRESENTE".equals(asistencia.getEstado())
                    || "TARDANZA".equals(asistencia.getEstado())) {
                diasConAsistencia.add(claveFecha);
            } else if ("AUSENTE".equals(asistencia.getEstado())) {
                diasConFalta.add(claveFecha);
            }

            totalMinutosTardanza += asistencia.getMinutosTardanza();
            totalMinutosExtras += asistencia.getMinutosExtras();
            totalHorasTrabajadas += asistencia.getHorasTrabajadas();
        }

        diasConFalta.removeAll(diasConAsistencia);
        int diasTrabajados = diasConAsistencia.size();
        int diasAusencia = diasConFalta.size();

        detalle.setDiasTrabajados(diasTrabajados);
        detalle.setDiasAusencia(diasAusencia);
        detalle.setMinutosTardanza(totalMinutosTardanza);

        String tipoPersonal = personal.getTipoPersonal() == null ? "" : personal.getTipoPersonal().trim().toUpperCase();

        if ("DOCENTE".equals(tipoPersonal)) {
            double salarioPorHora = Math.max(0.0, personal.getSalarioBase());
            double horasExtras = totalMinutosExtras / 60.0;
            double montoHorasExtras = horasExtras * salarioPorHora * TARIFA_HORA_EXTRA;
            double descuentoAusencias = diasAusencia * salarioPorHora * HORAS_DIA;
            double descuentoTardanzas = (totalMinutosTardanza / 60.0) * salarioPorHora;
            double totalIngresos = (totalHorasTrabajadas > 0 ? totalHorasTrabajadas * salarioPorHora : 0.0) + montoHorasExtras;
            double totalDescuentos = descuentoAusencias + descuentoTardanzas;
            double sueldoNeto = totalIngresos - totalDescuentos;

            detalle.setHorasExtras(horasExtras);
            detalle.setMontoHorasExtras(montoHorasExtras);
            detalle.setDescuentoAusencias(descuentoAusencias);
            detalle.setDescuentoTardanzas(descuentoTardanzas);
            detalle.setTotalIngresos(totalIngresos);
            detalle.setTotalDescuentos(totalDescuentos);
            detalle.setSueldoNeto(Math.max(0, sueldoNeto));
            return detalle;
        }

        double salarioBase = Math.max(0.0, personal.getSalarioBase());
        double salarioDiario = salarioBase / DIAS_MES;
        double salarioPorHora = salarioBase / DIAS_MES / HORAS_DIA;
        double horasExtras = totalMinutosExtras / 60.0;
        double montoHorasExtras = horasExtras * salarioPorHora * TARIFA_HORA_EXTRA;
        double descuentoAusencias = diasAusencia * salarioDiario;
        double descuentoTardanzas = (totalMinutosTardanza / 60.0) * salarioPorHora;
        double totalIngresos = salarioBase + montoHorasExtras;
        double totalDescuentos = descuentoAusencias + descuentoTardanzas;
        double sueldoNeto = totalIngresos - totalDescuentos;

        detalle.setHorasExtras(horasExtras);
        detalle.setMontoHorasExtras(montoHorasExtras);
        detalle.setDescuentoAusencias(descuentoAusencias);
        detalle.setDescuentoTardanzas(descuentoTardanzas);
        detalle.setTotalIngresos(totalIngresos);
        detalle.setTotalDescuentos(totalDescuentos);
        detalle.setSueldoNeto(Math.max(0, sueldoNeto));
        return detalle;
    }

    // Cargar planillas en tabla
    public void cargarPlanillas(JTable tabla) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);

        List<Planilla> planillas = planillaDAO.listarPlanillas();

        for (Planilla planilla : planillas) {
            Object[] fila = {
                planilla.getIdPlanilla(),
                planilla.getPeriodo(),
                planilla.getFechaGeneracion(),
                String.format("S/ %.2f", planilla.getTotalPlanilla()),
                planilla.getEstado()
            };
            modelo.addRow(fila);
        }
    }

    // Cargar detalle de planilla en tabla
    public void cargarDetallePlanilla(JTable tabla, int idPlanilla) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);

        List<DetallePlanilla> detalles = planillaDAO.obtenerDetallesPlanilla(idPlanilla);

        for (DetallePlanilla detalle : detalles) {
            Object[] fila = {
                detalle.getDni(),
                detalle.getNombreCompleto(),
                detalle.getCargo(),
                detalle.getDiasTrabajados(),
                detalle.getHorasExtras(),
                String.format("S/ %.2f", detalle.getMontoHorasExtras()),
                detalle.getDiasAusencia(),
                String.format("S/ %.2f", detalle.getDescuentoAusencias()),
                detalle.getMinutosTardanza(),
                String.format("S/ %.2f", detalle.getDescuentoTardanzas()),
                String.format("S/ %.2f", detalle.getTotalIngresos()),
                String.format("S/ %.2f", detalle.getTotalDescuentos()),
                String.format("S/ %.2f", detalle.getSueldoNeto())
            };
            modelo.addRow(fila);
        }
    }

    // Cambiar estado de planilla
    public boolean cambiarEstadoPlanilla(int idPlanilla, String nuevoEstado) {
        boolean resultado = planillaDAO.actualizarEstadoPlanilla(idPlanilla, nuevoEstado);

        if (resultado) {
            JOptionPane.showMessageDialog(null,
                    "Estado actualizado a: " + nuevoEstado,
                    "Actualización exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Error al actualizar el estado",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return resultado;
    }

    // Obtener planilla por ID
    public Planilla obtenerPlanilla(int idPlanilla) {
        return planillaDAO.obtenerPlanillaPorId(idPlanilla);
    }

    // Validar periodo (formato YYYY-MM)
    public boolean validarPeriodo(String periodo) {
        if (periodo == null || periodo.length() != 7) {
            return false;
        }

        String[] partes = periodo.split("-");
        if (partes.length != 2) {
            return false;
        }

        try {
            int anio = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);

            return anio >= 2020 && anio <= 2030 && mes >= 1 && mes <= 12;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
