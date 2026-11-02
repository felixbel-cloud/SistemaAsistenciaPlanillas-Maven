package util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SeguridadTest {

    @Test
    void elHashVerificaLaContrasenaCorrectaYRechazaLaIncorrecta() {
        String hash = Seguridad.hashContrasena("admin123");
        assertTrue(Seguridad.esHash(hash));
        assertTrue(Seguridad.verificar("admin123", hash));
        assertFalse(Seguridad.verificar("Admin123", hash));
        assertFalse(Seguridad.verificar("", hash));
    }

    @Test
    void dosHashesDeLaMismaContrasenaSonDistintosPorLaSal() {
        String h1 = Seguridad.hashContrasena("rrhh123");
        String h2 = Seguridad.hashContrasena("rrhh123");
        assertFalse(h1.equals(h2));
        assertTrue(Seguridad.verificar("rrhh123", h1));
        assertTrue(Seguridad.verificar("rrhh123", h2));
    }

    @Test
    void aceptaContrasenasAntiguasEnTextoPlanoParaMigrarlas() {
        assertFalse(Seguridad.esHash("oper123"));
        assertTrue(Seguridad.verificar("oper123", "oper123"));
        assertFalse(Seguridad.verificar("otra", "oper123"));
    }
}
