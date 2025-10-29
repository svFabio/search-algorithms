package edu.ai.misioneros.controlador;

import edu.ai.misioneros.algoritmo.AlgoritmoAEstrella;
import edu.ai.misioneros.algoritmo.AlgoritmoProfundidad;
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

    private final Button btnBack = new Button("◀ Back");
    private final Button btnNext = new Button("Next ▶");

    // Botones de zoom
    private final Button btnZoomIn = new Button("🔍+");
    private final Button btnZoomOut = new Button("🔍-");
    private final Button btnZoomReset = new Button("🔄");

    private final PanelInformacion panelInfo = new PanelInformacion();

    private ResultadoBusqueda resultadoDFS;
    private ResultadoBusqueda resultadoAStar;
    private int indiceSolucion = 0;

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

        // Panel izquierdo (DFS)
        VBox izquierda = crearPanelAlgoritmo("Algoritmo por Profundidad", panelArbolDFS);
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
            HBox nav = new HBox(10, btnBack, btnNext);
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

        // Para DFS, solo título y scroll
        VBox box = new VBox(8, lblTitulo, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        box.setPadding(new Insets(10));
        return box;
    }

    private void wireEventos() {
        btnResolver.setOnAction(e -> resolverCompleto());
        btnPaso.setOnAction(e -> pasoAPaso());
        btnReiniciar.setOnAction(e -> reiniciar());
        btnBack.setOnAction(e -> moverIndice(-1));
        btnNext.setOnAction(e -> moverIndice(1));

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
            AlgoritmoProfundidad dfs = new AlgoritmoProfundidad();
            resultadoDFS = dfs.resolver();
            long fin = System.nanoTime();
            long tiempo = fin - inicio;
            System.out.println("DFS terminó en " + tiempo / 1_000_000.0 + " ms (" + tiempo + " ns)");
            panelInfo.actualizarTiempoDFS(tiempo);
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

        indiceSolucion = 0;
        actualizarSeleccionActual();
        panelInfo.actualizarResultado(resultadoAStar);
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
        indiceSolucion = 0;

        // Mostrar solo la raíz inicialmente
        panelArbolAStar.setDatos(resultadoAStar.getRaiz(), resultadoAStar.getCaminoSolucion());

        // Actualizar la información
        actualizarSeleccionActual();
        panelInfo.actualizarNodoActual(resultadoAStar.getRaiz());
    }

    private void reiniciar() {
        resultadoDFS = null;
        resultadoAStar = null;
        indiceSolucion = 0;
        panelArbolDFS.setDatos(null, null);
        panelArbolAStar.setDatos(null, null);
        panelInfo.actualizarNodoActual(null);
        panelInfo.actualizarResultado(null);
    }

    private void moverIndice(int delta) {
        if (resultadoAStar == null)
            return;
        List<Nodo> camino = resultadoAStar.getCaminoSolucion();
        if (camino.isEmpty())
            return;

        if (delta > 0) {
            // Avanzar: mostrar el siguiente nodo y sus ramas
            if (indiceSolucion < camino.size() - 1) {
                indiceSolucion++;
                Nodo actual = camino.get(indiceSolucion);

                // Expandir desde el nodo actual para mostrar sus ramas
                panelArbolAStar.expandirDesde(actual);
                panelArbolAStar.resaltarNodoActual(actual);
                panelInfo.actualizarNodoActual(actual);
            }
        } else {
            // Retroceder: mostrar el nodo anterior
            if (indiceSolucion > 0) {
                indiceSolucion--;
                Nodo actual = camino.get(indiceSolucion);
                panelArbolAStar.resaltarNodoActual(actual);
                panelInfo.actualizarNodoActual(actual);
            }
        }

        // Actualizar estado de los botones
        btnBack.setDisable(indiceSolucion == 0);
        btnNext.setDisable(indiceSolucion >= camino.size() - 1);
    }

    private void actualizarSeleccionActual() {
        if (resultadoAStar == null || resultadoAStar.getCaminoSolucion().isEmpty())
            return;
        Nodo actual = resultadoAStar.getCaminoSolucion().get(indiceSolucion);
        panelArbolAStar.resaltarNodoActual(actual);
        panelInfo.actualizarNodoActual(actual);
        btnBack.setDisable(indiceSolucion == 0);
        btnNext.setDisable(indiceSolucion >= resultadoAStar.getCaminoSolucion().size() - 1);
    }
}
