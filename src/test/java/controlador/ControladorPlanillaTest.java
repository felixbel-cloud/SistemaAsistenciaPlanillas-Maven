package controlador;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import modelo.entidades.Asistencia;
import modelo.entidades.DetallePlanilla;
import modelo.entidades.Personal;

class ControladorPlanillaTest {

    @Test
    void todosLosEmpleadosTienenSueldoFijoMensual() {
        Personal docente = new Personal();
        docente.setIdPersonal(10);
        docente.setTipoPersonal("DOCENTE");
        docente.setSalarioBase(2100.0);

        List<Asistencia> asistenciasDocente = new ArrayList<>();
        Asistencia asistencia1 = new Asistencia();
        asistencia1.setFecha(new Date());
        asistencia1.setHorasTrabajadas(8.0);
        asistencia1.setMinutosExtras(0);
        asistencia1.setMinutosTardanza(0);
        asistencia1.setEstado("PRESENTE");
        asistenciasDocente.add(asistencia1);

        DetallePlanilla detalleDocente = ControladorPlanilla.calcularDetallePorTipo(docente, asistenciasDocente);
        assertEquals(2100.0, detalleDocente.getSueldoNeto(), 0.01);

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

    private static Asistencia asistencia(String estado, double horas, int minExtras, int minTardanza, int dia) {
        Asistencia a = new Asistencia();
        a.setFecha(java.sql.Date.valueOf(java.time.LocalDate.of(2026, 10, dia)));
        a.setEstado(estado);
        a.setHorasTrabajadas(horas);
        a.setMinutosExtras(minExtras);
        a.setMinutosTardanza(minTardanza);
        return a;
    }

    @Test
    void personalConHorasExtraYTardanzaCalculaIngresosYDescuentosCorrectamente() {
        Personal docente = new Personal();
        docente.setIdPersonal(20);
        docente.setTipoPersonal("DOCENTE");
        docente.setSalarioBase(2400.0); // Sueldo mensual fijo: S/ 2,400 (S/ 80 por día, S/ 10 por hora)

        List<Asistencia> lista = new ArrayList<>();
        // Día 1: 60 min extra (1 hora extra)
        lista.add(asistencia("PRESENTE", 9.0, 60, 0, 1));
        // Día 2: 30 min de tardanza
        lista.add(asistencia("TARDANZA", 7.5, 0, 30, 2));

        DetallePlanilla d = ControladorPlanilla.calcularDetallePorTipo(docente, lista);

        // Sueldo fijo: 2400. Horas extra: 1h * 10 * 1.25 = 12.50. Total ingresos: 2412.50
        assertEquals(12.50, d.getMontoHorasExtras(), 0.01);
        assertEquals(2412.50, d.getTotalIngresos(), 0.01);
        // Descuento tardanza: (30 / 60) * 10 = 5.00
        assertEquals(5.00, d.getDescuentoTardanzas(), 0.01);
        assertEquals(2407.50, d.getSueldoNeto(), 0.01);
        assertEquals(2, d.getDiasTrabajados());
        assertEquals(0, d.getDiasAusencia());
    }

    @Test
    void administrativoMantieneDescuentosPorFaltaYTardanza() {
        Personal admin = new Personal();
        admin.setIdPersonal(21);
        admin.setTipoPersonal("ADMINISTRATIVO");
        admin.setSalarioBase(2400.0); // diario 80, hora 10

        List<Asistencia> lista = new ArrayList<>();
        lista.add(asistencia("TARDANZA", 8.0, 0, 30, 1));  // 0.5h * 10 = 5
        lista.add(asistencia("AUSENTE", 0.0, 0, 0, 2));    // falta

        DetallePlanilla d = ControladorPlanilla.calcularDetallePorTipo(admin, lista);
        int n = ControladorPlanilla.obtenerDiasJornadaSemanal(admin);
        double descFalta = 80.0 * (1.0 + 1.0 / n);
        assertEquals(descFalta + 5.0, d.getTotalDescuentos(), 0.01);
        assertEquals(2400.0 - (descFalta + 5.0), d.getSueldoNeto(), 0.01);
    }

    @Test
    void administrativoConSoloDosDiasTrabajadosEnfoqueB() {
        Personal admin = new Personal();
        admin.setIdPersonal(22);
        admin.setTipoPersonal("ADMINISTRATIVO");
        admin.setSalarioBase(1800.0); // diario 60

        List<Asistencia> lista = new ArrayList<>();
        // 2 días trabajados
        lista.add(asistencia("PRESENTE", 8.0, 0, 0, 1));
        lista.add(asistencia("PRESENTE", 8.0, 0, 0, 2));
        // 17 ausencias
        for (int dia = 3; dia <= 19; dia++) {
            lista.add(asistencia("AUSENTE", 0.0, 0, 0, dia));
        }

        DetallePlanilla d = ControladorPlanilla.calcularDetallePorTipo(admin, lista);
        assertEquals(2, d.getDiasTrabajados());
        assertEquals(17, d.getDiasAusencia());
        int n = ControladorPlanilla.obtenerDiasJornadaSemanal(admin);
        double ganadoEsperado = 2.0 * (60.0 * (1.0 + 1.0 / n));
        assertEquals(ganadoEsperado, d.getSueldoNeto(), 0.01);
        assertEquals(1800.0 - ganadoEsperado, d.getDescuentoAusencias(), 0.01);
    }

    @Test
    void administrativoContratadoAMitadDeMesCobraProporcional() {
        Personal admin = new Personal();
        admin.setIdPersonal(30);
        admin.setTipoPersonal("ADMINISTRATIVO");
        admin.setSalarioBase(3000.0);
        admin.setFechaContratacion(java.sql.Date.valueOf("2026-10-16")); // 15 días: 16 al 30

        DetallePlanilla d = ControladorPlanilla.calcularDetallePorTipo(admin, new ArrayList<>(), "2026-10");
        assertEquals(1500.0, d.getSueldoNeto(), 0.01);

        // Contratado antes del mes: sueldo completo
        admin.setFechaContratacion(java.sql.Date.valueOf("2026-01-10"));
        assertEquals(3000.0, ControladorPlanilla.calcularDetallePorTipo(admin, new ArrayList<>(), "2026-10").getSueldoNeto(), 0.01);

        // Contratado el día 1: completo; el 31 (mes de 31 días): 0 días sobre 30
        assertEquals(30, ControladorPlanilla.diasComputablesPorContratacion(java.sql.Date.valueOf("2026-10-01"), "2026-10"));
        assertEquals(0, ControladorPlanilla.diasComputablesPorContratacion(java.sql.Date.valueOf("2026-10-31"), "2026-10"));
        assertEquals(1, ControladorPlanilla.diasComputablesPorContratacion(java.sql.Date.valueOf("2026-10-30"), "2026-10"));
        // Febrero: contratado el 15 -> 16 días (30 - 15 + 1)
        assertEquals(16, ControladorPlanilla.diasComputablesPorContratacion(java.sql.Date.valueOf("2026-02-15"), "2026-02"));
    }

    @Test
    void noSePuedeGenerarPlanillaDeUnMesQueNoEmpieza() {
        ControladorPlanilla c = new ControladorPlanilla();
        java.time.YearMonth actual = java.time.YearMonth.now();
        assertTrue(c.validarPeriodo(actual.toString()));
        assertTrue(c.validarPeriodo(actual.minusMonths(1).toString()));
        org.junit.jupiter.api.Assertions.assertFalse(c.validarPeriodo(actual.plusMonths(1).toString()));
        org.junit.jupiter.api.Assertions.assertFalse(c.validarPeriodo("2019-12"));
        assertTrue(c.validarPeriodo("2031-01") == !java.time.YearMonth.of(2031, 1).isAfter(actual));
    }

    @Test
    void docenteConTardanzaTieneDescuentoProporcionalASuSueldoFijo() {
        Personal docente = new Personal();
        docente.setIdPersonal(17);
        docente.setTipoPersonal("DOCENTE");
        docente.setSalarioBase(2100.0);

        List<Asistencia> asistencias = new ArrayList<>();
        Asistencia a = new Asistencia();
        a.setFecha(new Date());
        a.setHorasTrabajadas(6.0);
        a.setMinutosExtras(0);
        a.setMinutosTardanza(117); // 1.95 horas
        a.setEstado("TARDANZA");
        asistencias.add(a);

        DetallePlanilla d = ControladorPlanilla.calcularDetallePorTipo(docente, asistencias, "2026-10");
        // Sueldo fijo: 2100. Valor hora: 2100 / 30 / 8 = 8.75. Descuento tardanza: 1.95 * 8.75 = 17.0625 -> 17.06
        // Neto: 2100 - 17.06 = 2082.94
        assertEquals(2100.0, d.getTotalIngresos(), 0.01);
        assertEquals(17.06, d.getDescuentoTardanzas(), 0.01);
        assertEquals(2082.94, d.getSueldoNeto(), 0.01);
    }
}
