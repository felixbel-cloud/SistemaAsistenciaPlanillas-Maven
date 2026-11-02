// util/ExportadorReportes.java
package util;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Exporta el contenido de una JTable a Excel (.xlsx) o PDF con un diseño
 * institucional: encabezado con el nombre del instituto, resumen con totales,
 * tabla con filas alternadas, estados en color y numeración de páginas.
 */
public class ExportadorReportes {

    // ---------- Paleta institucional ----------
    private static final Color AZUL_OSCURO = new Color(12, 35, 64);
    private static final Color AZUL = new Color(28, 92, 160);
    private static final Color DORADO = new Color(232, 185, 72);
    private static final Color TEXTO = new Color(30, 41, 59);
    private static final Color TEXTO_SUAVE = new Color(100, 116, 139);
    private static final Color LINEA = new Color(226, 232, 240);
    private static final Color FILA_ALTERNA = new Color(246, 249, 252);
    private static final Color FONDO_TARJETA = new Color(244, 247, 251);

    private static final Color VERDE = new Color(22, 128, 61);
    private static final Color VERDE_FONDO = new Color(220, 245, 228);
    private static final Color AMBAR = new Color(180, 100, 8);
    private static final Color AMBAR_FONDO = new Color(254, 240, 199);
    private static final Color ROJO = new Color(185, 28, 28);
    private static final Color ROJO_FONDO = new Color(254, 226, 226);

    private static final String INSTITUCION = "INSTITUTO AMERICANO";
    private static final String SISTEMA = "Sistema de Asistencias y Planillas";

    private static final Pattern MONEDA = Pattern.compile("^S/\\s?(-?[\\d,]*\\.?\\d+)$");
    private static final Pattern ENTERO = Pattern.compile("^-?\\d+$");
    private static final Pattern DECIMAL = Pattern.compile("^-?\\d+\\.\\d+$");

    // =====================================================================
    // API pública (usada por las ventanas)
    // =====================================================================

    /** Exporta a Excel usando el nombre del archivo como título. */
    public static void exportarAExcel(JTable tabla, String nombreArchivo) {
        exportarAExcel(tabla, nombreArchivo.replace('_', ' '), null, nombreArchivo);
    }

