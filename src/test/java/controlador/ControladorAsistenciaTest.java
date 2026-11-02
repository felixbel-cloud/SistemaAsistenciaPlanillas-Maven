package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import modelo.conexion.ConexionDB;
import modelo.dao.AsistenciaDAO;

class ControladorAsistenciaTest {
    @Test
    void obtenerDiaSemanaDevuelveCadaDiaEnElFormatoDeHorario() {
        String[] diasEsperados = {
            "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"
        };

        for (int indice = 0; indice < diasEsperados.length; indice++) {
            Calendar fecha = Calendar.getInstance();
            fecha.clear();
            fecha.set(2025, Calendar.NOVEMBER, 3 + indice, 12, 0, 0);

            assertEquals(diasEsperados[indice], ControladorAsistencia.obtenerDiaSemana(fecha.getTime()));
        }
    }

    @Test
    void debeEncontrarLaAsistenciaAbiertaParaRegistrarSalida() {
        AsistenciaDAO asistenciaDAO = new AsistenciaDAO();
        assertNotNull(asistenciaDAO.obtenerUltimaAsistenciaAbierta(1));
    }

    @Test
    void debeCalcularMinutosEnBaseAlHorarioDelDiaNoAlEpoch() {
        Time entrada = Time.valueOf("08:00:00");
        Time salida = Time.valueOf("17:00:00");

        assertEquals(9 * 60, java.time.Duration.between(entrada.toLocalTime(), salida.toLocalTime()).toMinutes());
        assertEquals(0, java.time.Duration.between(Time.valueOf("17:00:00").toLocalTime(), salida.toLocalTime()).toMinutes());
    }

