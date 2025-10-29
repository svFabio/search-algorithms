package edu.ai.misioneros.vista;

import edu.ai.misioneros.modelo.Nodo;
import edu.ai.misioneros.modelo.ResultadoBusqueda;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Panel inferior que muestra detalles del estado actual, cálculos y estadísticas.
 */
public class PanelInformacion extends VBox {
    private final Label lblEstadoActual = new Label("Estado actual: -");
    private final TextArea areaDetalles = new TextArea();
    private final Label lblTimerDFS = new Label("DFS: No ejecutado");
    private final Label lblTimerAStar = new Label("A*: No ejecutado");

    private AtomicLong tiempoDFS = new AtomicLong(0);
    private AtomicLong tiempoAStar = new AtomicLong(0);

    public PanelInformacion() {
        setSpacing(8);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #F9FAFB;");

        areaDetalles.setEditable(false);
        areaDetalles.getStyleClass().add("info-area");
        areaDetalles.setWrapText(true);

        lblTimerDFS.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1f2937;");
        lblTimerAStar.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1f2937;");

        HBox timers = new HBox(20, lblTimerDFS, lblTimerAStar);
        timers.setPadding(new Insets(5, 0, 5, 0));

        getChildren().addAll(lblEstadoActual, timers, areaDetalles);
        VBox.setVgrow(areaDetalles, Priority.ALWAYS);
    }

    public void actualizarNodoActual(Nodo nodo) {
        if (nodo == null) {
            lblEstadoActual.setText("Estado actual: -");
            areaDetalles.setText("");
            return;
        }
        lblEstadoActual.setText("Estado actual: " + nodo.getEstado());
    }

    public void actualizarResultado(ResultadoBusqueda r) {
        if (r == null) { areaDetalles.setText(""); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("Sección 2: Cálculos detallados\n");
        if (!r.getCaminoSolucion().isEmpty()) {
            Nodo n = r.getCaminoSolucion().get(r.getCaminoSolucion().size()-1);
            int M = n.getEstado().getMisionerosIzquierda();
            int C = n.getEstado().getCanibalesIzquierda();
            int h = 6 - 2*M - 2*C;
            int g = n.getNivel();
            int mL = M, cL = C, mR = 3 - M, cR = 3 - C;
            boolean izqOk = (mL==0) || (mL>=cL);
            boolean derOk = (mR==0) || (mR>=cR);
            int pen = 0; if (cL>mL && mL>0) pen -= 1000; if (mR>cR && mR>0) pen -= 1000;
            int FH = h + g + pen;
            sb.append("h = 6 - 2×"+M+" - 2×"+C+" = "+h+"\n");
            sb.append("g = nivel del nodo = "+g+"\n");
            sb.append("Penalizaciones:\n");
            sb.append("- Lado izquierdo: "+(izqOk?"válido ✓":"inválido ✗")+"\n");
            sb.append("- Lado derecho: "+(derOk?"válido ✓":"inválido ✗")+"\n");
            sb.append("- Penalización total = "+pen+"\n\n");
            sb.append("Función de evaluación:\n");
            sb.append("FH = h + g + penalizaciones = "+FH+"\n\n");
        }

        sb.append("Sección 4: Estadísticas del algoritmo\n");
        sb.append("- Nodos explorados: "+r.getNodosExplorados()+"\n");
        sb.append("- Nodos en frontera (ABIERTOS): "+r.getNodosAbiertos()+"\n");
        sb.append("- Nodos visitados (CERRADOS): "+r.getNodosCerrados()+"\n");
        sb.append("- Longitud de la solución: "+r.getCaminoSolucion().size()+" pasos\n");
        sb.append("- Tiempo de ejecución: "+r.getTiempoMs()+" ms\n");

        areaDetalles.setText(sb.toString());
    }

    public void actualizarTiempoDFS(long tiempo) {
        this.tiempoDFS.set(tiempo);
        Platform.runLater(() -> lblTimerDFS.setText("DFS: " + String.format("%.2f ms (%d ns)", tiempo / 1_000_000.0, tiempo)));
    }

    public void actualizarTiempoAStar(long tiempo) {
        this.tiempoAStar.set(tiempo);
        Platform.runLater(() -> lblTimerAStar.setText("A*: " + String.format("%.2f ms (%d ns)", tiempo / 1_000_000.0, tiempo)));
    }
}



