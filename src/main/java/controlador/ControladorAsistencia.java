// controlador/ControladorAsistencia.java
package controlador;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

    // Minutos antes del inicio de un bloque desde los que se permite marcar la entrada
    public static final int VENTANA_ANTES_MINUTOS = 30;

    // Tiempo después del fin del bloque: si es menor a este mínimo NO cuenta como hora extra
    public static final int MINIMO_EXTRAS_MINUTOS = 15;

    // Máximo de minutos extra que se reconocen por registro (bloque) al día
    public static final int TOPE_EXTRAS_MINUTOS = 120;

    // Jornada de referencia para personal SIN horario detallado (8h + 1h refrigerio)
    public static final int JORNADA_SIN_HORARIO_MINUTOS = 540;

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

    // Bloques del día que todavía no tienen ningún registro (ni entrada ni falta)
    private List<HorarioPersonal> bloquesPendientes(int idPersonal, Date fecha, List<HorarioPersonal> bloquesDelDia) {
        List<HorarioPersonal> pendientes = new java.util.ArrayList<>();
        for (HorarioPersonal bloque : bloquesDelDia) {
            if (!asistenciaDAO.existeRegistroBloque(idPersonal, fecha, bloque.getIdHorario())) {
                pendientes.add(bloque);
            }
        }
        return pendientes;
    }

    private static int minutosDelDia(LocalTime hora) {
        return hora.getHour() * 60 + hora.getMinute();
    }

    private static int minutosDelDia(Time hora) {
        return minutosDelDia(hora.toLocalTime());
    }

    private static String hhmm(int minutosDelDia) {
        int m = Math.max(0, minutosDelDia);
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    // La entrada a un bloque se puede marcar desde VENTANA_ANTES_MINUTOS antes de su inicio
    // y hasta antes de su hora de fin.
    static boolean dentroDeVentanaEntrada(HorarioPersonal bloque, LocalTime ahora) {
        int desde = Math.max(0, minutosDelDia(bloque.getHoraInicio()) - VENTANA_ANTES_MINUTOS);
        int hasta = minutosDelDia(bloque.getHoraFin());
        int actual = minutosDelDia(ahora);
        return actual >= desde && actual < hasta;
    }

    // Devuelve el bloque pendiente cuya ventana de entrada contiene la hora actual,
    // o null si a esta hora no corresponde marcar entrada en ningún bloque.
    static HorarioPersonal elegirBloqueParaEntrada(List<HorarioPersonal> pendientes, LocalTime ahora) {
        if (pendientes == null) {
            return null;
        }
        for (HorarioPersonal bloque : pendientes) {
            if (dentroDeVentanaEntrada(bloque, ahora)) {
                return bloque;
            }
        }
        return null;
    }

    // Mensaje que explica por qué no se puede marcar la entrada a esta hora
    static String motivoEntradaFueraDeHorario(List<HorarioPersonal> pendientes, LocalTime ahora) {
        int actual = minutosDelDia(ahora);
        for (HorarioPersonal bloque : pendientes) {
            int inicio = minutosDelDia(bloque.getHoraInicio());
            int desde = Math.max(0, inicio - VENTANA_ANTES_MINUTOS);
            if (actual < desde) {
                return "Todavía no puede marcar su entrada.\n"
                        + "Su horario de hoy empieza a las " + hhmm(inicio)
                        + " (bloque " + hhmm(inicio) + "-" + hhmm(minutosDelDia(bloque.getHoraFin())) + ").\n"
                        + "Puede marcar su entrada desde las " + hhmm(desde) + ".";
            }
        }
        StringBuilder bloques = new StringBuilder();
        for (HorarioPersonal bloque : pendientes) {
            if (bloques.length() > 0) {
                bloques.append(" / ");
            }
            bloques.append(hhmm(minutosDelDia(bloque.getHoraInicio()))).append("-")
                   .append(hhmm(minutosDelDia(bloque.getHoraFin())));
        }
        return "Su horario de hoy ya terminó (" + bloques + ").\n"
                + "No se puede registrar la entrada fuera del horario asignado.";
    }

    // Prefijo con el que se marcan las observaciones de los registros simulados
    public static final String PREFIJO_SIMULADO = "[SIMULADO] ";

    // Registrar entrada con la fecha y hora actuales del sistema
    public boolean registrarEntrada(String dni) {
        return registrarEntrada(dni, LocalDateTime.now(), false);
    }

    // Registrar entrada en un momento dado. Si "simulado" es true, se usa la fecha/hora
    // indicada (modo demostración) y el registro queda marcado como [SIMULADO].
    // Se aplican exactamente las mismas reglas de horario que en un registro real.
    public boolean registrarEntrada(String dni, LocalDateTime momento, boolean simulado) {
        String sufijoTitulo = simulado ? " (simulación)" : "";
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

            Date fechaActual = java.sql.Date.valueOf(momento.toLocalDate());
            Time horaActual = Time.valueOf(momento.toLocalTime().withNano(0));
            String diaSemana = obtenerDiaSemana(fechaActual);

            List<HorarioPersonal> bloquesDelDia = horarioPersonalDAO.listarPorPersonalYDia(
                personal.getIdPersonal(), diaSemana);

            Asistencia asistencia = new Asistencia();
            asistencia.setIdPersonal(personal.getIdPersonal());
            asistencia.setFecha(fechaActual);
            asistencia.setHoraEntrada(horaActual);

            if (bloquesDelDia.isEmpty()) {
                // Si la persona SÍ tiene horario detallado (en otros días), hoy no le corresponde
                // trabajar: no se registra nada para evitar pagos fuera de horario.
                if (!horarioPersonalDAO.listarPorPersonal(personal.getIdPersonal()).isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                        personal.getNombreCompleto() + " no tiene horario asignado para hoy (" + diaSemana + ").\n\n"
                        + "Su horario es: " + obtenerHorarioTexto(personal) + "\n\n"
                        + "No se registró la entrada.",
                        "Fuera de horario",
                        JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                // Personal SIN ningún horario detallado: se mantiene el registro simple
                // (sin bloque específico), como antes.
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
                List<HorarioPersonal> pendientes = bloquesPendientes(
                    personal.getIdPersonal(), fechaActual, bloquesDelDia);

                if (pendientes.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                        "El personal ya registró su entrada en todos los bloques de horario de hoy (" + diaSemana + ")",
                        "Registro duplicado",
                        JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                LocalTime ahora = horaActual.toLocalTime();
                HorarioPersonal bloque = elegirBloqueParaEntrada(pendientes, ahora);

                if (bloque == null) {
                    JOptionPane.showMessageDialog(null,
                        personal.getNombreCompleto() + "\n\n" + motivoEntradaFueraDeHorario(pendientes, ahora)
                        + "\n\nNo se registró la entrada.",
                        "Fuera de horario",
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

            if (simulado) {
                asistencia.setObservaciones(PREFIJO_SIMULADO + asistencia.getObservaciones());
            }

            boolean resultado = asistenciaDAO.registrarEntrada(asistencia);

            if (resultado) {
                String mensajeTardanza = asistencia.getMinutosTardanza() > 0
                        ? "\nTardanza: " + formatearTiempo(asistencia.getMinutosTardanza()) : "";
                JOptionPane.showMessageDialog(null,
                    "Entrada registrada exitosamente" + sufijoTitulo + "\n" +
                    "Personal: " + personal.getNombreCompleto() + "\n" +
                    (simulado ? "Fecha: " + formatoFecha(momento.toLocalDate()) + "\n" : "") +
                    "Hora: " + horaActual + mensajeTardanza,
                    "Registro exitoso" + sufijoTitulo,
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

    // Calcula el tiempo a pagar de un registro, en minutos.
    // - Las horas normales se cuentan desde el INICIO del bloque (llegar antes no suma)
    //   y hasta el FIN del bloque (o la salida, si sale antes).
    // - Si llega dentro de la tolerancia (hasta TOLERANCIA_MINUTOS después del inicio),
    //   se considera puntual y se paga desde el inicio del bloque. Si llega más tarde,
    //   se paga desde su hora real de entrada.
    // - Lo que pasa del fin del bloque es tiempo extra: si es menor a MINIMO_EXTRAS_MINUTOS
    //   no cuenta, y nunca se reconoce más de TOPE_EXTRAS_MINUTOS.
    // Devuelve {minutosPagables (normales + extra reconocidos), minutosExtra reconocidos, minutosExtra reales}
    static int[] calcularMinutosPagables(int entrada, int salida, int inicioBloque, int finBloque) {
        int desde = entrada <= inicioBloque + TOLERANCIA_MINUTOS ? inicioBloque : entrada;
        int hastaNormal = Math.min(salida, finBloque);
        int normales = Math.max(0, hastaNormal - desde);

        int extraReal = Math.max(0, salida - Math.max(finBloque, desde));
        int extraReconocido = extraReal < MINIMO_EXTRAS_MINUTOS ? 0 : Math.min(extraReal, TOPE_EXTRAS_MINUTOS);

        return new int[] { normales + extraReconocido, extraReconocido, extraReal };
    }

    // Registrar salida con la fecha y hora actuales del sistema
    public boolean registrarSalida(String dni) {
        return registrarSalida(dni, LocalDateTime.now(), false);
    }

    // Registrar salida en un momento dado (ver registrarEntrada con simulación)
    public boolean registrarSalida(String dni, LocalDateTime momento, boolean simulado) {
        String sufijoTitulo = simulado ? " (simulación)" : "";
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

            Date fechaActual = java.sql.Date.valueOf(momento.toLocalDate());

            // Solo se cierra una entrada abierta DE HOY (con hora de entrada y sin salida).
            // No se usan registros de días anteriores ni faltas (AUSENTE), porque
            // calcularía horas mezclando fechas o fallaría por no tener hora de entrada.
            Asistencia asistencia = asistenciaDAO.obtenerBloqueAbierto(personal.getIdPersonal(), fechaActual);

            if (asistencia == null || asistencia.getHoraEntrada() == null) {
                JOptionPane.showMessageDialog(null,
                    "No hay una entrada registrada " + (simulado ? "el " + formatoFecha(momento.toLocalDate()) : "hoy")
                    + " pendiente de salida para:\n"
                    + personal.getNombreCompleto() + "\n\n"
                    + "Primero debe registrar la entrada.",
                    "Sin entrada registrada",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }

            Time horaSalida = Time.valueOf(momento.toLocalTime().withNano(0));
            if (minutosDelDia(horaSalida) < minutosDelDia(asistencia.getHoraEntrada())) {
                JOptionPane.showMessageDialog(null,
                    "La hora de salida (" + horaSalida + ") no puede ser anterior a la hora de entrada ("
                    + asistencia.getHoraEntrada() + ").",
                    "Hora inválida" + sufijoTitulo,
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
            int entradaMin = minutosDelDia(asistencia.getHoraEntrada());
            int salidaMin = minutosDelDia(horaSalida);

            // Por defecto (personal sin horario detallado): jornada de referencia desde la entrada
            int inicioBloque = entradaMin;
            int finBloque = entradaMin + JORNADA_SIN_HORARIO_MINUTOS;

            if (asistencia.getIdHorario() != null) {
                String diaSemana = obtenerDiaSemana(fechaActual);
                List<HorarioPersonal> bloquesDelDia = horarioPersonalDAO.listarPorPersonalYDia(
                        personal.getIdPersonal(), diaSemana);
                for (HorarioPersonal b : bloquesDelDia) {
                    if (b.getIdHorario() == asistencia.getIdHorario()) {
                        inicioBloque = minutosDelDia(b.getHoraInicio());
                        finBloque = minutosDelDia(b.getHoraFin());
                        break;
                    }
                }
            }

            int[] tiempos = calcularMinutosPagables(entradaMin, salidaMin, inicioBloque, finBloque);
            int minutosExtras = tiempos[1];
            int extraReal = tiempos[2];
            double horasTrabajadas = Math.round(tiempos[0] / 60.0 * 100.0) / 100.0;

            String notaExtras = "";
            if (extraReal > 0 && extraReal < MINIMO_EXTRAS_MINUTOS) {
                notaExtras = "\n(" + extraReal + " min después del horario: menos de "
                        + MINIMO_EXTRAS_MINUTOS + " min, no cuenta como extra)";
            } else if (extraReal > TOPE_EXTRAS_MINUTOS) {
                notaExtras = "\n(Se quedó " + formatearTiempo(extraReal) + " después del horario; se reconoce el tope de "
                        + formatearTiempo(TOPE_EXTRAS_MINUTOS) + ")";
            }

            boolean resultado = asistenciaDAO.registrarSalida(
                asistencia.getIdAsistencia(), horaSalida, minutosExtras, horasTrabajadas);

            if (resultado) {
                JOptionPane.showMessageDialog(null,
                    "Salida registrada exitosamente" + sufijoTitulo + "\n" +
                    "Personal: " + personal.getNombreCompleto() + "\n" +
                    (simulado ? "Fecha: " + formatoFecha(momento.toLocalDate()) + "\n" : "") +
                    "Hora: " + horaSalida + "\n" +
                    "Horas a pagar: " + String.format("%.2f", horasTrabajadas) + "\n" +
                    "Horas extras: " + formatearTiempo(minutosExtras) + notaExtras,
                    "Registro exitoso" + sufijoTitulo,
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
        return generarFaltasAutomaticas(fechaInicio, fechaFin, LocalDateTime.now());
    }

    // Versión con "ahora" explícito (permite probar la lógica con una fecha/hora fija).
    // Solo se marca falta en bloques que YA TERMINARON: días anteriores completos y,
    // para el día de hoy, únicamente los bloques cuya hora de fin ya pasó.
    int generarFaltasAutomaticas(Date fechaInicio, Date fechaFin, LocalDateTime ahora) {
        int faltasRegistradas = 0;
        List<Personal> listaPersonal = personalDAO.listarPersonal();

        LocalDate hoy = ahora.toLocalDate();
        LocalDate inicio = aLocalDate(fechaInicio);
        LocalDate fin = aLocalDate(fechaFin);
        LocalDate limite = fin.isAfter(hoy) ? hoy : fin;

        // En feriados no se marcan faltas
        java.util.Set<LocalDate> feriados = limite.isBefore(inicio)
                ? java.util.Collections.emptySet()
                : new modelo.dao.FeriadoDAO().fechasEntre(inicio, limite);

        for (LocalDate dia = inicio; !dia.isAfter(limite); dia = dia.plusDays(1)) {
            if (feriados.contains(dia)) {
                continue;
            }
            Date fecha = java.sql.Date.valueOf(dia);
            String diaSemana = obtenerDiaSemana(fecha);

            for (Personal personal : listaPersonal) {
                // No se marcan faltas antes de la fecha de contratación
                if (personal.getFechaContratacion() != null
                        && dia.isBefore(aLocalDate(personal.getFechaContratacion()))) {
                    continue;
                }
                List<HorarioPersonal> bloquesDelDia = horarioPersonalDAO.listarPorPersonalYDia(
                        personal.getIdPersonal(), diaSemana);

                for (HorarioPersonal bloque : bloquesDelDia) {
                    if (!bloqueYaTermino(dia, bloque, ahora)) {
                        continue; // el bloque aún no termina: todavía puede marcar
                    }
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

        }

        return faltasRegistradas;
    }

    // Un bloque "ya terminó" si es de un día anterior a hoy, o si es de hoy y su hora de fin ya pasó
    static boolean bloqueYaTermino(LocalDate dia, HorarioPersonal bloque, LocalDateTime ahora) {
        LocalDate hoy = ahora.toLocalDate();
        if (dia.isBefore(hoy)) {
            return true;
        }
        if (dia.isAfter(hoy) || bloque.getHoraFin() == null) {
            return false;
        }
        return !ahora.toLocalTime().isBefore(bloque.getHoraFin().toLocalTime());
    }

    private static LocalDate aLocalDate(Date fecha) {
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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

    private static String formatoFecha(LocalDate fecha) {
        return fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // Cantidad de registros creados en modo simulación
    public int contarRegistrosSimulados() {
        return asistenciaDAO.contarPorPrefijoObservacion(PREFIJO_SIMULADO);
    }

    // Elimina todos los registros creados en modo simulación (no toca los registros reales)
    public int eliminarRegistrosSimulados() {
        return asistenciaDAO.eliminarPorPrefijoObservacion(PREFIJO_SIMULADO);
    }

    // Buscar personal por DNI
    public Personal buscarPersonal(String dni) {
        return personalDAO.buscarPorDni(dni);
    }
}
