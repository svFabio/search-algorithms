package edu.ai.misioneros.controlador;

import edu.ai.misioneros.algoritmoAestrella.AlgoritmoAEstrella;
import edu.ai.misioneros.logicaVoraz.AlgoritmoVoraz;
import edu.ai.misioneros.modelo.Nodo;
import edu.ai.misioneros.modelo.ResultadoBusqueda;
import edu.ai.misioneros.vista.PanelArbol;
import edu.ai.misioneros.vista.PanelInformacion;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Controla la ventana principal, botones y visualización.
 */
public class ControladorPrincipal {
    private final BorderPane root = new BorderPane();

    private final Button btnResolver = new Button("Resolver");
    private final Button btnPaso = new Button("Paso a Paso");
    private final Button btnReiniciar = new Button("Reiniciar");
    private final Slider sldVelocidad = new Slider(0.5, 3.0, 1.0);

    private final PanelArbol panelArbolDFS = new PanelArbol();
    private final PanelArbol panelArbolAStar = new PanelArbol();

    private final Button btnBackDFS = new Button("◀ Back");
    private final Button btnNextDFS = new Button("Next ▶");
    private final Button btnBackAStar = new Button("◀ Back");
    private final Button btnNextAStar = new Button("Next ▶");

    // Botones de zoom
    private final Button btnZoomIn = new Button("🔍+");
    private final Button btnZoomOut = new Button("🔍-");
    private final Button btnZoomReset = new Button("🔄");

    private final PanelInformacion panelInfo = new PanelInformacion();

    private ResultadoBusqueda resultadoDFS;
    private ResultadoBusqueda resultadoAStar;
    private int indiceSolucionDFS = 0;
    private int indiceSolucionAStar = 0;

    public ControladorPrincipal() {
        construirUI();
        wireEventos();
    }

    public BorderPane getRoot() {
        return root;
    }

    private void construirUI() {
        // TOP
        HBox top = new HBox(10, btnResolver, btnPaso, btnReiniciar, new Label("Velocidad:"), sldVelocidad);
        top.setPadding(new Insets(10));
        top.getStyleClass().add("top-bar");
        root.setTop(top);

        // CENTER split
        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.HORIZONTAL);
        split.setDividerPositions(0.5);
        split.getStyleClass().add("split-pane");

        // Panel izquierdo (Voraz)
        VBox izquierda = crearPanelAlgoritmo("Algoritmo Voraz", panelArbolDFS);
        // Panel derecho (A*)
        VBox derecha = crearPanelAlgoritmo("Algoritmo A*", panelArbolAStar);

        split.getItems().addAll(izquierda, derecha);
        root.setCenter(split);

