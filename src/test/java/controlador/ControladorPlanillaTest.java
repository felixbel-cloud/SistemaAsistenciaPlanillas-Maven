package controlador;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import modelo.entidades.Asistencia;
import modelo.entidades.DetallePlanilla;
import modelo.entidades.Personal;

class ControladorPlanillaTest {

    @Test
    void docenteDebePagarsePorHorasTrabajadasYAdministrativoPorSalarioMensual() {
        Personal docente = new Personal();
        docente.setIdPersonal(10);
        docente.setTipoPersonal("DOCENTE");
        docente.setSalarioBase(25.0);

        List<Asistencia> asistenciasDocente = new ArrayList<>();
        Asistencia asistencia1 = new Asistencia();
        asistencia1.setFecha(new Date());
        asistencia1.setHorasTrabajadas(8.0);
        asistencia1.setMinutosExtras(0);
        asistencia1.setMinutosTardanza(0);
        asistencia1.setEstado("PRESENTE");
        asistenciasDocente.add(asistencia1);

        DetallePlanilla detalleDocente = ControladorPlanilla.calcularDetallePorTipo(docente, asistenciasDocente);
        assertEquals(200.0, detalleDocente.getSueldoNeto(), 0.01);

        Personal administrativo = new Personal();
        administrativo.setIdPersonal(11);
        administrativo.setTipoPersonal("ADMINISTRATIVO");
        administrativo.setSalarioBase(2000.0);

        List<Asistencia> asistenciasAdmin = new ArrayList<>();
        Asistencia asistencia2 = new Asistencia();
        asistencia2.setFecha(new Date());
        asistencia2.setHorasTrabajadas(8.0);
        asistencia2.setMinutosExtras(0);
        asistencia2.setMinutosTardanza(0);
        asistencia2.setEstado("PRESENTE");
        asistenciasAdmin.add(asistencia2);

        DetallePlanilla detalleAdmin = ControladorPlanilla.calcularDetallePorTipo(administrativo, asistenciasAdmin);
        assertEquals(2000.0, detalleAdmin.getSueldoNeto(), 0.01);
    }
}
