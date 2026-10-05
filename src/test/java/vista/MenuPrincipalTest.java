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

}
