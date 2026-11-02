// controlador/ControladorReporte.java
package controlador;

import modelo.dao.AsistenciaDAO;
import modelo.dao.PersonalDAO;
import modelo.dao.PlanillaDAO;
import modelo.entidades.Asistencia;
import modelo.entidades.Personal;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ControladorReporte {
    private AsistenciaDAO asistenciaDAO;
    private PersonalDAO personalDAO;
    private PlanillaDAO planillaDAO;
    
    public ControladorReporte() {
        this.asistenciaDAO = new AsistenciaDAO();
        this.personalDAO = new PersonalDAO();
        this.planillaDAO = new PlanillaDAO();
    }
    
    // Generar reporte de asistencias por rango de fechas
    public void generarReporteAsistencia(JTable tabla, Date fechaInicio, Date fechaFin, String tipoPersonal) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);
        
        List<Asistencia> asistencias = asistenciaDAO.listarPorRangoFechas(fechaInicio, fechaFin);
        
        for (Asistencia asistencia : asistencias) {
            // Buscar información del personal
            List<Personal> listaPersonal = personalDAO.listarPersonal();
            Personal personal = null;
            
            for (Personal p : listaPersonal) {
                if (p.getIdPersonal() == asistencia.getIdPersonal()) {
                    personal = p;
                    break;
                }
            }
            
            // Filtrar por tipo de personal si es necesario
            if (personal != null) {
                if (tipoPersonal.equals("TODOS") || personal.getTipoPersonal().equals(tipoPersonal)) {
                    Object[] fila = {
                        personal.getDni(),
                        personal.getNombreCompleto(),
                        personal.getTipoPersonal(),
                        asistencia.getFecha(),
                        asistencia.getHoraEntrada(),
                        asistencia.getHoraSalida() != null ? asistencia.getHoraSalida() : "Sin registro",
                        asistencia.getEstado(),
                        ControladorAsistencia.formatearTiempo(asistencia.getMinutosTardanza()),
                        ControladorAsistencia.formatearTiempo(asistencia.getMinutosExtras())
                    };
                    modelo.addRow(fila);
                }
            }
        }
    }
    
    // Generar resumen estadístico de asistencia
    public Map<String, Object> generarResumenEstadistico(Date fechaInicio, Date fechaFin) {
        Map<String, Object> resumen = new HashMap<>();
        
        List<Asistencia> asistencias = asistenciaDAO.listarPorRangoFechas(fechaInicio, fechaFin);
        
        int totalRegistros = asistencias.size();
        int totalPresentes = 0;
        int totalTardanzas = 0;
        int totalAusencias = 0;
        int totalMinutosExtras = 0;
        int totalMinutosTardanza = 0;
        
        for (Asistencia asistencia : asistencias) {
            switch (asistencia.getEstado()) {
                case "PRESENTE":
                    totalPresentes++;
                    break;
                case "TARDANZA":
                    totalTardanzas++;
                    break;
                case "AUSENTE":
                    totalAusencias++;
                    break;
            }
            
            totalMinutosExtras += asistencia.getMinutosExtras();
            totalMinutosTardanza += asistencia.getMinutosTardanza();
        }
        
        resumen.put("totalRegistros", totalRegistros);
        resumen.put("totalPresentes", totalPresentes);
        resumen.put("totalTardanzas", totalTardanzas);
        resumen.put("totalAusencias", totalAusencias);
        resumen.put("totalHorasExtras", totalMinutosExtras / 60.0);
        resumen.put("totalHorasTardanza", totalMinutosTardanza / 60.0);
        resumen.put("porcentajeAsistencia", totalRegistros > 0 ? (totalPresentes * 100.0 / totalRegistros) : 0);
        
        return resumen;
    }
    
    // Cantidad de incidencias (tardanzas + faltas) desde la cual el personal queda "EN ALERTA"
    public static final int UMBRAL_ALERTA = 3;
    public static final String ESTADO_ALERTA = "EN ALERTA";
    public static final String ESTADO_NORMAL = "NORMAL";

    // Reporte de tardanzas y faltas por persona, con alerta para quien acumula
    // UMBRAL_ALERTA o más incidencias en el periodo. Antes de calcularlo se detectan
    // las faltas de los bloques ya terminados (igual que al generar la planilla).
    // Columnas: DNI, Nombre, Tipo, Tardanzas, Faltas, Minutos tardanza, Tiempo total, Estado
    public void generarReporteTardanzas(JTable tabla, Date fechaInicio, Date fechaFin) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);

        new ControladorAsistencia().generarFaltasAutomaticas(fechaInicio, fechaFin);

        List<Asistencia> asistencias = asistenciaDAO.listarPorRangoFechas(fechaInicio, fechaFin);
        Map<Integer, int[]> conteo = new HashMap<>(); // {tardanzas, faltas, minutos}
        for (Asistencia a : asistencias) {
            int[] c = conteo.computeIfAbsent(a.getIdPersonal(), k -> new int[3]);
            if ("TARDANZA".equals(a.getEstado())) {
                c[0]++;
                c[2] += a.getMinutosTardanza();
            } else if ("AUSENTE".equals(a.getEstado())) {
                c[1]++;
            }
        }

        List<Object[]> filas = new java.util.ArrayList<>();
        for (Personal personal : personalDAO.listarPersonal(true)) {
            int[] c = conteo.get(personal.getIdPersonal());
            if (c == null || c[0] + c[1] == 0) {
                continue;
            }
            filas.add(new Object[] {
                personal.getDni(),
                personal.getNombreCompleto() + (personal.isActivo() ? "" : " (inactivo)"),
                personal.getTipoPersonal(),
                c[0],
                c[1],
                c[2],
                ControladorAsistencia.formatearTiempo(c[2]),
                c[0] + c[1] >= UMBRAL_ALERTA ? ESTADO_ALERTA : ESTADO_NORMAL
            });
        }
        // Primero quienes tienen más incidencias
        filas.sort((x, y) -> Integer.compare((int) y[3] + (int) y[4], (int) x[3] + (int) x[4]));
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
    }
    
    // Generar reporte de horas extras
    public void generarReporteHorasExtras(JTable tabla, Date fechaInicio, Date fechaFin) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);
        
        List<Personal> listaPersonal = personalDAO.listarPersonal();
        
        for (Personal personal : listaPersonal) {
            List<Asistencia> asistencias = asistenciaDAO.listarPorRangoFechas(fechaInicio, fechaFin);
            
            int totalMinutosExtras = 0;
            
            for (Asistencia asistencia : asistencias) {
                if (asistencia.getIdPersonal() == personal.getIdPersonal()) {
                    totalMinutosExtras += asistencia.getMinutosExtras();
                }
            }
            
            if (totalMinutosExtras > 0) {
                double horasExtras = totalMinutosExtras / 60.0;
                double valorHora = personal.getSalarioBase() / 30.0 / 8.0;
                double montoEstimado = valorHora * horasExtras * 1.25;
                
                Object[] fila = {
                    personal.getDni(),
                    personal.getNombreCompleto(),
                    personal.getCargo(),
                    totalMinutosExtras,
                    String.format("%.2f horas", horasExtras),
                    String.format("S/ %.2f", montoEstimado)
                };
                modelo.addRow(fila);
            }
        }
    }

    // =====================================================================
    // Reportes individuales (una sola persona)
    // =====================================================================

    private static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
        "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

    private static String diaSemana(Date fecha) {
        String d = ControladorAsistencia.obtenerDiaSemana(fecha);
        switch (d) {
            case "MIERCOLES": return "Miércoles";
            case "SABADO": return "Sábado";
            default: return d.charAt(0) + d.substring(1).toLowerCase();
        }
    }

    private static String fecha(Date f) {
        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(f);
    }

    private static String soles(double monto) {
        return String.format(java.util.Locale.US, "S/ %,.2f", monto);
    }

    private static double montoDe(String texto) {
        if (texto == null) return 0;
        String t = texto.replace("S/", "").replace(",", "").trim();
        try {
            return t.isEmpty() ? 0 : Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Datos de la persona para el encabezado del reporte individual. */
    private Map<String, String> datosPersona(String dni, String nombreRespaldo) {
        Map<String, String> datos = new java.util.LinkedHashMap<>();
        Personal p = personalDAO.buscarPorDniIncluyendoInactivos(dni);
        if (p == null) {
            datos.put("Nombre", nombreRespaldo);
            datos.put("DNI", dni);
            return datos;
        }
        boolean docente = "DOCENTE".equalsIgnoreCase(p.getTipoPersonal());
        datos.put("Nombre", p.getNombreCompleto() + (p.isActivo() ? "" : " (inactivo)"));
        datos.put("DNI", p.getDni());
        datos.put("Tipo", docente ? "Docente" : "Administrativo");
        datos.put("Cargo", p.getCargo());
        datos.put("Sueldo mensual", soles(p.getSalarioBase()));
        return datos;
    }

    private Personal personal(String dni) {
        return personalDAO.buscarPorDniIncluyendoInactivos(dni);
    }

    /** Reporte de asistencias de una persona a partir de la fila seleccionada (columna "DNI"). */
    public util.ExportadorReportes.ReporteIndividual individualAsistencia(JTable tabla, int fila, String titulo,
            String periodo) {
        String dni = util.ExportadorReportes.valor(tabla, fila, "DNI");
        String nombre = util.ExportadorReportes.valor(tabla, fila, "Nombre", "Nombre Completo");
        util.ExportadorReportes.ReporteIndividual r = new util.ExportadorReportes.ReporteIndividual();
        r.titulo = titulo;
        r.subtitulo = periodo;
        r.persona = datosPersona(dni, nombre);
        JTable filtrada = util.ExportadorReportes.filtrarTabla(tabla, "DNI", dni,
                "ID", "DNI", "Nombre", "Nombre Completo", "Tipo", "Tipo Personal");
        r.detalle = conDiaYOrdenCronologico(filtrada);
        r.tituloDetalle = "Detalle de asistencias";
        return r;
    }

    // Ordena por fecha (de la más antigua a la más reciente), muestra la fecha como dd/MM/yyyy
    // y agrega la columna "Día" a continuación de la fecha.
    private static JTable conDiaYOrdenCronologico(JTable tabla) {
        int colFecha = -1;
        for (int c = 0; c < tabla.getColumnCount(); c++) {
            if (tabla.getColumnName(c).equalsIgnoreCase("Fecha")) colFecha = c;
        }
        if (colFecha < 0) return tabla;
        java.util.List<String> cols = new java.util.ArrayList<>();
        for (int c = 0; c < tabla.getColumnCount(); c++) {
            cols.add(tabla.getColumnName(c));
            if (c == colFecha) cols.add("Día");
        }
        java.util.List<Object[]> filas = new java.util.ArrayList<>();
        for (int r = 0; r < tabla.getRowCount(); r++) {
            Object[] f = new Object[cols.size()];
            int k = 0;
            for (int c = 0; c < tabla.getColumnCount(); c++) {
                Object v = tabla.getValueAt(r, c);
                if (c == colFecha) {
                    java.sql.Date d = null;
                    try { d = java.sql.Date.valueOf(String.valueOf(v)); } catch (IllegalArgumentException ignorada) { }
                    f[k++] = d != null ? d : v;
                    f[k++] = d != null ? diaSemana(d) : "";
                } else {
                    f[k++] = v;
                }
            }
            filas.add(f);
        }
        final int cf = colFecha;
        filas.sort((x, y) -> String.valueOf(x[cf]).compareTo(String.valueOf(y[cf])));
        DefaultTableModel m = new DefaultTableModel(cols.toArray(), 0);
        for (Object[] f : filas) {
            if (f[cf] instanceof java.sql.Date) f[cf] = fecha((java.sql.Date) f[cf]);
            m.addRow(f);
        }
        return new JTable(m);
    }

    /** Reporte de tardanzas y faltas de una persona, con el detalle de cada día. */
    public util.ExportadorReportes.ReporteIndividual individualTardanzas(JTable tabla, int fila, Date inicio,
            Date fin, String periodo) {
        String dni = util.ExportadorReportes.valor(tabla, fila, "DNI");
        String nombre = util.ExportadorReportes.valor(tabla, fila, "Nombre", "Nombre Completo");
        Personal p = personal(dni);

        util.ExportadorReportes.ReporteIndividual r = new util.ExportadorReportes.ReporteIndividual();
        r.titulo = "Reporte de Tardanzas y Faltas";
        r.subtitulo = periodo;
        r.persona = datosPersona(dni, nombre);

        DefaultTableModel m = new DefaultTableModel(
                new String[] {"Fecha", "Día", "Entrada", "Estado", "Tardanza"}, 0);
        int tardanzas = 0, faltas = 0, minutos = 0;
        if (p != null) {
            for (Asistencia a : asistenciaDAO.listarPorRangoFechas(inicio, fin)) {
                if (a.getIdPersonal() != p.getIdPersonal()) continue;
                boolean esTardanza = "TARDANZA".equals(a.getEstado());
                boolean esFalta = "AUSENTE".equals(a.getEstado());
                if (!esTardanza && !esFalta) continue;
                if (esTardanza) { tardanzas++; minutos += a.getMinutosTardanza(); }
                if (esFalta) faltas++;
                m.addRow(new Object[] {
                    fecha(a.getFecha()), diaSemana(a.getFecha()),
                    a.getHoraEntrada() != null ? a.getHoraEntrada().toString() : "",
                    esFalta ? "AUSENTE" : "TARDANZA",
                    esTardanza ? ControladorAsistencia.formatearTiempo(a.getMinutosTardanza()) : ""
                });
            }
        }
        // Orden cronológico
        java.util.List<Object[]> filas = new java.util.ArrayList<>();
        for (int i = 0; i < m.getRowCount(); i++) {
            Object[] f = new Object[m.getColumnCount()];
            for (int c = 0; c < f.length; c++) f[c] = m.getValueAt(i, c);
            filas.add(f);
        }
        filas.sort((x, y) -> {
            String[] a = x[0].toString().split("/"), b = y[0].toString().split("/");
            return (a[2] + a[1] + a[0]).compareTo(b[2] + b[1] + b[0]);
        });
        m.setRowCount(0);
        for (Object[] f : filas) m.addRow(f);

        r.tarjetas.put("Tardanzas", String.valueOf(tardanzas));
        r.tarjetas.put("Faltas", String.valueOf(faltas));
        r.tarjetas.put("Tiempo de tardanza", ControladorAsistencia.formatearTiempo(minutos));
        r.tarjetas.put("Situación", tardanzas + faltas >= UMBRAL_ALERTA ? "EN ALERTA" : "NORMAL");
        r.detalle = new JTable(m);
        r.tituloDetalle = "Detalle de tardanzas y faltas";
        r.sinDetalle = "No registra tardanzas ni faltas en el periodo.";
        return r;
    }

    /** Reporte de horas extras de una persona, con el detalle de cada día. */
    public util.ExportadorReportes.ReporteIndividual individualHorasExtras(JTable tabla, int fila, Date inicio,
            Date fin, String periodo) {
        String dni = util.ExportadorReportes.valor(tabla, fila, "DNI");
        String nombre = util.ExportadorReportes.valor(tabla, fila, "Nombre", "Nombre Completo");
        Personal p = personal(dni);

        util.ExportadorReportes.ReporteIndividual r = new util.ExportadorReportes.ReporteIndividual();
        r.titulo = "Reporte de Horas Extras";
        r.subtitulo = periodo;
        r.persona = datosPersona(dni, nombre);

        double valorHora = p == null ? 0 : p.getSalarioBase() / 30.0 / 8.0;

        java.util.List<Asistencia> lista = new java.util.ArrayList<>();
        if (p != null) {
            for (Asistencia a : asistenciaDAO.listarPorRangoFechas(inicio, fin)) {
                if (a.getIdPersonal() == p.getIdPersonal() && a.getMinutosExtras() > 0) lista.add(a);
            }
        }
        lista.sort((x, y) -> x.getFecha().compareTo(y.getFecha()));

        DefaultTableModel m = new DefaultTableModel(
                new String[] {"Fecha", "Día", "Entrada", "Salida", "Horas Extra", "Monto"}, 0);
        int totalMin = 0;
        for (Asistencia a : lista) {
            totalMin += a.getMinutosExtras();
            m.addRow(new Object[] {
                fecha(a.getFecha()), diaSemana(a.getFecha()),
                a.getHoraEntrada() != null ? a.getHoraEntrada().toString() : "",
                a.getHoraSalida() != null ? a.getHoraSalida().toString() : "",
                ControladorAsistencia.formatearTiempo(a.getMinutosExtras()),
                soles(valorHora * a.getMinutosExtras() / 60.0 * 1.25)
            });
        }
        r.tarjetas.put("Días con horas extra", String.valueOf(lista.size()));
        r.tarjetas.put("Horas extra", ControladorAsistencia.formatearTiempo(totalMin));
        r.tarjetas.put("Monto estimado", soles(valorHora * totalMin / 60.0 * 1.25));
        r.detalle = new JTable(m);
        r.tituloDetalle = "Detalle de horas extra";
        r.sinDetalle = "No registra horas extra en el periodo.";
        return r;
    }

    /** Boleta de pago de una persona a partir de la fila seleccionada del detalle de planilla. */
    public util.ExportadorReportes.ReporteIndividual boletaPlanilla(JTable detalle, int fila, String periodo) {
        String dni = util.ExportadorReportes.valor(detalle, fila, "DNI");
        String nombre = util.ExportadorReportes.valor(detalle, fila, "Nombre");
        Personal p = personal(dni);

        util.ExportadorReportes.ReporteIndividual r = new util.ExportadorReportes.ReporteIndividual();
        r.titulo = "Boleta de Pago";
        r.subtitulo = "Periodo: " + nombrePeriodo(periodo);
        r.persona = datosPersona(dni, nombre);

        double montoExtra = montoDe(util.ExportadorReportes.valor(detalle, fila, "Monto Extra"));
        double totalIng = montoDe(util.ExportadorReportes.valor(detalle, fila, "Total Ing."));
        double descAus = montoDe(util.ExportadorReportes.valor(detalle, fila, "Desc. Aus."));
        double descTard = montoDe(util.ExportadorReportes.valor(detalle, fila, "Desc. Tard."));
        double totalDesc = montoDe(util.ExportadorReportes.valor(detalle, fila, "Total Desc."));
        double neto = montoDe(util.ExportadorReportes.valor(detalle, fila, "Sueldo Neto"));
        double horasExtra = montoDe(util.ExportadorReportes.valor(detalle, fila, "Horas Extra"));

        java.util.List<String[]> asistencia = new java.util.ArrayList<>();
        asistencia.add(new String[] {"Días trabajados", util.ExportadorReportes.valor(detalle, fila, "Días Trab.")});
        asistencia.add(new String[] {"Días de ausencia", util.ExportadorReportes.valor(detalle, fila, "Días Aus.")});
        asistencia.add(new String[] {"Minutos de tardanza", util.ExportadorReportes.valor(detalle, fila, "Min. Tard.")});
        asistencia.add(new String[] {"Horas extra", String.format(java.util.Locale.US, "%.2f h", horasExtra)});

        java.util.List<String[]> ingresos = new java.util.ArrayList<>();
        ingresos.add(new String[] {"Sueldo base", soles(totalIng - montoExtra)});
        ingresos.add(new String[] {"Horas extra (125%)", soles(montoExtra)});
        ingresos.add(new String[] {"Total ingresos", soles(totalIng)});

        java.util.List<String[]> descuentos = new java.util.ArrayList<>();
        descuentos.add(new String[] {"Por ausencias", soles(descAus)});
        descuentos.add(new String[] {"Por tardanzas", soles(descTard)});
        descuentos.add(new String[] {"Total descuentos", soles(totalDesc)});

        r.secciones.put("Asistencia del periodo", asistencia);
        r.secciones.put("Ingresos", ingresos);
        r.secciones.put("Descuentos", descuentos);
        r.destacado = new String[] {"NETO A PAGAR", soles(neto)};
        r.firmas = true;
        return r;
    }

    private static String nombrePeriodo(String periodo) {
        try {
            String[] partes = periodo.split("-");
            int mes = Integer.parseInt(partes[1]);
            return MESES[mes - 1] + " " + partes[0];
        } catch (Exception e) {
            return periodo;
        }
    }
}
