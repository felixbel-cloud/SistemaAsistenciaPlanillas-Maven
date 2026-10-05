package util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class ValidacionesTest {
    @Test
    void aceptaCamposPersonalesYContactoValidos() {
        assertAll(
            () -> assertTrue(Validaciones.validarCampoVacio(new JTextField("Instituto"), "Instituto")),
            () -> assertTrue(Validaciones.validarDNI("12345678")),
            () -> assertTrue(Validaciones.validarSoloLetras("José Núñez", "Nombre")),
            () -> assertTrue(Validaciones.validarEmail("nombre.apellido+test@example.com")),
            () -> assertTrue(Validaciones.validarLongitudMinima("clave-segura", 8, "Clave"))
        );
    }

    @Test
    void aceptaNumerosHorasYPeriodosDentroDeRango() {
        assertAll(
            () -> assertTrue(Validaciones.validarDecimalPositivo("0", "Salario")),
            () -> assertTrue(Validaciones.validarDecimalPositivo("1250.50", "Salario")),
            () -> assertTrue(Validaciones.validarFormatoHora("00:00", "Entrada")),
            () -> assertTrue(Validaciones.validarFormatoHora("8:30", "Entrada")),
            () -> assertTrue(Validaciones.validarFormatoHora("23:59", "Salida")),
            () -> assertTrue(Validaciones.validarFormatoPeriodo("2020-01")),
            () -> assertTrue(Validaciones.validarFormatoPeriodo("2030-12"))
        );
    }
}