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
 * Panel inferior que muestra detalles del estado actual, cálculos y
 * estadísticas.
 */
public class PanelInformacion extends VBox {
    private final Label lblEstadoActualDFS = new Label("Voraz actual: -");
    private final Label lblEstadoActualAStar = new Label("A* actual: -");
    private final TextArea areaDetallesDFS = new TextArea();
    private final TextArea areaDetallesAStar = new TextArea();
    private final Label lblTimerDFS = new Label("Voraz: No ejecutado");
    private final Label lblTimerAStar = new Label("A*: No ejecutado");

    private AtomicLong tiempoDFS = new AtomicLong(0);
    private AtomicLong tiempoAStar = new AtomicLong(0);

    public PanelInformacion() {
        setSpacing(8);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #F9FAFB;");

        areaDetallesDFS.setEditable(false);
        areaDetallesDFS.getStyleClass().add("info-area");
        areaDetallesDFS.setWrapText(true);
        areaDetallesAStar.setEditable(false);
        areaDetallesAStar.getStyleClass().add("info-area");
        areaDetallesAStar.setWrapText(true);

        lblTimerDFS.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1f2937;");
        lblTimerAStar.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1f2937;");

        HBox timers = new HBox(20, lblTimerDFS, lblTimerAStar);
        timers.setPadding(new Insets(5, 0, 5, 0));

        HBox headers = new HBox(20, lblEstadoActualDFS, lblEstadoActualAStar);
        headers.setPadding(new Insets(0, 0, 5, 0));

        HBox areas = new HBox(10, areaDetallesDFS, areaDetallesAStar);
        HBox.setHgrow(areaDetallesDFS, Priority.ALWAYS);
        HBox.setHgrow(areaDetallesAStar, Priority.ALWAYS);
        areaDetallesDFS.setPrefColumnCount(40);
        areaDetallesAStar.setPrefColumnCount(40);

        getChildren().addAll(headers, timers, areas);
        VBox.setVgrow(areas, Priority.ALWAYS);
    }

    public void actualizarNodoActualVoraz(Nodo nodo) {
        if (nodo == null) {
            lblEstadoActualDFS.setText("Voraz actual: -");
            return;
        }
        lblEstadoActualDFS.setText("Voraz actual: " + nodo.getEstado());
    }

    public void actualizarNodoActualAStar(Nodo nodo) {
        if (nodo == null) {
            lblEstadoActualAStar.setText("A* actual: -");
            return;
        }
        lblEstadoActualAStar.setText("A* actual: " + nodo.getEstado());
    }

    public void actualizarResultadoVoraz(ResultadoBusqueda r) {
        areaDetallesDFS.setText(buildDetallesVoraz(r));
    }

    public void actualizarResultadoAStar(ResultadoBusqueda r) {
        areaDetallesAStar.setText(buildDetallesAStar(r));
    }

    public void actualizarTiempoVoraz(long tiempo) {
        this.tiempoDFS.set(tiempo);
        Platform.runLater(
                () -> lblTimerDFS.setText("Voraz: " + String.format("%.2f ms (%d ns)", tiempo / 1_000_000.0, tiempo)));
    }

    public void actualizarTiempoAStar(long tiempo) {
        this.tiempoAStar.set(tiempo);
        Platform.runLater(
                () -> lblTimerAStar.setText("A*: " + String.format("%.2f ms (%d ns)", tiempo / 1_000_000.0, tiempo)));
    }

