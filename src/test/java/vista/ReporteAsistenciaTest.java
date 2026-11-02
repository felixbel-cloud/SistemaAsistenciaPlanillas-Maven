package vista;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.YearMonth;
import static org.junit.jupiter.api.Assertions.*;

class ReporteAsistenciaTest {

    @Test
    void calculoMesCompletoDebeAjustarPrimerYUltimoDia() {
        int anio = 2026;

        // Agosto: 01/08/2026 al 31/08/2026
        YearMonth ymAgosto = YearMonth.of(anio, 8);
        LocalDate inicioAgosto = ymAgosto.atDay(1);
        LocalDate finAgosto = ymAgosto.atEndOfMonth();
        assertEquals("2026-08-01", inicioAgosto.toString());
        assertEquals("2026-08-31", finAgosto.toString());

        // Febrero no bisiesto 2026: 01/02/2026 al 28/02/2026
        YearMonth ymFeb = YearMonth.of(anio, 2);
        LocalDate inicioFeb = ymFeb.atDay(1);
        LocalDate finFeb = ymFeb.atEndOfMonth();
        assertEquals("2026-02-01", inicioFeb.toString());
        assertEquals("2026-02-28", finFeb.toString());

        // Febrero bisiesto 2024: 01/02/2024 al 29/02/2024
        YearMonth ymFebBisiesto = YearMonth.of(2024, 2);
        assertEquals(29, ymFebBisiesto.lengthOfMonth());
        assertEquals("2024-02-29", ymFebBisiesto.atEndOfMonth().toString());
    }

    @Test
    void nombreArchivoReporteDebeIncluirMes() {
        String mes = "Febrero";
        String nombreGeneral = "Reporte_Asistencias" + (mes.isEmpty() ? "" : "_" + mes);
        assertEquals("Reporte_Asistencias_Febrero", nombreGeneral);

        String persona = "Gladys Yovana Bustamante Cruz";
        String nombreIndividual = ExportacionIndividual.nombreArchivo(nombreGeneral, persona);
        assertEquals("Reporte_Asistencias_Febrero_Gladys_Yovana_Bustamante_Cruz", nombreIndividual);
    }
}
