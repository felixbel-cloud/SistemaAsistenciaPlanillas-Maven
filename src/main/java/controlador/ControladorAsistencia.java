// controlador/ControladorAsistencia.java
package controlador;

import java.sql.Time;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import modelo.dao.AsistenciaDAO;
import modelo.dao.HorarioPersonalDAO;
import modelo.dao.PersonalDAO;
import modelo.entidades.Asistencia;
import modelo.entidades.HorarioPersonal;
import modelo.entidades.Personal;

public class ControladorAsistencia {

    // Minutos de tolerancia antes de considerar tardanza
    public static final int TOLERANCIA_MINUTOS = 5;

    private AsistenciaDAO asistenciaDAO;
    private PersonalDAO personalDAO;
    private HorarioPersonalDAO horarioPersonalDAO;

    public ControladorAsistencia() {
        this.asistenciaDAO = new AsistenciaDAO();
        this.personalDAO = new PersonalDAO();
        this.horarioPersonalDAO = new HorarioPersonalDAO();
    }

    // Convierte el día de la semana de una fecha al formato usado en horario_personal (LUNES..DOMINGO)
    public static String obtenerDiaSemana(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        int dia = cal.get(Calendar.DAY_OF_WEEK);
        switch (dia) {
            case Calendar.MONDAY: return "LUNES";
            case Calendar.TUESDAY: return "MARTES";
            case Calendar.WEDNESDAY: return "MIERCOLES";
            case Calendar.THURSDAY: return "JUEVES";
            case Calendar.FRIDAY: return "VIERNES";
            case Calendar.SATURDAY: return "SABADO";
            default: return "DOMINGO";
        }
    }

    // Texto legible con TODOS los bloques de horario de un personal, agrupados por día
    // (Ej: "LUNES 08:00-12:00 / 17:00-20:00, MIERCOLES 08:00-12:00 / 17:00-20:00, SABADO 11:00-14:00")
    public String obtenerHorarioTexto(Personal personal) {
        List<HorarioPersonal> bloques = horarioPersonalDAO.listarPorPersonal(personal.getIdPersonal());

        if (bloques.isEmpty()) {
            // Sin horario detallado configurado: usar el horario simple (legado) del personal
            return personal.getHorarioEntrada() + " - " + personal.getHorarioSalida() + " (Lun-Vie, horario general)";
        }

        StringBuilder sb = new StringBuilder();
        String diaActual = null;
        for (HorarioPersonal b : bloques) {
            if (!b.getDiaSemana().equals(diaActual)) {
                if (diaActual != null) {
                    sb.append(", ");
                }
                sb.append(b.getDiaSemana()).append(" ");
                diaActual = b.getDiaSemana();
            } else {
                sb.append(" / ");
            }
            sb.append(formatoHora(b.getHoraInicio())).append("-").append(formatoHora(b.getHoraFin()));
        }
        return sb.toString();
    }

    private String formatoHora(Time hora) {
        return new java.text.SimpleDateFormat("HH:mm").format(hora);
    }

    // Busca, dentro de los bloques del día, el primero para el que aún no exista un registro de entrada
    private HorarioPersonal buscarSiguienteBloquePendiente(int idPersonal, Date fecha, List<HorarioPersonal> bloquesDelDia) {
        for (HorarioPersonal bloque : bloquesDelDia) {
            if (!asistenciaDAO.existeRegistroBloque(idPersonal, fecha, bloque.getIdHorario())) {
                return bloque;
            }
        }
        return null;
    }

