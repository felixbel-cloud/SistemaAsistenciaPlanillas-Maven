// util/ExportadorReportes.java
package util;

import javax.swing.JTable;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class ExportadorReportes {
    
    // Exportar tabla a Excel
    public static void exportarAExcel(JTable tabla, String nombreArchivo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar reporte en Excel");
        fileChooser.setSelectedFile(new File(nombreArchivo + ".xlsx"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx");
        fileChooser.setFileFilter(filter);
        
        int resultado = fileChooser.showSaveDialog(null);
        
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Reporte");
                
                // Crear estilo para encabezados
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                
                // Crear fila de encabezados
                Row headerRow = sheet.createRow(0);
                for (int col = 0; col < tabla.getColumnCount(); col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(tabla.getColumnName(col));
                    cell.setCellStyle(headerStyle);
                }
                
                // Llenar datos
                for (int row = 0; row < tabla.getRowCount(); row++) {
                    Row dataRow = sheet.createRow(row + 1);
                    for (int col = 0; col < tabla.getColumnCount(); col++) {
                        Cell cell = dataRow.createCell(col);
                        Object value = tabla.getValueAt(row, col);
                        if (value != null) {
                            cell.setCellValue(value.toString());
                        }
                    }
                }
                
                // Ajustar ancho de columnas
                for (int col = 0; col < tabla.getColumnCount(); col++) {
                    sheet.autoSizeColumn(col);
                }
                
                // Guardar archivo
                try (FileOutputStream fileOut = new FileOutputStream(archivo)) {
                    workbook.write(fileOut);
                }
                
                JOptionPane.showMessageDialog(null,
                    "Reporte exportado exitosamente a:\n" + archivo.getAbsolutePath(),
                    "Exportación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Error al exportar a Excel: " + e.getMessage(),
                    "Error de exportación",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Exportar tabla a PDF
    public static void exportarAPDF(JTable tabla, String tituloReporte, String nombreArchivo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar reporte en PDF");
        fileChooser.setSelectedFile(new File(nombreArchivo + ".pdf"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos PDF (*.pdf)", "pdf");
        fileChooser.setFileFilter(filter);

        int resultado = fileChooser.showSaveDialog(null);

        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = fileChooser.getSelectedFile();

        int numColumnas = tabla.getColumnCount();
        int numFilas = tabla.getRowCount();

        // Si hay muchas columnas, se usa orientación horizontal para que quepan mejor
        PDRectangle tamanoPagina = numColumnas > 6
                ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())
                : PDRectangle.A4;

        float margen = 35f;
        float anchoUtil = tamanoPagina.getWidth() - margen * 2;
        float anchoColumna = numColumnas > 0 ? anchoUtil / numColumnas : anchoUtil;
        float altoFila = 16f;
        float yInicio = tamanoPagina.getHeight() - margen;

        PDFont fuenteNormal = PDType1Font.HELVETICA;
        PDFont fuenteNegrita = PDType1Font.HELVETICA_BOLD;

        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(tamanoPagina);
            documento.addPage(pagina);
            PDPageContentStream cs = new PDPageContentStream(documento, pagina);

            float y = yInicio;

            // Título del reporte
            cs.setFont(fuenteNegrita, 14);
            cs.beginText();
            cs.newLineAtOffset(margen, y);
            cs.showText(quitarTildes(tituloReporte));
            cs.endText();
            y -= 18;

            cs.setFont(fuenteNormal, 8);
            cs.beginText();
            cs.newLineAtOffset(margen, y);
            cs.showText("Instituto Americano - Generado: "
                    + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            cs.endText();
            y -= 18;

            y = dibujarEncabezados(cs, tabla, margen, y, anchoColumna, anchoUtil, fuenteNegrita);
            y -= altoFila;

            cs.setFont(fuenteNormal, 8);

            for (int fila = 0; fila < numFilas; fila++) {
                if (y < margen + altoFila) {
                    cs.close();
                    pagina = new PDPage(tamanoPagina);
                    documento.addPage(pagina);
                    cs = new PDPageContentStream(documento, pagina);
                    y = yInicio;
                    y = dibujarEncabezados(cs, tabla, margen, y, anchoColumna, anchoUtil, fuenteNegrita);
                    y -= altoFila;
                    cs.setFont(fuenteNormal, 8);
                }

                float x = margen;
                for (int col = 0; col < numColumnas; col++) {
                    Object valor = tabla.getValueAt(fila, col);
                    String texto = valor != null ? quitarTildes(valor.toString()) : "";
                    texto = recortarTexto(texto, anchoColumna - 4, fuenteNormal, 8);

                    cs.beginText();
                    cs.newLineAtOffset(x + 2, y);
                    cs.showText(texto);
                    cs.endText();

                    x += anchoColumna;
                }
                y -= altoFila;
            }

            cs.close();
            documento.save(archivo);

            JOptionPane.showMessageDialog(null,
                    "Reporte exportado exitosamente a:\n" + archivo.getAbsolutePath(),
                    "Exportación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error al exportar a PDF: " + e.getMessage(),
                    "Error de exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static float dibujarEncabezados(PDPageContentStream cs, JTable tabla, float margen, float y,
            float anchoColumna, float anchoUtil, PDFont fuenteNegrita) throws IOException {
        cs.setFont(fuenteNegrita, 8);
        float x = margen;
        for (int col = 0; col < tabla.getColumnCount(); col++) {
            String texto = recortarTexto(quitarTildes(tabla.getColumnName(col)), anchoColumna - 4, fuenteNegrita, 8);
            cs.beginText();
            cs.newLineAtOffset(x + 2, y);
            cs.showText(texto);
            cs.endText();
            x += anchoColumna;
        }
        y -= 4;
        cs.setLineWidth(0.5f);
        cs.moveTo(margen, y);
        cs.lineTo(margen + anchoUtil, y);
        cs.stroke();
        return y;
    }

    // Recorta el texto (con "...") si no entra en el ancho disponible de la columna
    private static String recortarTexto(String texto, float anchoDisponible, PDFont fuente, float tamano)
            throws IOException {
        if (texto == null) {
            return "";
        }
        if (fuente.getStringWidth(texto) / 1000f * tamano <= anchoDisponible) {
            return texto;
        }
        String recorte = texto;
        while (recorte.length() > 1
                && fuente.getStringWidth(recorte + "...") / 1000f * tamano > anchoDisponible) {
            recorte = recorte.substring(0, recorte.length() - 1);
        }
        return recorte + "...";
    }

    // Los fonts estándar (Helvetica) no incluyen tildes/eñe en su codificación por
    // defecto, así que se normaliza el texto para evitar errores al exportar a PDF
    private static String quitarTildes(String texto) {
        if (texto == null) {
            return "";
        }
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
    
    // Método para formatear moneda
    public static String formatearMoneda(double monto) {
        return String.format("S/ %.2f", monto);
    }
    
    // Método para formatear porcentaje
    public static String formatearPorcentaje(double porcentaje) {
        return String.format("%.2f%%", porcentaje);
    }
}