    /** Exporta a Excel con título y subtítulo (por ejemplo, el periodo del reporte). */
    public static void exportarAExcel(JTable tabla, String titulo, String subtitulo, String nombreArchivo) {
        File archivo = elegirArchivo("Guardar reporte en Excel", nombreArchivo, "xlsx", "Archivos Excel (*.xlsx)");
        if (archivo == null) {
            return;
        }
        try {
            generarExcel(tabla, titulo, subtitulo, archivo);
            mostrarExito(archivo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al exportar a Excel: " + e.getMessage(),
                    "Error de exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Exporta a PDF con el título indicado. */
    public static void exportarAPDF(JTable tabla, String tituloReporte, String nombreArchivo) {
        exportarAPDF(tabla, tituloReporte, null, nombreArchivo);
    }

    /** Exporta a PDF con título y subtítulo (por ejemplo, el periodo del reporte). */
    public static void exportarAPDF(JTable tabla, String tituloReporte, String subtitulo, String nombreArchivo) {
        File archivo = elegirArchivo("Guardar reporte en PDF", nombreArchivo, "pdf", "Archivos PDF (*.pdf)");
        if (archivo == null) {
            return;
        }
        try {
            generarPDF(tabla, tituloReporte, subtitulo, archivo);
            mostrarExito(archivo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al exportar a PDF: " + e.getMessage(),
                    "Error de exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Texto del periodo para mostrar en los reportes, por ejemplo "Periodo: 01/10/2026 al 26/10/2026". */
    public static String describirPeriodo(Date inicio, Date fin) {
        if (inicio == null || fin == null) {
            return null;
        }
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        return "Periodo: " + f.format(inicio) + " al " + f.format(fin);
    }

    // Método para formatear moneda
    public static String formatearMoneda(double monto) {
        return String.format("S/ %.2f", monto);
    }

    // Método para formatear porcentaje
    public static String formatearPorcentaje(double porcentaje) {
        return String.format("%.2f%%", porcentaje);
    }

    private static File elegirArchivo(String tituloDialogo, String nombreArchivo, String extension, String descripcion) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(tituloDialogo);
        fileChooser.setSelectedFile(new File(nombreArchivo + "." + extension));
        fileChooser.setFileFilter(new FileNameExtensionFilter(descripcion, extension));
        if (fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File archivo = fileChooser.getSelectedFile();
        if (!archivo.getName().toLowerCase(Locale.ROOT).endsWith("." + extension)) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + "." + extension);
        }
        return archivo;
    }

    private static void mostrarExito(File archivo) {
        JOptionPane.showMessageDialog(null,
                "Reporte exportado exitosamente a:\n" + archivo.getAbsolutePath(),
                "Exportación exitosa",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // =====================================================================
    // Datos comunes
    // =====================================================================

    private static String[] encabezados(JTable tabla) {
        String[] h = new String[tabla.getColumnCount()];
        for (int c = 0; c < h.length; c++) {
            h[c] = tabla.getColumnName(c);
        }
        return h;
    }

    private static String[][] datos(JTable tabla) {
        String[][] d = new String[tabla.getRowCount()][tabla.getColumnCount()];
        for (int r = 0; r < d.length; r++) {
            for (int c = 0; c < d[r].length; c++) {
                Object v = tabla.getValueAt(r, c);
                d[r][c] = v == null ? "" : v.toString().trim();
            }
        }
        return d;
    }

    private static int columnaEstado(String[] encabezados) {
        for (int c = 0; c < encabezados.length; c++) {
            if (encabezados[c] != null && encabezados[c].trim().equalsIgnoreCase("Estado")) {
                return c;
            }
        }
        return -1;
    }

    private static boolean esMoneda(String v) {
        return v != null && MONEDA.matcher(v).matches();
    }

    private static double valorMoneda(String v) {
        Matcher m = MONEDA.matcher(v);
        return m.matches() ? Double.parseDouble(m.group(1).replace(",", "")) : 0;
    }

    /** Una columna es "numérica" si todos sus valores no vacíos son montos o números. */
    private static boolean[] columnasNumericas(String[][] d, int columnas) {
        boolean[] num = new boolean[columnas];
        for (int c = 0; c < columnas; c++) {
            boolean alguno = false, todos = true;
            for (String[] fila : d) {
                String v = fila[c];
                if (v.isEmpty()) {
                    continue;
                }
                alguno = true;
                if (!(esMoneda(v) || DECIMAL.matcher(v).matches() || (ENTERO.matcher(v).matches() && v.length() < 7))) {
                    todos = false;
                    break;
                }
            }
            num[c] = alguno && todos;
        }
        return num;
    }

    /** Tarjetas de resumen: total de registros, conteo por estado y total de la última columna de montos. */
    private static Map<String, String> resumen(String[] h, String[][] d) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("Registros", String.valueOf(d.length));
        int colEstado = columnaEstado(h);
        if (colEstado >= 0) {
            int presentes = 0, tardanzas = 0, ausentes = 0;
            for (String[] fila : d) {
                String e = fila[colEstado].toUpperCase(Locale.ROOT);
                if (e.equals("PRESENTE")) presentes++;
                else if (e.equals("TARDANZA")) tardanzas++;
                else if (e.equals("AUSENTE")) ausentes++;
            }
            int enAlerta = 0, normales = 0;
            for (String[] fila : d) {
                String e = fila[colEstado].toUpperCase(Locale.ROOT);
                if (e.equals("EN ALERTA")) enAlerta++;
                else if (e.equals("NORMAL")) normales++;
            }
            if (enAlerta + normales > 0) {
                int totalTard = 0, totalFaltas = 0;
                for (int c = 0; c < h.length; c++) {
                    for (String[] fila : d) {
                        if (h[c].equalsIgnoreCase("Tardanzas") && ENTERO.matcher(fila[c]).matches()) totalTard += Integer.parseInt(fila[c]);
                        if (h[c].equalsIgnoreCase("Faltas") && ENTERO.matcher(fila[c]).matches()) totalFaltas += Integer.parseInt(fila[c]);
                    }
                }
                r.put("Personas", String.valueOf(d.length));
                r.remove("Registros");
                r.put("Tardanzas", String.valueOf(totalTard));
                r.put("Faltas", String.valueOf(totalFaltas));
                r.put("En alerta", String.valueOf(enAlerta));
                return r;
            }
            if (presentes + tardanzas + ausentes > 0) {
                r.put("Presentes", String.valueOf(presentes));
                r.put("Tardanzas", String.valueOf(tardanzas));
                r.put("Ausentes", String.valueOf(ausentes));
                return r;
            }
        }
        for (int c = h.length - 1; c >= 0; c--) {
            boolean hayMonto = false, todos = true;
            double total = 0;
            for (String[] fila : d) {
                if (fila[c].isEmpty()) continue;
                if (esMoneda(fila[c])) {
                    hayMonto = true;
                    total += valorMoneda(fila[c]);
                } else {
                    todos = false;
                }
            }
            if (hayMonto && todos) {
                r.put("Total " + h[c].toLowerCase(Locale.ROOT), String.format(Locale.US, "S/ %,.2f", total));
                break;
            }
        }
        return r;
    }

    private static Color colorEstado(String estado, boolean fondo) {
        switch (estado.toUpperCase(Locale.ROOT)) {
            case "PRESENTE": case "ACTIVO": case "PAGADA": case "NORMAL": return fondo ? VERDE_FONDO : VERDE;
            case "TARDANZA": case "GENERADA": return fondo ? AMBAR_FONDO : AMBAR;
            case "AUSENTE": case "INACTIVO": case "ANULADA": case "EN ALERTA": return fondo ? ROJO_FONDO : ROJO;
            default: return fondo ? LINEA : TEXTO_SUAVE;
        }
    }

    // =====================================================================
    // PDF
    // =====================================================================

    /** Fuentes del PDF: se usa una TrueType del sistema (con tildes); si no hay, Helvetica. */
    private static class Fuentes {
        PDFont normal;
        PDFont negrita;
        boolean unicode;

        Fuentes(PDDocument doc) {
            String win = System.getenv("WINDIR") != null ? System.getenv("WINDIR") : "C:/Windows";
            String[][] candidatas = {
                {win + "/Fonts/segoeui.ttf", win + "/Fonts/segoeuib.ttf"},
                {win + "/Fonts/arial.ttf", win + "/Fonts/arialbd.ttf"},
                {"/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"},
                {"/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf", "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf"},
                {"/Library/Fonts/Arial.ttf", "/Library/Fonts/Arial Bold.ttf"},
            };
            for (String[] par : candidatas) {
                File f1 = new File(par[0]), f2 = new File(par[1]);
                if (f1.isFile() && f2.isFile()) {
                    try {
                        normal = PDType0Font.load(doc, f1);
                        negrita = PDType0Font.load(doc, f2);
                        unicode = true;
                        return;
                    } catch (IOException ignorada) {
                        // probar con la siguiente fuente
                    }
                }
            }
            normal = PDType1Font.HELVETICA;
            negrita = PDType1Font.HELVETICA_BOLD;
            unicode = false;
        }

        String preparar(String texto) {
            if (texto == null) return "";
            String t = texto.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
            return unicode ? t : quitarTildes(t).replace("—", "-").replace("…", "...").replace("·", "-");
        }

        String puntos() {
            return unicode ? "…" : "...";
        }

        String vacio() {
            return unicode ? "—" : "-";
        }
    }

    /** Genera el PDF del reporte en el archivo indicado (sin diálogos). */
    public static void generarPDF(JTable tabla, String titulo, String subtitulo, File archivo) throws IOException {
        String[] h = encabezados(tabla);
        String[][] d = datos(tabla);
        int columnas = h.length;

        PDRectangle tamano = columnas > 6
                ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())
                : PDRectangle.A4;
        float ancho = tamano.getWidth(), alto = tamano.getHeight();
        float margen = 36f;
        float anchoUtil = ancho - margen * 2;
        float altoBanda = 78f, altoCabecera = 22f, altoFila = 19f;
        float tamTexto = 8.5f, tamCabecera = 8.5f;
        float pie = 42f;

        try (PDDocument doc = new PDDocument()) {
            Fuentes f = new Fuentes(doc);
            int colEstado = columnaEstado(h);
            boolean[] numericas = columnasNumericas(d, columnas);
            float[] anchos = calcularAnchos(h, d, f, tamTexto, tamCabecera, anchoUtil, colEstado);
            String generado = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

            List<PDPage> paginas = new ArrayList<>();
            PDPage pagina = new PDPage(tamano);
            doc.addPage(pagina);
            paginas.add(pagina);
            PDPageContentStream cs = new PDPageContentStream(doc, pagina);

            dibujarBanda(cs, f, ancho, alto, margen, altoBanda, titulo, subtitulo, generado);
            float y = alto - altoBanda - 18;

            // Tarjetas de resumen (solo en la primera página)
            Map<String, String> res = resumen(h, d);
            y = dibujarResumen(cs, f, res, margen, y, anchoUtil);
            y -= 16;

            y = dibujarCabecera(cs, f, h, anchos, numericas, colEstado, margen, y, anchoUtil, altoCabecera, tamCabecera);

            if (d.length == 0) {
                escribir(cs, f.normal, 10, TEXTO_SUAVE, margen + 8, y - 22, f.preparar("No hay registros para el periodo seleccionado."));
            }

            for (int r = 0; r < d.length; r++) {
                if (y - altoFila < pie) {
                    cs.close();
                    pagina = new PDPage(tamano);
                    doc.addPage(pagina);
                    paginas.add(pagina);
                    cs = new PDPageContentStream(doc, pagina);
                    dibujarBandaCompacta(cs, f, ancho, alto, margen, titulo, subtitulo);
                    y = alto - 46 - 16;
                    y = dibujarCabecera(cs, f, h, anchos, numericas, colEstado, margen, y, anchoUtil, altoCabecera, tamCabecera);
                }

                float yFila = y - altoFila;
                dibujarFila(cs, f, d[r], r, anchos, numericas, colEstado, margen, yFila, anchoUtil, altoFila, tamTexto);
                y = yFila;
            }
            cs.close();

            dibujarPies(doc, paginas, f, ancho, margen, generado);

            doc.getDocumentInformation().setTitle(f.preparar(titulo));
            doc.getDocumentInformation().setAuthor("Instituto Americano");
            doc.getDocumentInformation().setCreator(SISTEMA);
            doc.save(archivo);
        }
    }

    private static void dibujarFila(PDPageContentStream cs, Fuentes f, String[] fila, int r, float[] anchos,
            boolean[] numericas, int colEstado, float margen, float yFila, float anchoUtil, float altoFila,
            float tamTexto) throws IOException {
        if (r % 2 == 1) {
            rect(cs, FILA_ALTERNA, margen, yFila, anchoUtil, altoFila);
        }
        linea(cs, LINEA, margen, yFila, margen + anchoUtil, yFila, 0.5f);

        float x = margen;
        for (int c = 0; c < fila.length; c++) {
            String valor = fila[c];
            float base = yFila + (altoFila - tamTexto) / 2f + 1.5f;
            if (c == colEstado && !valor.isEmpty()) {
                dibujarEtiqueta(cs, f, valor, x + 6, yFila, altoFila, anchos[c] - 12);
            } else if (valor.isEmpty() || valor.equalsIgnoreCase("Sin registro") || valor.equalsIgnoreCase("N/A")) {
                String t = valor.isEmpty() ? f.vacio() : f.preparar(valor);
                float tx = numericas[c] ? x + anchos[c] - 6 - ancho(f.normal, tamTexto, t) : x + 6;
                escribir(cs, f.normal, tamTexto, new Color(160, 170, 184), tx, base, t);
            } else {
                String t = recortar(f.preparar(valor), anchos[c] - 12, f.normal, tamTexto, f.puntos());
                float tx = numericas[c] ? x + anchos[c] - 6 - ancho(f.normal, tamTexto, t) : x + 6;
                boolean cero = valor.equals("0 min") || valor.equals("0") || valor.equals("S/ 0.00");
                escribir(cs, f.normal, tamTexto, cero ? new Color(160, 170, 184) : TEXTO, tx, base, t);
            }
            x += anchos[c];
        }
    }

    // Pie de página con numeración "Página X de Y"
    private static void dibujarPies(PDDocument doc, List<PDPage> paginas, Fuentes f, float ancho, float margen,
            String generado) throws IOException {
        int total = paginas.size();
        for (int i = 0; i < total; i++) {
            try (PDPageContentStream pcs = new PDPageContentStream(doc, paginas.get(i),
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                linea(pcs, LINEA, margen, 30, ancho - margen, 30, 0.6f);
                escribir(pcs, f.normal, 7.5f, TEXTO_SUAVE, margen, 18,
                        f.preparar("Instituto Americano · " + SISTEMA + " · Generado el " + generado));
                String pag = f.preparar("Página " + (i + 1) + " de " + total);
                escribir(pcs, f.negrita, 7.5f, TEXTO_SUAVE, ancho - margen - ancho(f.negrita, 7.5f, pag), 18, pag);
            }
        }
    }

    // =====================================================================
    // PDF individual (una sola persona)
    // =====================================================================

    /** Contenido de un reporte individual. Todos los campos son opcionales salvo el título. */
    public static class ReporteIndividual {
        public String titulo;
        public String subtitulo;
        /** Datos de la persona (ej. "DNI" -> "72960206"); la clave "Nombre" se muestra en grande. */
        public Map<String, String> persona = new LinkedHashMap<>();
        /** Tarjetas de resumen; si queda vacío y hay detalle, se calculan del detalle. */
        public Map<String, String> tarjetas = new LinkedHashMap<>();
        /** Secciones de conceptos (ej. "Ingresos" -> filas {concepto, valor}). */
        public Map<String, List<String[]>> secciones = new LinkedHashMap<>();
        /** Valor destacado al final, ej. {"SUELDO NETO", "S/ 2,500.00"}. */
        public String[] destacado;
        /** Nota aclaratoria (se muestra en cursiva bajo el contenido). */
        public String nota;
        /** Tabla de detalle (ej. asistencias día por día). */
        public JTable detalle;
        public String tituloDetalle;
        public String sinDetalle = "No hay registros para el periodo seleccionado.";
        /** Muestra líneas de firma al final (boletas). */
        public boolean firmas;
    }

    /** Pide dónde guardar y genera el PDF individual. */
    public static void exportarAPDFIndividual(ReporteIndividual reporte, String nombreArchivo) {
        File archivo = elegirArchivo("Guardar reporte individual en PDF", nombreArchivo, "pdf", "Archivos PDF (*.pdf)");
        if (archivo == null) {
            return;
        }
        try {
            generarPDFIndividual(reporte, archivo);
            mostrarExito(archivo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al exportar a PDF: " + e.getMessage(),
                    "Error de exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Devuelve una tabla solo con las filas cuya columna {@code columna} vale {@code valor},
     * quitando las columnas indicadas (por ejemplo DNI y Nombre, que ya van en el encabezado).
     */
    public static JTable filtrarTabla(JTable tabla, String columna, String valor, String... quitarColumnas) {
        String[] h = encabezados(tabla);
        int colFiltro = -1;
        for (int c = 0; c < h.length; c++) {
            if (h[c].equalsIgnoreCase(columna)) colFiltro = c;
        }
        List<Integer> mantener = new ArrayList<>();
        for (int c = 0; c < h.length; c++) {
            boolean quitar = false;
            for (String q : quitarColumnas) {
                if (h[c].equalsIgnoreCase(q)) quitar = true;
            }
            if (!quitar) mantener.add(c);
        }
        String[] nh = new String[mantener.size()];
        for (int i = 0; i < nh.length; i++) nh[i] = h[mantener.get(i)];
        javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(nh, 0);
        for (int r = 0; r < tabla.getRowCount(); r++) {
            Object v = colFiltro >= 0 ? tabla.getValueAt(r, colFiltro) : null;
            if (colFiltro < 0 || (v != null && v.toString().trim().equals(valor))) {
                Object[] fila = new Object[nh.length];
                for (int i = 0; i < nh.length; i++) fila[i] = tabla.getValueAt(r, mantener.get(i));
                m.addRow(fila);
            }
        }
        return new JTable(m);
    }

    /** Valor de una celda buscando la columna por nombre (o "" si no existe). */
    public static String valor(JTable tabla, int fila, String... nombresColumna) {
        for (int c = 0; c < tabla.getColumnCount(); c++) {
            for (String n : nombresColumna) {
                if (tabla.getColumnName(c).equalsIgnoreCase(n)) {
                    Object v = tabla.getValueAt(fila, c);
                    return v == null ? "" : v.toString().trim();
                }
            }
        }
        return "";
    }

    /** Genera el PDF individual en el archivo indicado (sin diálogos). */
    public static void generarPDFIndividual(ReporteIndividual rep, File archivo) throws IOException {
        PDRectangle tamano = PDRectangle.A4;
        String[] h = rep.detalle != null ? encabezados(rep.detalle) : new String[0];
        String[][] d = rep.detalle != null ? datos(rep.detalle) : new String[0][0];
        if (h.length > 7) {
            tamano = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
        }
        float ancho = tamano.getWidth(), alto = tamano.getHeight();
        float margen = 36f, anchoUtil = ancho - margen * 2;
        float altoBanda = 78f, altoCabecera = 22f, altoFila = 19f, tamTexto = 8.5f, pie = 42f;

        try (PDDocument doc = new PDDocument()) {
            Fuentes f = new Fuentes(doc);
            String generado = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            List<PDPage> paginas = new ArrayList<>();
            PDPage pagina = new PDPage(tamano);
            doc.addPage(pagina);
            paginas.add(pagina);
            PDPageContentStream cs = new PDPageContentStream(doc, pagina);

            dibujarBanda(cs, f, ancho, alto, margen, altoBanda, rep.titulo, rep.subtitulo, generado);
            float y = alto - altoBanda - 18;

            // Ficha de la persona
            y = dibujarFichaPersona(cs, f, rep.persona, margen, y, anchoUtil);
            y -= 14;

            // Tarjetas
            Map<String, String> tarjetas = rep.tarjetas;
            if ((tarjetas == null || tarjetas.isEmpty()) && rep.detalle != null) {
                tarjetas = resumen(h, d);
            }
            if (tarjetas != null && !tarjetas.isEmpty()) {
                y = dibujarResumen(cs, f, tarjetas, margen, y, anchoUtil);
                y -= 16;
            }

            // Secciones de conceptos (en columnas)
            if (!rep.secciones.isEmpty()) {
                y = dibujarSecciones(cs, f, rep.secciones, margen, y, anchoUtil);
                y -= 14;
            }

            // Valor destacado (ej. sueldo neto)
            if (rep.destacado != null) {
                float hD = 46f;
                redondeado(cs, AZUL_OSCURO, margen, y - hD, anchoUtil, hD, 8);
                rect(cs, DORADO, margen, y - hD + 10, 4, hD - 20);
                cs.setCharacterSpacing(1.2f);
                escribir(cs, f.negrita, 10f, DORADO, margen + 18, y - hD / 2f - 3.5f, f.preparar(rep.destacado[0]));
                cs.setCharacterSpacing(0f);
                String v = f.preparar(rep.destacado[1]);
                escribir(cs, f.negrita, 20f, Color.WHITE, margen + anchoUtil - 18 - ancho(f.negrita, 20f, v), y - hD / 2f - 7f, v);
                y -= hD + 14;
            }

            if (rep.nota != null && !rep.nota.isBlank()) {
                for (String lineaNota : partirTexto(f.preparar(rep.nota), f.normal, 8f, anchoUtil)) {
                    escribir(cs, f.normal, 8f, TEXTO_SUAVE, margen, y - 8, lineaNota);
                    y -= 12;
                }
                y -= 8;
            }

            // Detalle
            if (rep.detalle != null) {
                if (rep.tituloDetalle != null) {
                    if (y - 60 < pie) {
                        cs.close();
                        pagina = new PDPage(tamano);
                        doc.addPage(pagina);
                        paginas.add(pagina);
                        cs = new PDPageContentStream(doc, pagina);
                        dibujarBandaCompacta(cs, f, ancho, alto, margen, rep.titulo, nombrePersona(rep));
                        y = alto - 46 - 16;
                    }
                    escribir(cs, f.negrita, 11f, TEXTO, margen, y - 12, f.preparar(rep.tituloDetalle));
                    y -= 22;
                }
                int colEstado = columnaEstado(h);
                boolean[] numericas = columnasNumericas(d, h.length);
                float[] anchos = calcularAnchos(h, d, f, tamTexto, tamTexto, anchoUtil, colEstado);
                y = dibujarCabecera(cs, f, h, anchos, numericas, colEstado, margen, y, anchoUtil, altoCabecera, tamTexto);
                if (d.length == 0) {
                    escribir(cs, f.normal, 9.5f, TEXTO_SUAVE, margen + 8, y - 20, f.preparar(rep.sinDetalle));
                    y -= 30;
                }
                for (int r = 0; r < d.length; r++) {
                    if (y - altoFila < pie) {
                        cs.close();
                        pagina = new PDPage(tamano);
                        doc.addPage(pagina);
                        paginas.add(pagina);
                        cs = new PDPageContentStream(doc, pagina);
                        dibujarBandaCompacta(cs, f, ancho, alto, margen, rep.titulo, nombrePersona(rep));
                        y = alto - 46 - 16;
                        y = dibujarCabecera(cs, f, h, anchos, numericas, colEstado, margen, y, anchoUtil, altoCabecera, tamTexto);
                    }
                    float yFila = y - altoFila;
                    dibujarFila(cs, f, d[r], r, anchos, numericas, colEstado, margen, yFila, anchoUtil, altoFila, tamTexto);
                    y = yFila;
                }
            }

            // Firmas
            if (rep.firmas) {
                if (y - 110 < pie) {
                    cs.close();
                    pagina = new PDPage(tamano);
                    doc.addPage(pagina);
                    paginas.add(pagina);
                    cs = new PDPageContentStream(doc, pagina);
                    dibujarBandaCompacta(cs, f, ancho, alto, margen, rep.titulo, nombrePersona(rep));
                    y = alto - 46 - 16;
                }
                float yF = Math.max(pie + 40, y - 90);
                float anchoFirma = 190f;
                float x1 = margen + 30, x2 = margen + anchoUtil - 30 - anchoFirma;
                linea(cs, TEXTO_SUAVE, x1, yF, x1 + anchoFirma, yF, 0.7f);
                linea(cs, TEXTO_SUAVE, x2, yF, x2 + anchoFirma, yF, 0.7f);
                String t1 = f.preparar("Recursos Humanos");
                String t2 = f.preparar(nombrePersona(rep) != null ? nombrePersona(rep) : "Trabajador");
                escribir(cs, f.negrita, 8.5f, TEXTO, x1 + (anchoFirma - ancho(f.negrita, 8.5f, t1)) / 2, yF - 13, t1);
                escribir(cs, f.negrita, 8.5f, TEXTO, x2 + (anchoFirma - ancho(f.negrita, 8.5f, t2)) / 2, yF - 13, t2);
                String s1 = f.preparar("Instituto Americano"), s2 = f.preparar("Trabajador");
                escribir(cs, f.normal, 7.5f, TEXTO_SUAVE, x1 + (anchoFirma - ancho(f.normal, 7.5f, s1)) / 2, yF - 24, s1);
                escribir(cs, f.normal, 7.5f, TEXTO_SUAVE, x2 + (anchoFirma - ancho(f.normal, 7.5f, s2)) / 2, yF - 24, s2);
            }
            cs.close();

            dibujarPies(doc, paginas, f, ancho, margen, generado);
            doc.getDocumentInformation().setTitle(f.preparar(rep.titulo + (nombrePersona(rep) != null ? " - " + nombrePersona(rep) : "")));
            doc.getDocumentInformation().setAuthor("Instituto Americano");
            doc.getDocumentInformation().setCreator(SISTEMA);
            doc.save(archivo);
        }
    }

    private static String nombrePersona(ReporteIndividual rep) {
        return rep.persona == null ? null : rep.persona.get("Nombre");
    }

    private static float dibujarFichaPersona(PDPageContentStream cs, Fuentes f, Map<String, String> persona,
            float margen, float y, float anchoUtil) throws IOException {
        if (persona == null || persona.isEmpty()) {
            return y;
        }
        float hF = 64f;
        float yF = y - hF;
        redondeado(cs, Color.WHITE, margen, yF, anchoUtil, hF, 8);
        cs.setStrokingColor(LINEA);
        cs.setLineWidth(0.8f);
        cs.addRect(margen, yF, anchoUtil, hF);
        cs.stroke();

        // Círculo con iniciales
        String nombre = persona.getOrDefault("Nombre", "");
        float r = 20f, cx = margen + 16 + r, cy = yF + hF / 2f;
        redondeado(cs, AZUL, cx - r, cy - r, 2 * r, 2 * r, r);
        String ini = f.preparar(iniciales(nombre));
        escribir(cs, f.negrita, 13f, Color.WHITE, cx - ancho(f.negrita, 13f, ini) / 2f, cy - 4.5f, ini);

        float xT = cx + r + 14;
        escribir(cs, f.negrita, 15f, TEXTO, xT, yF + hF - 26, recortar(f.preparar(nombre), anchoUtil * 0.55f, f.negrita, 15f, f.puntos()));

        // Datos en una línea: "DNI 72960206  ·  DOCENTE  ·  Profesor de Química"
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : persona.entrySet()) {
            if (e.getKey().equals("Nombre") || e.getValue() == null || e.getValue().isBlank()) continue;
            if (sb.length() > 0) sb.append("   ·   ");
            sb.append(e.getKey()).append(": ").append(e.getValue());
        }
        escribir(cs, f.normal, 8.5f, TEXTO_SUAVE, xT, yF + 15,
                recortar(f.preparar(sb.toString()), anchoUtil - (xT - margen) - 12, f.normal, 8.5f, f.puntos()));
        return yF;
    }

    private static float dibujarSecciones(PDPageContentStream cs, Fuentes f, Map<String, List<String[]>> secciones,
            float margen, float y, float anchoUtil) throws IOException {
        int n = secciones.size();
        float sep = 12f;
        float anchoS = (anchoUtil - sep * (n - 1)) / n;
        int maxFilas = 0;
        for (List<String[]> filas : secciones.values()) maxFilas = Math.max(maxFilas, filas.size());
        float altoFila = 18f, altoTitulo = 24f;
        float hS = altoTitulo + maxFilas * altoFila + 10;
        float x = margen;
        for (Map.Entry<String, List<String[]>> s : secciones.entrySet()) {
            float yS = y - hS;
            redondeado(cs, FONDO_TARJETA, x, yS, anchoS, hS, 6);
            cs.setCharacterSpacing(0.8f);
            escribir(cs, f.negrita, 8f, AZUL, x + 12, y - 16, f.preparar(s.getKey().toUpperCase(new Locale("es"))));
            cs.setCharacterSpacing(0f);
            float yL = y - altoTitulo;
            int i = 0;
            for (String[] fila : s.getValue()) {
                boolean total = fila[0].toLowerCase(Locale.ROOT).startsWith("total");
                if (total) {
                    linea(cs, BORDE_SECCION, x + 12, yL - 2, x + anchoS - 12, yL - 2, 0.6f);
                }
                PDFont fu = total ? f.negrita : f.normal;
                escribir(cs, fu, 8.5f, total ? TEXTO : TEXTO_SUAVE, x + 12, yL - 13, f.preparar(fila[0]));
                String v = f.preparar(fila[1]);
                escribir(cs, total ? f.negrita : f.normal, 9f, TEXTO, x + anchoS - 12 - ancho(fu, 9f, v), yL - 13, v);
                yL -= altoFila;
                i++;
            }
            x += anchoS + sep;
        }
        return y - hS;
    }

    private static final Color BORDE_SECCION = new Color(203, 213, 225);

    private static String iniciales(String nombre) {
        StringBuilder sb = new StringBuilder();
        for (String p : nombre.trim().split("\\s+")) {
            if (!p.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(p.charAt(0)));
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    private static List<String> partirTexto(String texto, PDFont fuente, float tam, float anchoMax) throws IOException {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (ancho(fuente, tam, prueba) > anchoMax && actual.length() > 0) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (actual.length() > 0) lineas.add(actual.toString());
        return lineas;
    }

    private static void dibujarBanda(PDPageContentStream cs, Fuentes f, float ancho, float alto, float margen,
            float altoBanda, String titulo, String subtitulo, String generado) throws IOException {
        String sub = subtitulo != null && !subtitulo.isBlank() ? f.preparar(subtitulo) : null;
        String g = f.preparar("Generado: " + generado);
        String tit = f.preparar(titulo);

        // Ancho del bloque derecho (periodo y fecha) y espacio que queda para el título
        float anchoDerecha = Math.max(sub != null ? ancho(f.negrita, 9.5f, sub) : 0, ancho(f.normal, 8f, g));
        float cajaDerecha = Math.max(ancho * 0.30f, anchoDerecha + margen + 24);
        float inicioCaja = ancho - cajaDerecha;
        float disponible = inicioCaja - margen - 24;

        // Si el título no entra, se reduce el tamaño de la letra (mínimo 13 pt)
        float tamTitulo = 19f;
        while (tamTitulo > 13f && ancho(f.negrita, tamTitulo, tit) > disponible) {
            tamTitulo -= 0.5f;
        }
        tit = recortar(tit, disponible, f.negrita, tamTitulo, f.puntos());

        rect(cs, AZUL_OSCURO, 0, alto - altoBanda, ancho, altoBanda);
        rect(cs, AZUL, inicioCaja, alto - altoBanda, cajaDerecha, altoBanda);
        rect(cs, DORADO, 0, alto - altoBanda - 3, ancho, 3);

        cs.setCharacterSpacing(1.6f);
        escribir(cs, f.negrita, 8.5f, DORADO, margen, alto - 26, f.preparar(INSTITUCION));
        cs.setCharacterSpacing(0f);
        escribir(cs, f.negrita, tamTitulo, Color.WHITE, margen, alto - 52, tit);
        escribir(cs, f.normal, 8.5f, new Color(190, 208, 230), margen, alto - 67, f.preparar(SISTEMA));

        float derecha = ancho - margen;
        if (sub != null) {
            escribir(cs, f.negrita, 9.5f, Color.WHITE, derecha - ancho(f.negrita, 9.5f, sub), alto - 40, sub);
        }
        escribir(cs, f.normal, 8f, new Color(214, 228, 245), derecha - ancho(f.normal, 8f, g), alto - 56, g);
    }

    private static void dibujarBandaCompacta(PDPageContentStream cs, Fuentes f, float ancho, float alto, float margen,
            String titulo, String subtitulo) throws IOException {
        rect(cs, AZUL_OSCURO, 0, alto - 46, ancho, 46);
        rect(cs, DORADO, 0, alto - 48, ancho, 2);
        escribir(cs, f.negrita, 12f, Color.WHITE, margen, alto - 28, f.preparar(titulo));
        if (subtitulo != null && !subtitulo.isBlank()) {
            String s = f.preparar(subtitulo);
            escribir(cs, f.normal, 8.5f, new Color(214, 228, 245), ancho - margen - ancho(f.normal, 8.5f, s), alto - 28, s);
        }
    }

    private static float dibujarResumen(PDPageContentStream cs, Fuentes f, Map<String, String> res,
            float margen, float y, float anchoUtil) throws IOException {
        int n = res.size();
        float sep = 12f, altoT = 48f;
        float anchoT = Math.min(170f, (anchoUtil - sep * (n - 1)) / n);
        float x = margen;
        float yT = y - altoT;
        for (Map.Entry<String, String> e : res.entrySet()) {
            Color acento = AZUL;
            String k = e.getKey();
            if (k.equals("Presentes")) acento = VERDE;
            else if (k.equals("Tardanzas")) acento = AMBAR;
            else if (k.equals("Ausentes") || k.equals("Faltas") || k.equals("En alerta")) acento = ROJO;
            else if (k.startsWith("Total")) acento = DORADO;
            else if (k.equals("Situación")) acento = e.getValue().equalsIgnoreCase("EN ALERTA") ? ROJO : VERDE;

            redondeado(cs, FONDO_TARJETA, x, yT, anchoT, altoT, 6);
            rect(cs, acento, x, yT + 8, 3, altoT - 16);
            escribir(cs, f.normal, 7.5f, TEXTO_SUAVE, x + 14, yT + altoT - 16, f.preparar(k.toUpperCase(new Locale("es"))));
            escribir(cs, f.negrita, 16f, TEXTO, x + 14, yT + 11, f.preparar(e.getValue()));
            x += anchoT + sep;
        }
        return yT;
    }

    private static float dibujarCabecera(PDPageContentStream cs, Fuentes f, String[] h, float[] anchos,
            boolean[] numericas, int colEstado, float margen, float y, float anchoUtil, float altoCab, float tam)
            throws IOException {
        float yC = y - altoCab;
        redondeado(cs, AZUL, margen, yC, anchoUtil, altoCab, 4);
        float x = margen;
        for (int c = 0; c < h.length; c++) {
            String t = recortar(f.preparar(h[c]), anchos[c] - 12, f.negrita, tam, f.puntos());
            float tx = numericas[c] ? x + anchos[c] - 6 - ancho(f.negrita, tam, t) : x + 6;
            escribir(cs, f.negrita, tam, Color.WHITE, tx, yC + (altoCab - tam) / 2f + 1.5f, t);
            x += anchos[c];
        }
        return yC;
    }

    private static void dibujarEtiqueta(PDPageContentStream cs, Fuentes f, String valor, float x, float yFila,
            float altoFila, float maxAncho) throws IOException {
        float tam = 7f;
        String t = recortar(f.preparar(valor.toUpperCase(Locale.ROOT)), maxAncho - 12, f.negrita, tam, f.puntos());
        float w = Math.min(maxAncho, ancho(f.negrita, tam, t) + 14);
        float h = 13f;
        float yE = yFila + (altoFila - h) / 2f;
        redondeado(cs, colorEstado(valor, true), x, yE, w, h, h / 2f);
        escribir(cs, f.negrita, tam, colorEstado(valor, false), x + 7, yE + 3.8f, t);
    }

    /** Ancho de columnas según su contenido, ajustado al ancho de la página. */
    private static float[] calcularAnchos(String[] h, String[][] d, Fuentes f, float tamTexto, float tamCab,
            float anchoUtil, int colEstado) throws IOException {
        int n = h.length;
        float[] w = new float[n];
        for (int c = 0; c < n; c++) {
            float max = ancho(f.negrita, tamCab, f.preparar(h[c]));
            int muestra = Math.min(d.length, 300);
            for (int r = 0; r < muestra; r++) {
                PDFont fuente = c == colEstado ? f.negrita : f.normal;
                float tam = c == colEstado ? 7f : tamTexto;
                float extra = c == colEstado ? 14 : 0;
                max = Math.max(max, ancho(fuente, tam, f.preparar(c == colEstado ? d[r][c].toUpperCase(Locale.ROOT) : d[r][c])) + extra);
            }
            w[c] = Math.min(max + 14, 190);
        }
        float total = 0;
        for (float v : w) total += v;
        float factor = anchoUtil / total;
        for (int c = 0; c < n; c++) w[c] *= factor;
        return w;
    }

    // ---------- Primitivas de dibujo PDF ----------
    private static float ancho(PDFont fuente, float tam, String texto) throws IOException {
        try {
            return fuente.getStringWidth(texto) / 1000f * tam;
        } catch (IllegalArgumentException e) {
            return fuente.getStringWidth(quitarTildes(texto)) / 1000f * tam;
        }
    }

    private static void escribir(PDPageContentStream cs, PDFont fuente, float tam, Color color, float x, float y,
            String texto) throws IOException {
        cs.setNonStrokingColor(color);
        cs.beginText();
        cs.setFont(fuente, tam);
        cs.newLineAtOffset(x, y);
        try {
            cs.showText(texto);
        } catch (IllegalArgumentException e) {
            cs.showText(quitarTildes(texto).replaceAll("[^\\x20-\\x7E]", "?"));
        }
        cs.endText();
    }

    private static void rect(PDPageContentStream cs, Color color, float x, float y, float w, float h) throws IOException {
        cs.setNonStrokingColor(color);
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    private static void linea(PDPageContentStream cs, Color color, float x1, float y1, float x2, float y2, float grosor)
            throws IOException {
        cs.setStrokingColor(color);
        cs.setLineWidth(grosor);
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    private static void redondeado(PDPageContentStream cs, Color color, float x, float y, float w, float h, float r)
            throws IOException {
        r = Math.min(r, Math.min(w, h) / 2f);
        float k = 0.5523f * r;
        cs.setNonStrokingColor(color);
        cs.moveTo(x + r, y);
        cs.lineTo(x + w - r, y);
        cs.curveTo(x + w - r + k, y, x + w, y + r - k, x + w, y + r);
        cs.lineTo(x + w, y + h - r);
        cs.curveTo(x + w, y + h - r + k, x + w - r + k, y + h, x + w - r, y + h);
        cs.lineTo(x + r, y + h);
        cs.curveTo(x + r - k, y + h, x, y + h - r + k, x, y + h - r);
        cs.lineTo(x, y + r);
        cs.curveTo(x, y + r - k, x + r - k, y, x + r, y);
        cs.closePath();
        cs.fill();
    }

    // Recorta el texto (con "…") si no entra en el ancho disponible de la columna
    private static String recortar(String texto, float anchoDisponible, PDFont fuente, float tamano, String puntos)
            throws IOException {
        if (texto == null) {
            return "";
        }
        if (ancho(fuente, tamano, texto) <= anchoDisponible) {
            return texto;
        }
        String recorte = texto;
        while (recorte.length() > 1 && ancho(fuente, tamano, recorte + puntos) > anchoDisponible) {
            recorte = recorte.substring(0, recorte.length() - 1);
        }
        return recorte.trim() + puntos;
    }

    // Las fuentes estándar (Helvetica) no incluyen tildes/eñe; se usa solo como respaldo
    private static String quitarTildes(String texto) {
        if (texto == null) {
            return "";
        }
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    // =====================================================================
    // Excel
    // =====================================================================

    /** Genera el Excel del reporte en el archivo indicado (sin diálogos). */
    public static void generarExcel(JTable tabla, String titulo, String subtitulo, File archivo) throws IOException {
        String[] h = encabezados(tabla);
        String[][] d = datos(tabla);
        int columnas = Math.max(1, h.length);
        int colEstado = columnaEstado(h);
        boolean[] numericas = columnasNumericas(d, h.length);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet hoja = wb.createSheet("Reporte");
            hoja.setDisplayGridlines(false);

            // Estilos
            XSSFFont fInst = wb.createFont();
            fInst.setBold(true);
            fInst.setFontHeightInPoints((short) 9);
            fInst.setColor(new XSSFColor(DORADO, null));
            XSSFCellStyle sInst = estiloRelleno(wb, AZUL_OSCURO);
            sInst.setFont(fInst);

            XSSFFont fTitulo = wb.createFont();
            fTitulo.setBold(true);
            fTitulo.setFontHeightInPoints((short) 16);
            fTitulo.setColor(new XSSFColor(Color.WHITE, null));
            XSSFCellStyle sTitulo = estiloRelleno(wb, AZUL_OSCURO);
            sTitulo.setFont(fTitulo);
            sTitulo.setVerticalAlignment(VerticalAlignment.CENTER);

            XSSFFont fSub = wb.createFont();
            fSub.setFontHeightInPoints((short) 9);
            fSub.setColor(new XSSFColor(new Color(214, 228, 245), null));
            XSSFCellStyle sSub = estiloRelleno(wb, AZUL_OSCURO);
            sSub.setFont(fSub);

            XSSFFont fCab = wb.createFont();
            fCab.setBold(true);
            fCab.setColor(new XSSFColor(Color.WHITE, null));
            XSSFCellStyle sCab = estiloRelleno(wb, AZUL);
            sCab.setFont(fCab);
            sCab.setVerticalAlignment(VerticalAlignment.CENTER);
            bordes(sCab, AZUL);
            XSSFCellStyle sCabDer = wb.createCellStyle();
            sCabDer.cloneStyleFrom(sCab);
            sCabDer.setAlignment(HorizontalAlignment.RIGHT);

            XSSFCellStyle[] sTexto = new XSSFCellStyle[2], sNum = new XSSFCellStyle[2], sMon = new XSSFCellStyle[2], sDec = new XSSFCellStyle[2];
            short fmtMoneda = wb.createDataFormat().getFormat("\"S/\" #,##0.00");
            short fmtDec = wb.createDataFormat().getFormat("0.00");
            for (int i = 0; i < 2; i++) {
                Color fondo = i == 0 ? Color.WHITE : FILA_ALTERNA;
                sTexto[i] = estiloRelleno(wb, fondo);
                bordes(sTexto[i], LINEA);
                sTexto[i].setVerticalAlignment(VerticalAlignment.CENTER);
                sNum[i] = wb.createCellStyle();
                sNum[i].cloneStyleFrom(sTexto[i]);
                sNum[i].setAlignment(HorizontalAlignment.RIGHT);
                sMon[i] = wb.createCellStyle();
                sMon[i].cloneStyleFrom(sNum[i]);
                sMon[i].setDataFormat(fmtMoneda);
                sDec[i] = wb.createCellStyle();
                sDec[i].cloneStyleFrom(sNum[i]);
                sDec[i].setDataFormat(fmtDec);
            }
            Map<String, XSSFCellStyle> sEstado = new LinkedHashMap<>();

            // Banda de título (filas 0 a 2)
            Row r0 = hoja.createRow(0);
            Row r1 = hoja.createRow(1);
            Row r2 = hoja.createRow(2);
            r0.setHeightInPoints(20);
            r1.setHeightInPoints(28);
            r2.setHeightInPoints(18);
            for (int c = 0; c < columnas; c++) {
                r0.createCell(c).setCellStyle(sInst);
                r1.createCell(c).setCellStyle(sTitulo);
                r2.createCell(c).setCellStyle(sSub);
            }
            r0.getCell(0).setCellValue(INSTITUCION);
            r1.getCell(0).setCellValue(titulo);
            String generado = "Generado: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            r2.getCell(0).setCellValue((subtitulo != null && !subtitulo.isBlank() ? subtitulo + "   ·   " : "") + generado
                    + "   ·   Registros: " + d.length);
            if (columnas > 1) {
                hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, columnas - 1));
                hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, columnas - 1));
                hoja.addMergedRegion(new CellRangeAddress(2, 2, 0, columnas - 1));
            }

            // Cabecera de la tabla (fila 4)
            int filaCab = 4;
            Row cab = hoja.createRow(filaCab);
            cab.setHeightInPoints(22);
            for (int c = 0; c < h.length; c++) {
                Cell cell = cab.createCell(c);
                cell.setCellValue(h[c]);
                cell.setCellStyle(numericas[c] ? sCabDer : sCab);
            }

            // Datos
            for (int r = 0; r < d.length; r++) {
                Row fila = hoja.createRow(filaCab + 1 + r);
                fila.setHeightInPoints(18);
                int alt = r % 2;
                for (int c = 0; c < h.length; c++) {
                    Cell cell = fila.createCell(c);
                    String v = d[r][c];
                    if (c == colEstado && !v.isEmpty()) {
                        cell.setCellValue(v);
                        cell.setCellStyle(sEstado.computeIfAbsent(v.toUpperCase(Locale.ROOT) + alt, k -> {
                            XSSFCellStyle s = wb.createCellStyle();
                            s.cloneStyleFrom(sTexto[alt]);
                            s.setFillForegroundColor(new XSSFColor(colorEstado(v, true), null));
                            s.setAlignment(HorizontalAlignment.CENTER);
                            XSSFFont fe = wb.createFont();
                            fe.setBold(true);
                            fe.setFontHeightInPoints((short) 9);
                            fe.setColor(new XSSFColor(colorEstado(v, false), null));
                            s.setFont(fe);
                            return s;
                        }));
                    } else if (esMoneda(v)) {
                        cell.setCellValue(valorMoneda(v));
                        cell.setCellStyle(sMon[alt]);
                    } else if (numericas[c] && ENTERO.matcher(v).matches()) {
                        cell.setCellValue(Long.parseLong(v));
                        cell.setCellStyle(sNum[alt]);
                    } else if (numericas[c] && DECIMAL.matcher(v).matches()) {
                        cell.setCellValue(Double.parseDouble(v));
                        cell.setCellStyle(sDec[alt]);
                    } else {
                        cell.setCellValue(v);
                        cell.setCellStyle(sTexto[alt]);
                    }
                }
            }

            if (h.length > 0) {
                hoja.setAutoFilter(new CellRangeAddress(filaCab, filaCab + Math.max(d.length, 1), 0, h.length - 1));
            }
            hoja.createFreezePane(0, filaCab + 1);
            for (int c = 0; c < h.length; c++) {
                hoja.autoSizeColumn(c);
                hoja.setColumnWidth(c, Math.min(255 * 256, Math.max(hoja.getColumnWidth(c) + 3 * 256, 12 * 256)));
            }
            hoja.getPrintSetup().setLandscape(h.length > 6);
            hoja.setFitToPage(true);
            hoja.getPrintSetup().setFitWidth((short) 1);
            hoja.getPrintSetup().setFitHeight((short) 0);

            try (FileOutputStream out = new FileOutputStream(archivo)) {
                wb.write(out);
            }
        }
    }

    private static XSSFCellStyle estiloRelleno(XSSFWorkbook wb, Color color) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(color, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private static void bordes(XSSFCellStyle s, Color color) {
        XSSFColor c = new XSSFColor(color, null);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBottomBorderColor(c);
        s.setBorderTop(BorderStyle.THIN);
        s.setTopBorderColor(c);
        s.setBorderLeft(BorderStyle.THIN);
        s.setLeftBorderColor(c);
        s.setBorderRight(BorderStyle.THIN);
        s.setRightBorderColor(c);
    }
}