    @Test
    void debeRegistrarSalidaPorPersonalYFechaCuandoElIdNoExiste() throws Exception {
        AsistenciaDAO asistenciaDAO = new AsistenciaDAO();
        Connection conexion = ConexionDB.getConexion();
        Date fecha = new Date(System.currentTimeMillis() + 86400000L);

        String insertSql = "INSERT INTO asistencia (id_personal, fecha, hora_entrada, estado, observaciones) VALUES (?, ?, ?, 'PRESENTE', 'regresion salida')";
        try (PreparedStatement ps = conexion.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, 1);
            ps.setDate(2, new java.sql.Date(fecha.getTime()));
            ps.setTime(3, Time.valueOf("08:15:00"));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                int idAsistencia = rs.getInt(1);

                boolean resultado = asistenciaDAO.registrarSalidaPorPersonalFecha(
                        1,
                        fecha,
                        Time.valueOf("17:05:00"),
                        5,
                        8.5);

                assertTrue(resultado);

                try (PreparedStatement ver = conexion.prepareStatement(
                        "SELECT hora_salida, minutos_extras, horas_trabajadas FROM asistencia WHERE id_asistencia = ?")) {
                    ver.setInt(1, idAsistencia);
                    try (ResultSet fila = ver.executeQuery()) {
                        assertTrue(fila.next());
                        assertEquals("17:05:00", fila.getTime("hora_salida").toString());
                    }
                }
            }
        } finally {
            conexion.prepareStatement("DELETE FROM asistencia WHERE observaciones = 'regresion salida'").executeUpdate();
        }
    }

    private static modelo.entidades.HorarioPersonal bloque(int id, String inicio, String fin) {
        modelo.entidades.HorarioPersonal b = new modelo.entidades.HorarioPersonal();
        b.setIdHorario(id);
        b.setHoraInicio(Time.valueOf(inicio));
        b.setHoraFin(Time.valueOf(fin));
        return b;
    }

    @Test
    void soloSeMarcaFaltaEnBloquesQueYaTerminaron() {
        java.time.LocalDateTime ahora = java.time.LocalDateTime.of(2026, 10, 20, 10, 0);
        java.time.LocalDate hoy = ahora.toLocalDate();
        modelo.entidades.HorarioPersonal manana = bloque(1, "07:00:00", "09:00:00");
        modelo.entidades.HorarioPersonal tarde = bloque(2, "17:00:00", "20:00:00");

        assertTrue(ControladorAsistencia.bloqueYaTermino(hoy, manana, ahora));
        org.junit.jupiter.api.Assertions.assertFalse(ControladorAsistencia.bloqueYaTermino(hoy, tarde, ahora));
        assertTrue(ControladorAsistencia.bloqueYaTermino(hoy.minusDays(1), tarde, ahora));
        org.junit.jupiter.api.Assertions.assertFalse(ControladorAsistencia.bloqueYaTermino(hoy.plusDays(1), manana, ahora));
    }

    @Test
    void laEntradaSeAsociaAlBloqueQueAunNoTermina() {
        modelo.entidades.HorarioPersonal manana = bloque(1, "08:00:00", "12:00:00");
        modelo.entidades.HorarioPersonal tarde = bloque(2, "17:00:00", "20:00:00");
        java.util.List<modelo.entidades.HorarioPersonal> pendientes = java.util.List.of(manana, tarde);

        assertEquals(1, ControladorAsistencia.elegirBloqueParaEntrada(pendientes, java.time.LocalTime.of(7, 55)).getIdHorario());
        assertEquals(2, ControladorAsistencia.elegirBloqueParaEntrada(pendientes, java.time.LocalTime.of(16, 50)).getIdHorario());
        org.junit.jupiter.api.Assertions.assertNull(ControladorAsistencia.elegirBloqueParaEntrada(java.util.List.of(), java.time.LocalTime.NOON));
    }

    @Test
    void soloSePuedeMarcarEntradaDentroDelHorarioConfigurado() {
        modelo.entidades.HorarioPersonal manana = bloque(1, "08:00:00", "15:00:00");
        java.util.List<modelo.entidades.HorarioPersonal> pendientes = java.util.List.of(manana);

        // Antes de las 07:30 -> bloqueado
        org.junit.jupiter.api.Assertions.assertNull(ControladorAsistencia.elegirBloqueParaEntrada(pendientes, java.time.LocalTime.of(7, 29)));
        // 07:30 a 14:59 -> permitido
        assertNotNull(ControladorAsistencia.elegirBloqueParaEntrada(pendientes, java.time.LocalTime.of(7, 30)));
        assertNotNull(ControladorAsistencia.elegirBloqueParaEntrada(pendientes, java.time.LocalTime.of(14, 59)));
        // 15:00 o después (por ejemplo, 23:00) -> bloqueado
        org.junit.jupiter.api.Assertions.assertNull(ControladorAsistencia.elegirBloqueParaEntrada(pendientes, java.time.LocalTime.of(15, 0)));
        org.junit.jupiter.api.Assertions.assertNull(ControladorAsistencia.elegirBloqueParaEntrada(pendientes, java.time.LocalTime.of(23, 0)));

        assertTrue(ControladorAsistencia.motivoEntradaFueraDeHorario(pendientes, java.time.LocalTime.of(6, 0)).contains("desde las 07:30"));
        assertTrue(ControladorAsistencia.motivoEntradaFueraDeHorario(pendientes, java.time.LocalTime.of(23, 0)).contains("ya terminó"));
    }

    @Test
    void entreDosBloquesNoSePuedeMarcarHastaLaVentanaDelSiguiente() {
        modelo.entidades.HorarioPersonal manana = bloque(1, "08:00:00", "12:00:00");
        modelo.entidades.HorarioPersonal tarde = bloque(2, "17:00:00", "20:00:00");
        java.util.List<modelo.entidades.HorarioPersonal> pendientes = java.util.List.of(manana, tarde);

        org.junit.jupiter.api.Assertions.assertNull(ControladorAsistencia.elegirBloqueParaEntrada(pendientes, java.time.LocalTime.of(14, 0)));
        assertTrue(ControladorAsistencia.motivoEntradaFueraDeHorario(pendientes, java.time.LocalTime.of(14, 0)).contains("desde las 16:30"));
        assertEquals(2, ControladorAsistencia.elegirBloqueParaEntrada(pendientes, java.time.LocalTime.of(16, 30)).getIdHorario());
    }

    private static int m(int h, int min) { return h * 60 + min; }

    @Test
    void horasPagablesSeCuentanDesdeElInicioDelBloqueConMinimoYTopeDeExtras() {
        int inicio = m(8, 0), fin = m(15, 0);

        // Llega 07:30, sale 16:00 -> 7h normales (desde 08:00) + 60 min extra
        int[] t = ControladorAsistencia.calcularMinutosPagables(m(7, 30), m(16, 0), inicio, fin);
        assertEquals(7 * 60 + 60, t[0]);
        assertEquals(60, t[1]);

        // Sale 15:10 -> 10 min después: menos de 15, no es extra y no se paga
        t = ControladorAsistencia.calcularMinutosPagables(m(8, 0), m(15, 10), inicio, fin);
        assertEquals(7 * 60, t[0]);
        assertEquals(0, t[1]);
        assertEquals(10, t[2]);

        // Sale 15:15 -> exactamente el mínimo: 15 min extra
        t = ControladorAsistencia.calcularMinutosPagables(m(8, 0), m(15, 15), inicio, fin);
        assertEquals(15, t[1]);

        // Sale 23:00 -> 8h después: se reconoce el tope de 2h
        t = ControladorAsistencia.calcularMinutosPagables(m(8, 0), m(23, 0), inicio, fin);
        assertEquals(120, t[1]);
        assertEquals(7 * 60 + 120, t[0]);

        // Llega 08:04 (dentro de la tolerancia de 5 min) -> se paga desde las 08:00: 7h completas
        t = ControladorAsistencia.calcularMinutosPagables(m(8, 4), m(15, 0), inicio, fin);
        assertEquals(7 * 60, t[0]);
        // Llega 08:05 (límite de la tolerancia) -> todavía se paga desde las 08:00
        t = ControladorAsistencia.calcularMinutosPagables(m(8, 5), m(15, 0), inicio, fin);
        assertEquals(7 * 60, t[0]);
        // Llega 08:06 (fuera de tolerancia) -> se paga desde las 08:06
        t = ControladorAsistencia.calcularMinutosPagables(m(8, 6), m(15, 0), inicio, fin);
        assertEquals(7 * 60 - 6, t[0]);
        // Llega 10:00 -> solo de 10:00 a 15:00
        t = ControladorAsistencia.calcularMinutosPagables(m(10, 0), m(15, 0), inicio, fin);
        assertEquals(5 * 60, t[0]);

        // Llega tarde 08:30 y se va temprano 14:00 -> 5.5h, sin extras
        t = ControladorAsistencia.calcularMinutosPagables(m(8, 30), m(14, 0), inicio, fin);
        assertEquals(330, t[0]);
        assertEquals(0, t[1]);
    }
}