    // Registrar entrada
    public boolean registrarEntrada(String dni) {
        try {
            // Buscar personal por DNI
            Personal personal = personalDAO.buscarPorDni(dni);

            if (personal == null) {
                JOptionPane.showMessageDialog(null,
                    "Personal no encontrado con DNI: " + dni,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

            Date fechaActual = new Date();
            Time horaActual = new Time(System.currentTimeMillis());
            String diaSemana = obtenerDiaSemana(fechaActual);

            List<HorarioPersonal> bloquesDelDia = horarioPersonalDAO.listarPorPersonalYDia(
                personal.getIdPersonal(), diaSemana);

            Asistencia asistencia = new Asistencia();
            asistencia.setIdPersonal(personal.getIdPersonal());
            asistencia.setFecha(fechaActual);
            asistencia.setHoraEntrada(horaActual);

            if (bloquesDelDia.isEmpty()) {
                // El personal no tiene un horario detallado configurado para hoy.
                // Se mantiene compatibilidad con el registro simple (sin bloque específico).
                if (asistenciaDAO.existeRegistroBloque(personal.getIdPersonal(), fechaActual, null)) {
                    JOptionPane.showMessageDialog(null,
                        "El personal ya registró su entrada el día de hoy",
                        "Registro duplicado",
                        JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                asistencia.setIdHorario(null);
                asistencia.setMinutosTardanza(0);
                asistencia.setEstado("PRESENTE");
                asistencia.setObservaciones("Entrada registrada (sin horario configurado para " + diaSemana + ")");
            } else {
                HorarioPersonal bloque = buscarSiguienteBloquePendiente(
                    personal.getIdPersonal(), fechaActual, bloquesDelDia);

                if (bloque == null) {
                    JOptionPane.showMessageDialog(null,
                        "El personal ya registró su entrada en todos los bloques de horario de hoy (" + diaSemana + ")",
                        "Registro duplicado",
                        JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                int minutosTardanza = calcularMinutosTardanza(bloque.getHoraInicio(), horaActual);

                asistencia.setIdHorario(bloque.getIdHorario());
                asistencia.setMinutosTardanza(minutosTardanza);
                asistencia.setEstado(minutosTardanza > 0 ? "TARDANZA" : "PRESENTE");
                String nombreBloque = (bloque.getDescripcionBloque() != null && !bloque.getDescripcionBloque().isBlank())
                        ? bloque.getDescripcionBloque() : bloque.getDiaSemana();
                asistencia.setObservaciones("Entrada registrada - Bloque: " + nombreBloque
                        + " (" + formatoHora(bloque.getHoraInicio()) + "-" + formatoHora(bloque.getHoraFin()) + ")");
            }

            boolean resultado = asistenciaDAO.registrarEntrada(asistencia);

            if (resultado) {
                String mensajeTardanza = asistencia.getMinutosTardanza() > 0
                        ? "\nTardanza: " + formatearTiempo(asistencia.getMinutosTardanza()) : "";
                JOptionPane.showMessageDialog(null,
                    "Entrada registrada exitosamente\n" +
                    "Personal: " + personal.getNombreCompleto() + "\n" +
                    "Hora: " + horaActual + mensajeTardanza,
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(null,
                    "Error al registrar la entrada",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error: " + e.getMessage(),
                "Error del sistema",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private int calcularMinutosTardanza(Time horaProgramada, Time horaReal) {
        long diferenciaMin = Duration.between(horaProgramada.toLocalTime(), horaReal.toLocalTime()).toMinutes();
        long tardanza = Math.max(0, diferenciaMin - TOLERANCIA_MINUTOS);
        return (int) Math.min(1440, tardanza);
    }

    private int calcularMinutosExtras(Time horaProgramadaFin, Time horaSalidaReal) {
        long diferenciaMin = Duration.between(horaProgramadaFin.toLocalTime(), horaSalidaReal.toLocalTime()).toMinutes();
        return (int) Math.max(0, Math.min(1440, diferenciaMin));
    }

    private double calcularHorasTrabajadas(Time horaEntrada, Time horaSalida) {
        long diferenciaMin = Duration.between(horaEntrada.toLocalTime(), horaSalida.toLocalTime()).toMinutes();
        double horas = diferenciaMin / 60.0;
        return Math.max(0.0, Math.min(9999.99, Math.round(horas * 100.0) / 100.0));
    }

    // Registrar salida
    public boolean registrarSalida(String dni) {
        try {
            // Buscar personal por DNI
            Personal personal = personalDAO.buscarPorDni(dni);

            if (personal == null) {
                JOptionPane.showMessageDialog(null,
                    "Personal no encontrado con DNI: " + dni,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

            Date fechaActual = new Date();

            // Primero intenta encontrar la asistencia abierta del día actual.
            // Si por algún motivo la fecha del sistema no coincide con la registrada,
            // usa la última asistencia abierta del personal como fallback robusto.
            Asistencia asistencia = asistenciaDAO.obtenerBloqueAbierto(personal.getIdPersonal(), fechaActual);
            if (asistencia == null) {
                asistencia = asistenciaDAO.obtenerUltimaAsistenciaAbierta(personal.getIdPersonal());
            }

            if (asistencia == null) {
                JOptionPane.showMessageDialog(null,
                    "No se encontró un registro de entrada pendiente de salida para hoy",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

            Time horaSalida = new Time(System.currentTimeMillis());
            int minutosExtras = 0;
            double horasTrabajadas = calcularHorasTrabajadas(asistencia.getHoraEntrada(), horaSalida);

            if (asistencia.getIdHorario() != null) {
                // Buscar el bloque de horario programado para calcular horas extra reales
                String diaSemana = obtenerDiaSemana(fechaActual);
                List<HorarioPersonal> bloquesDelDia = horarioPersonalDAO.listarPorPersonalYDia(
                        personal.getIdPersonal(), diaSemana);
                for (HorarioPersonal b : bloquesDelDia) {
                    if (b.getIdHorario() == asistencia.getIdHorario()) {
                        minutosExtras = calcularMinutosExtras(b.getHoraFin(), horaSalida);
                        break;
                    }
                }
            } else {
                // Compatibilidad: sin bloque específico, usar 9 horas (8h + 1h refrigerio) como jornada de referencia
                long minutosTrabajados = Duration.between(
                        asistencia.getHoraEntrada().toLocalTime(),
                        horaSalida.toLocalTime()).toMinutes();
                minutosExtras = (int) Math.max(0, Math.min(1440, minutosTrabajados - 540));
            }

            boolean resultado = asistenciaDAO.registrarSalida(
                asistencia.getIdAsistencia(), horaSalida, minutosExtras, horasTrabajadas);

            if (!resultado) {
                resultado = asistenciaDAO.registrarSalidaPorPersonalFecha(
                    personal.getIdPersonal(), fechaActual, horaSalida, minutosExtras, horasTrabajadas);
            }

            if (resultado) {
                JOptionPane.showMessageDialog(null,
                    "Salida registrada exitosamente\n" +
                    "Personal: " + personal.getNombreCompleto() + "\n" +
                    "Hora: " + horaSalida + "\n" +
                    "Horas trabajadas: " + String.format("%.2f", horasTrabajadas) + "\n" +
                    "Horas extras: " + formatearTiempo(minutosExtras),
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(null,
                    "Error al registrar la salida",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error: " + e.getMessage(),
                "Error del sistema",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Detecta e inserta faltas (AUSENTE) para todos los bloques de horario ya vencidos
    // dentro del rango de fechas indicado que no tengan ningún registro de asistencia.
    // Se debe ejecutar antes de generar reportes/planillas de un periodo ya cerrado.
    public int generarFaltasAutomaticas(Date fechaInicio, Date fechaFin) {
        int faltasRegistradas = 0;
        List<Personal> listaPersonal = personalDAO.listarPersonal();
        Date hoy = new Date();
        Date limite = fechaFin.after(hoy) ? hoy : fechaFin;

        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaInicio);

        while (!cal.getTime().after(limite)) {
            Date fecha = cal.getTime();
            String diaSemana = obtenerDiaSemana(fecha);

            for (Personal personal : listaPersonal) {
                List<HorarioPersonal> bloquesDelDia = horarioPersonalDAO.listarPorPersonalYDia(
                        personal.getIdPersonal(), diaSemana);

                for (HorarioPersonal bloque : bloquesDelDia) {
                    if (!asistenciaDAO.existeRegistroBloque(personal.getIdPersonal(), fecha, bloque.getIdHorario())) {
                        String nombreBloque = (bloque.getDescripcionBloque() != null && !bloque.getDescripcionBloque().isBlank())
                                ? bloque.getDescripcionBloque() : bloque.getDiaSemana();
                        boolean ok = asistenciaDAO.registrarFalta(personal.getIdPersonal(), bloque.getIdHorario(),
                                fecha, "Falta detectada automáticamente - Bloque: " + nombreBloque);
                        if (ok) {
                            faltasRegistradas++;
                        }
                    }
                }
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return faltasRegistradas;
    }
    
    // Cargar asistencias en tabla
    public void cargarAsistencias(JTable tabla, Date fechaInicio, Date fechaFin) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);
        
        List<Asistencia> asistencias = asistenciaDAO.listarPorRangoFechas(fechaInicio, fechaFin);
        
        for (Asistencia asistencia : asistencias) {
            Personal personal = personalDAO.buscarPorId(asistencia.getIdPersonal());
            
            Object[] fila = {
                asistencia.getIdAsistencia(),
                personal != null ? personal.getDni() : "N/A",
                personal != null ? personal.getNombreCompleto() : "N/A",
                asistencia.getFecha(),
                asistencia.getHoraEntrada() != null ? asistencia.getHoraEntrada() : "Sin registro",
                asistencia.getHoraSalida() != null ? asistencia.getHoraSalida() : "Sin registro",
                formatearTiempo(asistencia.getMinutosTardanza()),
                formatearTiempo(asistencia.getMinutosExtras()),
                asistencia.getEstado()
            };
            modelo.addRow(fila);
        }
    }
    
    // Formatear minutos a formato legible (ej: "3h 6 min" o "55 min")
    public static String formatearTiempo(int minutos) {
        if (minutos <= 0) {
            return "0 min";
        }
        int horas = minutos / 60;
        int resto = minutos % 60;
        if (horas == 0) {
            return resto + " min";
        }
        if (resto == 0) {
            return horas + "h";
        }
        return horas + "h " + resto + " min";
    }

    // Buscar personal por DNI
    public Personal buscarPersonal(String dni) {
        return personalDAO.buscarPorDni(dni);
    }
}