        // BOTTOM info
        panelInfo.setPrefHeight(180);
        root.setBottom(panelInfo);
    }

    private VBox crearPanelAlgoritmo(String titulo, PanelArbol panelArbol) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("titulo-panel");
        ScrollPane scroll = new ScrollPane(panelArbol);
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(false);

        // Botones de navegación para A* (solo en el panel derecho)
        if (titulo.equals("Algoritmo A*")) {
            HBox nav = new HBox(10, btnBackAStar, btnNextAStar);
            nav.setPadding(new Insets(8));
            nav.setStyle("-fx-alignment: center;");

            // Botones de zoom
            HBox zoomControls = new HBox(5, btnZoomIn, btnZoomOut, btnZoomReset);
            zoomControls.setPadding(new Insets(4));
            zoomControls.setStyle("-fx-alignment: center;");

            VBox box = new VBox(8, lblTitulo, scroll, nav, zoomControls);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            box.setPadding(new Insets(10));
            return box;
        }

        // Para DFS: incluir navegación propia
        HBox nav = new HBox(10, btnBackDFS, btnNextDFS);
        nav.setPadding(new Insets(8));
        nav.setStyle("-fx-alignment: center;");
        VBox box = new VBox(8, lblTitulo, scroll, nav);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        box.setPadding(new Insets(10));
        return box;
    }

    private void wireEventos() {
        btnResolver.setOnAction(e -> resolverCompleto());
        btnPaso.setOnAction(e -> pasoAPaso());
        btnReiniciar.setOnAction(e -> reiniciar());
        btnBackDFS.setOnAction(e -> moverIndiceDFS(-1));
        btnNextDFS.setOnAction(e -> moverIndiceDFS(1));
        btnBackAStar.setOnAction(e -> moverIndiceAStar(-1));
        btnNextAStar.setOnAction(e -> moverIndiceAStar(1));

        // Eventos de zoom
        btnZoomIn.setOnAction(e -> panelArbolAStar.aplicarZoom(1.2));
        btnZoomOut.setOnAction(e -> panelArbolAStar.aplicarZoom(0.8));
        btnZoomReset.setOnAction(e -> panelArbolAStar.resetearZoom());
    }

    private void resolverCompleto() {
        long inicioTotal = System.nanoTime();

        // Ejecutar ambos algoritmos en paralelo
        Thread dfsThread = new Thread(() -> {
            long inicio = System.nanoTime();
            AlgoritmoVoraz voraz = new AlgoritmoVoraz();
            resultadoDFS = voraz.resolver();
            long fin = System.nanoTime();
            long tiempo = fin - inicio;
            System.out.println("Voraz terminó en " + tiempo / 1_000_000.0 + " ms (" + tiempo + " ns)");
            panelInfo.actualizarTiempoVoraz(tiempo);
        });

        Thread astarThread = new Thread(() -> {
            long inicio = System.nanoTime();
            AlgoritmoAEstrella astar = new AlgoritmoAEstrella();
            resultadoAStar = astar.resolver();
            long fin = System.nanoTime();
            long tiempo = fin - inicio;
            System.out.println("A* terminó en " + tiempo / 1_000_000.0 + " ms (" + tiempo + " ns)");
            panelInfo.actualizarTiempoAStar(tiempo);
        });

        dfsThread.start();
        astarThread.start();

        try {
            dfsThread.join();
            astarThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long finTotal = System.nanoTime();
        System.out.println("Tiempo total: " + (finTotal - inicioTotal) / 1_000_000.0 + " ms");

        // Mostrar TODO el árbol desde el inicio
        panelArbolDFS.setDatos(resultadoDFS.getRaiz(), resultadoDFS.getCaminoSolucion());
        panelArbolAStar.setDatos(resultadoAStar.getRaiz(), resultadoAStar.getCaminoSolucion());

        // Expandir y mostrar TODO el árbol
        expandirTodoElArbol();

        indiceSolucionDFS = 0;
        indiceSolucionAStar = 0;
        actualizarSeleccionActualDFS();
        actualizarSeleccionActualAStar();
        panelInfo.actualizarResultadoVoraz(resultadoDFS);
        panelInfo.actualizarResultadoAStar(resultadoAStar);
    }

    private void expandirTodoElArbol() {
        if (resultadoAStar == null || resultadoAStar.getCaminoSolucion().isEmpty())
            return;

        // Mostrar todos los nodos del camino solución
        for (Nodo n : resultadoAStar.getCaminoSolucion()) {
            panelArbolAStar.expandirDesde(n);
        }

        // También para DFS
        if (resultadoDFS != null && !resultadoDFS.getCaminoSolucion().isEmpty()) {
            for (Nodo n : resultadoDFS.getCaminoSolucion()) {
                panelArbolDFS.expandirDesde(n);
            }
        }
    }

    private void pasoAPaso() {
        // Modo: empezar desde el inicio
        if (resultadoAStar == null) {
            resolverCompleto();
        }

        // Reiniciar el modo paso a paso
        indiceSolucionDFS = 0;
        indiceSolucionAStar = 0;

        // Mostrar solo la raíz inicialmente en ambos
        panelArbolDFS.setDatos(resultadoDFS.getRaiz(), resultadoDFS.getCaminoSolucion());
        panelArbolAStar.setDatos(resultadoAStar.getRaiz(), resultadoAStar.getCaminoSolucion());

        // Actualizar la información
        actualizarSeleccionActualDFS();
        actualizarSeleccionActualAStar();
        panelInfo.actualizarNodoActualVoraz(resultadoDFS.getRaiz());
        panelInfo.actualizarNodoActualAStar(resultadoAStar.getRaiz());
    }

    private void reiniciar() {
        resultadoDFS = null;
        resultadoAStar = null;
        indiceSolucionDFS = 0;
        indiceSolucionAStar = 0;
        panelArbolDFS.setDatos(null, null);
        panelArbolAStar.setDatos(null, null);
        panelInfo.actualizarNodoActualVoraz(null);
        panelInfo.actualizarNodoActualAStar(null);
        panelInfo.actualizarResultadoVoraz(null);
        panelInfo.actualizarResultadoAStar(null);
    }

    private void moverIndiceAStar(int delta) {
        if (resultadoAStar == null)
            return;
        List<Nodo> camino = resultadoAStar.getCaminoSolucion();
        if (camino.isEmpty())
            return;

        if (delta > 0) {
            if (indiceSolucionAStar < camino.size() - 1) {
                indiceSolucionAStar++;
                Nodo actual = camino.get(indiceSolucionAStar);
                panelArbolAStar.expandirDesde(actual);
                panelArbolAStar.resaltarNodoActual(actual);
                panelInfo.actualizarNodoActualAStar(actual);
            }
        } else {
            if (indiceSolucionAStar > 0) {
                indiceSolucionAStar--;
                Nodo actual = camino.get(indiceSolucionAStar);
                panelArbolAStar.resaltarNodoActual(actual);
                panelInfo.actualizarNodoActualAStar(actual);
            }
        }
        btnBackAStar.setDisable(indiceSolucionAStar == 0);
        btnNextAStar.setDisable(indiceSolucionAStar >= camino.size() - 1);
    }

    private void moverIndiceDFS(int delta) {
        if (resultadoDFS == null)
            return;
        List<Nodo> camino = resultadoDFS.getCaminoSolucion();
        if (camino.isEmpty())
            return;

        if (delta > 0) {
            if (indiceSolucionDFS < camino.size() - 1) {
                indiceSolucionDFS++;
                Nodo actual = camino.get(indiceSolucionDFS);
                panelArbolDFS.expandirDesde(actual);
                panelArbolDFS.resaltarNodoActual(actual);
                panelInfo.actualizarNodoActualVoraz(actual);
            }
        } else {
            if (indiceSolucionDFS > 0) {
                indiceSolucionDFS--;
                Nodo actual = camino.get(indiceSolucionDFS);
                panelArbolDFS.resaltarNodoActual(actual);
                panelInfo.actualizarNodoActualVoraz(actual);
            }
        }
        btnBackDFS.setDisable(indiceSolucionDFS == 0);
        btnNextDFS.setDisable(indiceSolucionDFS >= camino.size() - 1);
    }

    private void actualizarSeleccionActualAStar() {
        if (resultadoAStar == null || resultadoAStar.getCaminoSolucion().isEmpty())
            return;
        Nodo actual = resultadoAStar.getCaminoSolucion().get(indiceSolucionAStar);
        panelArbolAStar.resaltarNodoActual(actual);
        panelInfo.actualizarNodoActualAStar(actual);
        btnBackAStar.setDisable(indiceSolucionAStar == 0);
        btnNextAStar.setDisable(indiceSolucionAStar >= resultadoAStar.getCaminoSolucion().size() - 1);
    }

    private void actualizarSeleccionActualDFS() {
        if (resultadoDFS == null || resultadoDFS.getCaminoSolucion().isEmpty())
            return;
        Nodo actual = resultadoDFS.getCaminoSolucion().get(indiceSolucionDFS);
        panelArbolDFS.resaltarNodoActual(actual);
        panelInfo.actualizarNodoActualVoraz(actual);
        btnBackDFS.setDisable(indiceSolucionDFS == 0);
        btnNextDFS.setDisable(indiceSolucionDFS >= resultadoDFS.getCaminoSolucion().size() - 1);
    }
}
