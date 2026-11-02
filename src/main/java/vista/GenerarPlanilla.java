// vista/GenerarPlanilla.java
package vista;

import modelo.entidades.Usuario;

/**
 * Módulo unificado de Planillas: la generación y la consulta se gestionan
 * desde un único panel centralizado (ConsultaPlanilla).
 * Esta clase se mantiene para compatibilidad con accesos directos y pruebas.
 */
public class GenerarPlanilla extends ConsultaPlanilla {

    public GenerarPlanilla() {
        super();
    }

    public GenerarPlanilla(Usuario usuario) {
        super(usuario);
    }

    public void abrirConsultaPlanilla() {
        abrirConsultaPlanilla(null);
    }

    public void abrirConsultaPlanilla(String periodo) {
        ConsultaPlanilla ventana = new ConsultaPlanilla(periodo);
        ventana.setVisible(true);
    }
}