    private String buildDetallesVoraz(ResultadoBusqueda r) {
        if (r == null)
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append("Detalles de evaluación (H = g + n)\n");
        if (!r.getCaminoSolucion().isEmpty()) {
            Nodo n = r.getCaminoSolucion().get(r.getCaminoSolucion().size() - 1);
            int M = n.getEstado().getMisionerosIzquierda();
            int C = n.getEstado().getCanibalesIzquierda();
            int mR = n.getEstado().getMisionerosDerecha();
            int cR = n.getEstado().getCanibalesDerecha();
            
            // g = misioneros + canibales en el lado izquierdo
            int g = M + C;
            // n = misioneros + canibales en el lado derecho
            int nVal = mR + cR;
            // H = g + n
            int H = g + nVal;
            
            sb.append("g = misioneros izquierda + canibales izquierda = " + M + " + " + C + " = " + g + "\n");
            sb.append("n = misioneros derecha + canibales derecha = " + mR + " + " + cR + " = " + nVal + "\n");
            sb.append("H = g + n = " + g + " + " + nVal + " = " + H + "\n\n");
            
            // Validación: solo aplicar cuando misioneros >= canibales en ambos lados
            boolean izqOk = (M == 0) || (M >= C);
            boolean derOk = (mR == 0) || (mR >= cR);
            sb.append("Validación (misioneros >= canibales):\n");
            sb.append("- Lado izquierdo: " + (izqOk ? "válido ✓" : "inválido ✗") + " (" + M + " >= " + C + ")\n");
            sb.append("- Lado derecho: " + (derOk ? "válido ✓" : "inválido ✗") + " (" + mR + " >= " + cR + ")\n\n");
        }
        sb.append("Estadísticas\n");
        sb.append("- Nodos explorados: " + r.getNodosExplorados() + "\n");
        sb.append("- Nodos en frontera (ABIERTOS): " + r.getNodosAbiertos() + "\n");
        sb.append("- Nodos visitados (CERRADOS): " + r.getNodosCerrados() + "\n");
        sb.append("- Longitud de la solución: " + r.getCaminoSolucion().size() + " pasos\n");
        sb.append("- Tiempo de ejecución: " + r.getTiempoMs() + " ms\n");
        return sb.toString();
    }
    
    private String buildDetallesAStar(ResultadoBusqueda r) {
        if (r == null)
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append("Detalles de evaluación (h, g, FH)\n");
        if (!r.getCaminoSolucion().isEmpty()) {
            Nodo n = r.getCaminoSolucion().get(r.getCaminoSolucion().size() - 1);
            int M = n.getEstado().getMisionerosIzquierda();
            int C = n.getEstado().getCanibalesIzquierda();
            int h = 6 - 2 * M - 2 * C;
            int g = n.getNivel();
            int mL = M, cL = C, mR = 3 - M, cR = 3 - C;
            boolean izqOk = (mL == 0) || (mL >= cL);
            boolean derOk = (mR == 0) || (mR >= cR);
            int pen = 0;
            if (cL > mL && mL > 0)
                pen -= 1000;
            if (mR > cR && mR > 0)
                pen -= 1000;
            int FH = h + g + pen;
            sb.append("h = 6 - 2×" + M + " - 2×" + C + " = " + h + "\n");
            sb.append("g = nivel del nodo = " + g + "\n");
            sb.append("Penalizaciones:\n");
            sb.append("- Lado izquierdo: " + (izqOk ? "válido ✓" : "inválido ✗") + "\n");
            sb.append("- Lado derecho: " + (derOk ? "válido ✓" : "inválido ✗") + "\n");
            sb.append("- Penalización total = " + pen + "\n\n");
            sb.append("FH = h + g + penalizaciones = " + FH + "\n\n");
        }
        sb.append("Estadísticas\n");
        sb.append("- Nodos explorados: " + r.getNodosExplorados() + "\n");
        sb.append("- Nodos en frontera (ABIERTOS): " + r.getNodosAbiertos() + "\n");
        sb.append("- Nodos visitados (CERRADOS): " + r.getNodosCerrados() + "\n");
        sb.append("- Longitud de la solución: " + r.getCaminoSolucion().size() + " pasos\n");
        sb.append("- Tiempo de ejecución: " + r.getTiempoMs() + " ms\n");
        return sb.toString();
    }
}
