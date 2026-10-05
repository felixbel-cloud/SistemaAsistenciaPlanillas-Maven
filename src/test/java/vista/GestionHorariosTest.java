package vista;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import modelo.entidades.HorarioPersonal;
import modelo.entidades.Personal;

class GestionHorariosTest {

    @Test
    void coincideConFiltro_debeFiltrarPorDocenteTipoCursoYDia() {
        GestionHorarios gestion = new GestionHorarios();
        Personal docente = new Personal();
        docente.setIdPersonal(7);
        docente.setNombres("Ana");
        docente.setApellidos("García");
        docente.setTipoPersonal("DOCENTE");

        HorarioPersonal horario = new HorarioPersonal();
        horario.setIdPersonal(7);
        horario.setDiaSemana("LUNES");
        horario.setDescripcionBloque("Matemática");

        assertTrue(gestion.coincideConFiltro(horario, docente, "DOCENTE", "Ana García", "Matemática", "LUNES"));
        assertTrue(gestion.coincideConFiltro(horario, docente, "DOCENTE", "", "matem", ""));
        assertFalse(gestion.coincideConFiltro(horario, docente, "ADMINISTRATIVO", "Ana García", "Matemática", "LUNES"));
        assertFalse(gestion.coincideConFiltro(horario, docente, "DOCENTE", "Pedro", "Matemática", "LUNES"));
    }
}
