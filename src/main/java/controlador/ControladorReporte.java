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
    
    // Generar reporte de personal con más tardanzas
    public void generarReporteTardanzas(JTable tabla, Date fechaInicio, Date fechaFin) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);
        
        List<Personal> listaPersonal = personalDAO.listarPersonal();
        
        for (Personal personal : listaPersonal) {
            List<Asistencia> asistencias = asistenciaDAO.listarPorRangoFechas(fechaInicio, fechaFin);
            
            int totalTardanzas = 0;
            int totalMinutosTardanza = 0;
            
            for (Asistencia asistencia : asistencias) {
                if (asistencia.getIdPersonal() == personal.getIdPersonal() && 
                    "TARDANZA".equals(asistencia.getEstado())) {
                    totalTardanzas++;
                    totalMinutosTardanza += asistencia.getMinutosTardanza();
                }
            }
            
            if (totalTardanzas > 0) {
                Object[] fila = {
                    personal.getDni(),
                    personal.getNombreCompleto(),
                    personal.getTipoPersonal(),
                    totalTardanzas,
                    totalMinutosTardanza,
                    String.format("%.2f horas", totalMinutosTardanza / 60.0)
                };
                modelo.addRow(fila);
            }
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
                double montoEstimado = (personal.getSalarioBase() / 30.0 / 8.0) * horasExtras * 1.25;
                
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
}
