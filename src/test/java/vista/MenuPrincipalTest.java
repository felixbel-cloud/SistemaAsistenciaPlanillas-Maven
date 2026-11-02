package vista;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class MenuPrincipalTest {

    @Test
    void debeRespetarPermisosPorRol() {
        assertTrue(MenuPrincipal.puedeAcceder(1, "GESTION_PERSONAL"));
        assertTrue(MenuPrincipal.puedeAcceder(1, "REGISTRO_ASISTENCIA"));
        assertTrue(MenuPrincipal.puedeAcceder(1, "CONSULTA_ASISTENCIA"));

        assertTrue(MenuPrincipal.puedeAcceder(2, "GESTION_PERSONAL"));
        assertTrue(MenuPrincipal.puedeAcceder(2, "CONSULTA_ASISTENCIA"));
        assertFalse(MenuPrincipal.puedeAcceder(2, "REGISTRO_ASISTENCIA"));

        assertTrue(MenuPrincipal.puedeAcceder(3, "REGISTRO_ASISTENCIA"));
        assertFalse(MenuPrincipal.puedeAcceder(3, "CONSULTA_ASISTENCIA"));
        assertFalse(MenuPrincipal.puedeAcceder(3, "GESTION_PERSONAL"));
    }

    @Test
    void debeMostrarPagoPorHoraSoloParaDocentes() {
        assertTrue(GestionPersonal.requierePagoPorHora("DOCENTE"));
        assertFalse(GestionPersonal.requierePagoPorHora("ADMINISTRATIVO"));
    }

    @Test
    void debeOcultarMenusQueNoCorrespondenAlRol() {
        assertTrue(MenuPrincipal.esMenuVisibleParaRol(1, "ASISTENCIA"));
        assertTrue(MenuPrincipal.esMenuVisibleParaRol(1, "PLANILLA"));
        assertTrue(MenuPrincipal.esMenuVisibleParaRol(1, "MANTENIMIENTO"));

        assertTrue(MenuPrincipal.esMenuVisibleParaRol(2, "ASISTENCIA"));
        assertFalse(MenuPrincipal.esMenuVisibleParaRol(2, "REGISTRO_ASISTENCIA"));
        assertTrue(MenuPrincipal.esMenuVisibleParaRol(2, "PLANILLA"));
        assertTrue(MenuPrincipal.esMenuVisibleParaRol(2, "MANTENIMIENTO"));

        assertTrue(MenuPrincipal.esMenuVisibleParaRol(3, "ASISTENCIA"));
        assertFalse(MenuPrincipal.esMenuVisibleParaRol(3, "PLANILLA"));
        assertFalse(MenuPrincipal.esMenuVisibleParaRol(3, "REPORTES"));
        assertFalse(MenuPrincipal.esMenuVisibleParaRol(3, "MANTENIMIENTO"));
    }

    @Test
    void debeMostrarSoloTarjetasPermitidasSegunRol() {
        String[] tarjetas = {
            "Registrar Asistencia",
            "Consultar Asistencia",
            "Gestión de Planillas",
            "Gestión Personal",
            "Gestión de Horarios",
            "Reportes"
        };

        // Operador (Rol 3): Solo ve 1 tarjeta (Registrar Asistencia)
        int conteoOperador = 0;
        for (String t : tarjetas) {
            if (MenuPrincipal.puedeMostrarTarjeta(3, t)) conteoOperador++;
        }
        assertEquals(1, conteoOperador);
        assertTrue(MenuPrincipal.puedeMostrarTarjeta(3, "Registrar Asistencia"));
        assertFalse(MenuPrincipal.puedeMostrarTarjeta(3, "Consultar Asistencia"));
        assertFalse(MenuPrincipal.puedeMostrarTarjeta(3, "Gestión de Planillas"));

        // RRHH (Rol 2): Ve 5 tarjetas (todo excepto Registrar Asistencia)
        int conteoRRHH = 0;
        for (String t : tarjetas) {
            if (MenuPrincipal.puedeMostrarTarjeta(2, t)) conteoRRHH++;
        }
        assertEquals(5, conteoRRHH);
        assertFalse(MenuPrincipal.puedeMostrarTarjeta(2, "Registrar Asistencia"));
        assertTrue(MenuPrincipal.puedeMostrarTarjeta(2, "Consultar Asistencia"));
        assertTrue(MenuPrincipal.puedeMostrarTarjeta(2, "Gestión de Planillas"));

        // Administrador (Rol 1): Ve las 6 tarjetas completas
        int conteoAdmin = 0;
        for (String t : tarjetas) {
            if (MenuPrincipal.puedeMostrarTarjeta(1, t)) conteoAdmin++;
        }
        assertEquals(6, conteoAdmin);
    }
}
