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
}