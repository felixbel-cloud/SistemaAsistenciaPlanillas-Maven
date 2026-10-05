// util/Validaciones.java
package util;

import javax.swing.JTextField;
import javax.swing.JOptionPane;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Validaciones {
    
    // Validar que un campo no esté vacío
    public static boolean validarCampoVacio(JTextField campo, String nombreCampo) {
        if (campo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "El campo " + nombreCampo + " es obligatorio",
                "Campo vacío",
                JOptionPane.WARNING_MESSAGE);
            campo.requestFocus();
            return false;
        }
        return true;
    }
    
    // Validar DNI (8 dígitos)
    public static boolean validarDNI(String dni) {
        Pattern pattern = Pattern.compile("^[0-9]{8}$");
        Matcher matcher = pattern.matcher(dni);
        
        if (!matcher.matches()) {
            JOptionPane.showMessageDialog(null,
                "El DNI debe contener exactamente 8 dígitos",
                "DNI inválido",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    // Validar que solo contenga letras
    public static boolean validarSoloLetras(String texto, String nombreCampo) {
        Pattern pattern = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$");
        Matcher matcher = pattern.matcher(texto);
        
        if (!matcher.matches()) {
            JOptionPane.showMessageDialog(null,
                "El campo " + nombreCampo + " solo debe contener letras",
                "Formato inválido",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    // Validar número decimal positivo
    public static boolean validarDecimalPositivo(String texto, String nombreCampo) {
        try {
            double valor = Double.parseDouble(texto);
            if (valor < 0) {
                JOptionPane.showMessageDialog(null,
                    "El campo " + nombreCampo + " debe ser un número positivo",
                    "Valor inválido",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                "El campo " + nombreCampo + " debe ser un número válido",
                "Formato inválido",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    // Validar formato de hora (HH:MM)
    public static boolean validarFormatoHora(String hora, String nombreCampo) {
        Pattern pattern = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
        Matcher matcher = pattern.matcher(hora);
        
        if (!matcher.matches()) {
            JOptionPane.showMessageDialog(null,
                "El campo " + nombreCampo + " debe tener formato HH:MM (ejemplo: 08:00)",
                "Formato inválido",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    // Validar formato de periodo (YYYY-MM)
    public static boolean validarFormatoPeriodo(String periodo) {
        Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}$");
        Matcher matcher = pattern.matcher(periodo);
        
        if (!matcher.matches()) {
            JOptionPane.showMessageDialog(null,
                "El periodo debe tener formato YYYY-MM (ejemplo: 2025-10)",
                "Formato inválido",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            String[] partes = periodo.split("-");
            int anio = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            
            if (anio < 2020 || anio > 2030 || mes < 1 || mes > 12) {
                JOptionPane.showMessageDialog(null,
                    "El periodo debe estar entre 2020-01 y 2030-12",
                    "Periodo inválido",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Validar email
    public static boolean validarEmail(String email) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        Matcher matcher = pattern.matcher(email);
        
        if (!matcher.matches()) {
            JOptionPane.showMessageDialog(null,
                "El formato del email no es válido",
                "Email inválido",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    // Validar longitud mínima
    public static boolean validarLongitudMinima(String texto, int longitudMinima, String nombreCampo) {
        if (texto.length() < longitudMinima) {
            JOptionPane.showMessageDialog(null,
                "El campo " + nombreCampo + " debe tener al menos " + longitudMinima + " caracteres",
                "Longitud insuficiente",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
