package edu.ai.misioneros.vista;

import edu.ai.misioneros.modelo.Estado;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Componente que dibuja una matriz EXACTA de 3 filas x 5 columnas
 * Columnas: [MisL][CanL][Río][MisR][CanR]
 */
public class PanelMatriz extends Pane {
    private static final double CELL_SIZE = 40; // 35-45px recomendado
    private static final double CELL_GAP = 2;
    private final GridPane grid = new GridPane();
    private Estado estado;

    public PanelMatriz(Estado estado) {
        this.estado = estado;
        grid.setHgap(CELL_GAP);
        grid.setVgap(CELL_GAP);
        grid.setPadding(new Insets(4));
        getChildren().add(grid);
        setPrefSize(CELL_SIZE * 5 + CELL_GAP * 6, CELL_SIZE * 3 + CELL_GAP * 6);
        dibujar();
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
        dibujar();
    }

    private void dibujar() {
        grid.getChildren().clear();
        int M = estado.getMisionerosIzquierda();
        int C = estado.getCanibalesIzquierda();
        int MR = 3 - M;
        int CR = 3 - C;

        for (int fila = 0; fila < 3; fila++) {
            // Columna 0: Misioneros izquierda
            grid.add(celdaMisionero(fila < M), 0, fila);
            // Columna 1: Caníbales izquierda
            grid.add(celdaCanibal(fila < C), 1, fila);
            // Columna 2: Río siempre lleno
            grid.add(celdaRio(), 2, fila);
            // Columna 3: Misioneros derecha
            grid.add(celdaMisionero(fila < MR), 3, fila);
            // Columna 4: Caníbales derecha
            grid.add(celdaCanibal(fila < CR), 4, fila);
        }
    }

    private Node celdaMisionero(boolean llena) {
        Rectangle base = baseCell();
        if (llena) {
            Rectangle r = new Rectangle(CELL_SIZE * 0.6, CELL_SIZE * 0.6, Color.web("#3B82F6"));
            r.setArcWidth(6); r.setArcHeight(6);
            Pane p = wrap(base);
            r.setTranslateX((CELL_SIZE - r.getWidth()) / 2);
            r.setTranslateY((CELL_SIZE - r.getHeight()) / 2);
            p.getChildren().add(r);
            return p;
        }
        return wrap(base);
    }

    private Node celdaCanibal(boolean llena) {
        Rectangle base = baseCell();
        if (llena) {
            Circle c = new Circle(CELL_SIZE * 0.3, Color.web("#EF4444"));
            Pane p = wrap(base);
            c.setTranslateX(CELL_SIZE / 2);
            c.setTranslateY(CELL_SIZE / 2);
            p.getChildren().add(c);
            return p;
        }
        return wrap(base);
    }

    private Node celdaRio() {
        Rectangle base = baseCell();
        base.setFill(Color.web("#B3E5FC"));
        // ondas opcionales: tres líneas curvas simples mediante pequeños rectángulos
        Rectangle wave1 = new Rectangle(CELL_SIZE * 0.6, 2, Color.web("#7DD3FC"));
        Rectangle wave2 = new Rectangle(CELL_SIZE * 0.5, 2, Color.web("#60A5FA"));
        Rectangle wave3 = new Rectangle(CELL_SIZE * 0.4, 2, Color.web("#7DD3FC"));
        Pane p = wrap(base);
        wave1.setTranslateX(CELL_SIZE * 0.2); wave1.setTranslateY(CELL_SIZE * 0.3);
        wave2.setTranslateX(CELL_SIZE * 0.25); wave2.setTranslateY(CELL_SIZE * 0.5);
        wave3.setTranslateX(CELL_SIZE * 0.3); wave3.setTranslateY(CELL_SIZE * 0.7);
        p.getChildren().addAll(wave1, wave2, wave3);
        return p;
    }

    private Rectangle baseCell() {
        Rectangle r = new Rectangle(CELL_SIZE, CELL_SIZE);
        r.setFill(Color.web("#F5F5F5"));
        r.setStroke(Color.BLACK);
        r.setStrokeWidth(1);
        return r;
    }

    private Pane wrap(Rectangle base) {
        Pane p = new Pane();
        p.setPrefSize(CELL_SIZE, CELL_SIZE);
        p.getChildren().add(base);
        return p;
    }
}



