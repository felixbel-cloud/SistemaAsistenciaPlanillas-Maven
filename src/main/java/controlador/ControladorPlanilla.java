// controlador/ControladorPlanilla.java
package controlador;

import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import modelo.conexion.ConexionDB;
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
            if (!validarPeriodo(periodo)) {
                JOptionPane.showMessageDialog(null,
                        "El periodo " + periodo + " no es válido.\n"
                        + "Use el formato AAAA-MM, desde 2020-01 hasta el mes actual.",
                        "Periodo inválido",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Verificar si ya existe planilla para el periodo
            if (planillaDAO.existePlanillaPeriodo(periodo)) {
                JOptionPane.showMessageDialog(null,
                        "Ya existe una planilla generada para el periodo: " + periodo,
                        "Planilla existente",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Registros de prueba (modo simulación) dentro del periodo
            int simulados = asistenciaDAO.contarSimuladosEnPeriodo(periodo, ControladorAsistencia.PREFIJO_SIMULADO);
            if (simulados > 0) {
                Object[] opciones = {"Cancelar", "Generar de todos modos (demostración)"};
                int r = JOptionPane.showOptionDialog(null,
                        "El periodo " + periodo + " tiene " + simulados + " registro(s) de asistencia SIMULADOS.\n"
                        + "Si genera la planilla, se pagarán como si fueran reales.\n\n"
                        + "Para una planilla real, primero bórrelos en Registrar Asistencia > \"Borrar registros simulados\".",
                        "Hay registros simulados",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, opciones, opciones[0]);
                if (r != 1) {
                    return false;
                }
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

            // La planilla y todos sus detalles se guardan en UNA transacción:
            // si algo falla a la mitad, no queda una planilla incompleta.
            double totalGeneral = 0.0;
            int empleados = 0;
            try {
                ConexionDB.iniciarTransaccion();

                int idPlanilla = planillaDAO.crearPlanilla(planilla);
                if (idPlanilla == -1) {
                    throw new java.sql.SQLException("No se pudo crear el registro de la planilla.");
                }

                for (Personal personal : listaPersonal) {
                    if (!trabajaEnPeriodo(personal, periodo)) {
                        continue; // contratado después del periodo
                    }
                    DetallePlanilla detalle = calcularDetallePlanilla(idPlanilla, personal, periodo);
                    if (!planillaDAO.crearDetallePlanilla(detalle)) {
                        throw new java.sql.SQLException("No se pudo guardar el detalle de " + personal.getNombreCompleto() + ".");
                    }
                    totalGeneral += detalle.getSueldoNeto();
                    empleados++;
                }

                if (!planillaDAO.actualizarTotalPlanilla(idPlanilla, totalGeneral)) {
                    throw new java.sql.SQLException("No se pudo actualizar el total de la planilla.");
                }

                ConexionDB.confirmar();
            } catch (java.sql.SQLException | RuntimeException e) {
                ConexionDB.revertir();
                JOptionPane.showMessageDialog(null,
                        "No se generó la planilla del periodo " + periodo + ".\n"
                        + "Motivo: " + e.getMessage() + "\n\n"
                        + "No se guardó ningún dato; puede volver a intentarlo.",
                        "Error al generar planilla",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            JOptionPane.showMessageDialog(null,
                    "Planilla generada exitosamente\n"
                    + "Periodo: " + periodo + "\n"
                    + "Personal incluido: " + empleados + "\n"
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

    // El personal contratado DESPUÉS del último día del periodo no entra en esa planilla
    private static boolean trabajaEnPeriodo(Personal personal, String periodo) {
        if (personal.getFechaContratacion() == null) {
            return true;
        }
        java.time.YearMonth ym = java.time.YearMonth.parse(periodo);
        java.time.LocalDate contratacion = new java.sql.Date(personal.getFechaContratacion().getTime()).toLocalDate();
        return !contratacion.isAfter(ym.atEndOfMonth());
    }

    // Días (sobre 30) que le corresponden al administrativo en el periodo según su fecha de contratación.
    // Contratado antes del mes: 30. Contratado el día 16: 15 días (30 - 16 + 1). Después del mes: 0.
    static int diasComputablesPorContratacion(Date fechaContratacion, String periodo) {
        if (fechaContratacion == null || periodo == null) {
            return DIAS_MES;
        }
        java.time.YearMonth ym = java.time.YearMonth.parse(periodo);
        java.time.LocalDate c = new java.sql.Date(fechaContratacion.getTime()).toLocalDate();
        if (c.isBefore(ym.atDay(1))) {
            return DIAS_MES;
        }
        if (c.isAfter(ym.atEndOfMonth())) {
            return 0;
        }
        return Math.max(0, Math.min(DIAS_MES, DIAS_MES - c.getDayOfMonth() + 1));
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

    // Obtiene los días laborables por semana según el horario del personal (por defecto 6)
    public static int obtenerDiasJornadaSemanal(Personal personal) {
        if (personal == null || personal.getIdPersonal() <= 0) {
            return 6;
        }
        try {
            modelo.dao.HorarioPersonalDAO dao = new modelo.dao.HorarioPersonalDAO();
            List<modelo.entidades.HorarioPersonal> horarios = dao.listarPorPersonal(personal.getIdPersonal());
            if (horarios != null && !horarios.isEmpty()) {
                java.util.Set<String> dias = new java.util.HashSet<>();
                for (modelo.entidades.HorarioPersonal h : horarios) {
                    if (h != null && h.isActivo() && h.getDiaSemana() != null) {
                        dias.add(h.getDiaSemana().trim().toUpperCase());
                    }
                }
                if (!dias.isEmpty()) {
                    return Math.max(1, Math.min(6, dias.size()));
                }
            }
        } catch (Exception e) {
            // Silencioso: en entornos de prueba o sin conexión se usa el régimen general
        }
        return 6;
    }

    // Calcular detalle de planilla para un personal
    private DetallePlanilla calcularDetallePlanilla(int idPlanilla, Personal personal, String periodo) {
        List<Asistencia> asistencias = asistenciaDAO.listarPorPersonalYMes(
                personal.getIdPersonal(), periodo);

        DetallePlanilla detalle = calcularDetallePorTipo(personal, asistencias, periodo);
        detalle.setIdPlanilla(idPlanilla);
        detalle.setIdPersonal(personal.getIdPersonal());
        return detalle;
    }

    static DetallePlanilla calcularDetallePorTipo(Personal personal, List<Asistencia> asistencias) {
        return calcularDetallePorTipo(personal, asistencias, null);
    }

    static DetallePlanilla calcularDetallePorTipo(Personal personal, List<Asistencia> asistencias, String periodo) {
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

        double salarioBase = Math.max(0.0, personal.getSalarioBase());
        double salarioDiario = salarioBase / DIAS_MES;
        double salarioPorHora = salarioBase / DIAS_MES / HORAS_DIA;
        double horasExtras = totalMinutosExtras / 60.0;
        double montoHorasExtras = horasExtras * salarioPorHora * TARIFA_HORA_EXTRA;
        double descuentoTardanzas = (totalMinutosTardanza / 60.0) * salarioPorHora;
        int diasComputables = diasComputablesPorContratacion(personal.getFechaContratacion(), periodo);
        double sueldoDelPeriodo = salarioBase * diasComputables / DIAS_MES;

        int diasJornada = obtenerDiasJornadaSemanal(personal);
        double factorDsr = 1.0 / diasJornada;
        double costoDiaConDsr = salarioDiario * (1.0 + factorDsr);

        double descuentoAusencias;
        if (diasAusencia == 0) {
            descuentoAusencias = 0.0;
        } else {
            double descuentoFaltas = diasAusencia * costoDiaConDsr;
            double sueldoTrasFaltas = Math.max(0.0, sueldoDelPeriodo - descuentoFaltas);

            double maximoGanadoPorTrabajo = diasTrabajados * costoDiaConDsr;

            double sueldoBaseGanado;
            if (diasTrabajados == 0) {
                sueldoBaseGanado = 0.0;
            } else if (sueldoTrasFaltas > maximoGanadoPorTrabajo && diasAusencia >= (diasJornada * 2)) {
                sueldoBaseGanado = maximoGanadoPorTrabajo;
            } else {
                sueldoBaseGanado = sueldoTrasFaltas;
            }

            descuentoAusencias = Math.max(0.0, sueldoDelPeriodo - sueldoBaseGanado);
        }

        double totalIngresos = sueldoDelPeriodo + montoHorasExtras;
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

    // Verificar si existe planilla para el periodo
    public boolean existePlanilla(String periodo) {
        return planillaDAO.existePlanillaPeriodo(periodo);
    }

    // Obtener planilla por periodo
    public Planilla obtenerPlanillaPorPeriodo(String periodo) {
        return planillaDAO.obtenerPlanillaPorPeriodo(periodo);
    }

    // Eliminar planilla por ID
    public boolean eliminarPlanilla(int idPlanilla) {
        return planillaDAO.eliminarPlanilla(idPlanilla);
    }

    // Regenerar planilla (elimina la anterior no pagada y recalcula)
    public boolean regenerarPlanilla(String periodo, int idUsuario) {
        Planilla existente = planillaDAO.obtenerPlanillaPorPeriodo(periodo);
        if (existente != null) {
            if ("PAGADA".equalsIgnoreCase(existente.getEstado())) {
                JOptionPane.showMessageDialog(null,
                        "No se puede regenerar la planilla del periodo " + periodo + " porque ya está en estado PAGADA.",
                        "Planilla Pagada",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (!planillaDAO.eliminarPlanilla(existente.getIdPlanilla())) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo eliminar la planilla previa para regenerar.",
                        "Error al regenerar",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return generarPlanilla(periodo, idUsuario);
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

            if (anio < 2020 || mes < 1 || mes > 12) {
                return false;
            }
            // No se puede generar la planilla de un mes que todavía no empieza
            return !java.time.YearMonth.of(anio, mes).isAfter(java.time.YearMonth.now());
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
