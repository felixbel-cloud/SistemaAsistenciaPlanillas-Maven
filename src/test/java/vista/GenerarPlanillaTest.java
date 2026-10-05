package vista;

import java.awt.Window;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import modelo.entidades.Usuario;

class GenerarPlanillaTest {

    @Test
    void abrirConsultaPlanilla_debeCrearVentanaVisible() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombreUsuario("admin");

        GenerarPlanilla ventanaOrigen = new GenerarPlanilla(usuario);
        ventanaOrigen.setVisible(false);

        ventanaOrigen.abrirConsultaPlanilla();

        boolean existeConsulta = Arrays.stream(Window.getWindows())
                .anyMatch(window -> window instanceof ConsultaPlanilla);

        for (Window window : Window.getWindows()) {
            if (window instanceof ConsultaPlanilla || window == ventanaOrigen) {
                window.dispose();
            }
        }

        assertTrue(existeConsulta,
                "Debe abrir la ventana de consulta de planillas después de generar la planilla.");
    }
}
