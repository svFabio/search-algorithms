package edu.ai.misioneros.vista;

import edu.ai.misioneros.modelo.Nodo;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import java.util.*;

/**
 * Dibuja el árbol de búsqueda con nodos en niveles y conexiones.
 * Muestra solo el camino solución inicialmente, expandiendo gradualmente.
 */
public class PanelArbol extends Pane {
    private static final double NODE_WIDTH = 210; // Ancho del nodo completo
    private static final double NODE_HEIGHT = 170; // Alto del nodo completo
    private static final double H_SPACING = 220; // Separación horizontal entre nodos del mismo nivel
    private static final double V_SPACING = 180; // Separación vertical entre niveles

    private Nodo raiz;
    private List<Nodo> caminoSolucion = new ArrayList<>();
    private Nodo nodoActual;
    private Set<Nodo> nodosVisibles = new HashSet<>(); // Solo mostrar ciertos nodos

    // Variables para zoom y ajuste automático
    private double escalaActual = 1.0;
    private double escalaMinima = 0.3;
    private double escalaMaxima = 3.0;
    private Group contenidoArbol;
    private Scale transformacionEscala;
    // Flag para indicar que el usuario hizo zoom o pan manual y evitar reajustes automáticos
    private boolean zoomManual = false;

    // Variables para arrastrar
    private double ultimoX = 0;
    private double ultimoY = 0;
    private boolean arrastrando = false;

    public PanelArbol() {
        setStyle("-fx-background-color: white;");

        // Inicializar el grupo de contenido y la transformación de escala
        contenidoArbol = new Group();
        transformacionEscala = new Scale();
        contenidoArbol.getTransforms().add(transformacionEscala);
        getChildren().add(contenidoArbol);

        // Configurar eventos de zoom con la rueda del mouse
        setOnScroll(event -> {
            double factorZoom = event.getDeltaY() > 0 ? 1.1 : 0.9;
            aplicarZoom(factorZoom);
            // Marcar interacción manual para no sobrescribir el zoom automáticamente
            zoomManual = true;
            event.consume();
        });

        // Configurar eventos de arrastrar
        setOnMousePressed(event -> {
            ultimoX = event.getX();
            ultimoY = event.getY();
            arrastrando = true;
            event.consume();
        });

        setOnMouseDragged(event -> {
            if (arrastrando) {
                double deltaX = event.getX() - ultimoX;
                double deltaY = event.getY() - ultimoY;

                contenidoArbol.setLayoutX(contenidoArbol.getLayoutX() + deltaX);
                contenidoArbol.setLayoutY(contenidoArbol.getLayoutY() + deltaY);

                ultimoX = event.getX();
                ultimoY = event.getY();
                // Usuario hizo pan: considerar zoom/pan como manual
                zoomManual = true;
            }
            event.consume();
        });

        setOnMouseReleased(event -> {
            arrastrando = false;
            event.consume();
        });
    }

    /**
     * Aplica zoom al árbol
     */
    public void aplicarZoom(double factor) {
        double nuevaEscala = escalaActual * factor;
        nuevaEscala = Math.max(escalaMinima, Math.min(escalaMaxima, nuevaEscala));

        if (nuevaEscala != escalaActual) {
            escalaActual = nuevaEscala;
            transformacionEscala.setX(escalaActual);
            transformacionEscala.setY(escalaActual);
        }
    }

    /**
     * Ajusta automáticamente el zoom para que todo el árbol sea visible
     */
    public void ajustarZoomAutomatico() {
        // Si el usuario ya interactuó manualmente (zoom/pan), no forzar ajuste automático
        if (zoomManual) return;

        if (raiz == null || nodosVisibles.isEmpty())
            return;

        Map<Nodo, double[]> posiciones = calcularPosiciones();
        if (posiciones.isEmpty())
            return;

        // Calcular los límites del árbol
        double minX = posiciones.values().stream().mapToDouble(v -> v[0]).min().orElse(0);
        double maxX = posiciones.values().stream().mapToDouble(v -> v[0] + NODE_WIDTH).max().orElse(0);
        double minY = posiciones.values().stream().mapToDouble(v -> v[1]).min().orElse(0);
        double maxY = posiciones.values().stream().mapToDouble(v -> v[1] + NODE_HEIGHT).max().orElse(0);

        double anchoArbol = maxX - minX;
        double altoArbol = maxY - minY;

        // Obtener el tamaño disponible del panel
        double anchoDisponible = getWidth() > 0 ? getWidth() - 100 : 800; // Margen de 50px por lado
        double altoDisponible = getHeight() > 0 ? getHeight() - 100 : 600; // Margen de 50px por lado

        // Calcular la escala necesaria para ajustar el árbol al panel
        double escalaX = anchoDisponible / anchoArbol;
        double escalaY = altoDisponible / altoArbol;
        double escalaOptima = Math.min(escalaX, escalaY);

        // Aplicar la escala óptima con límites
        escalaOptima = Math.max(escalaMinima, Math.min(escalaMaxima, escalaOptima));
        escalaActual = escalaOptima;

        transformacionEscala.setX(escalaActual);
        transformacionEscala.setY(escalaActual);

        // Centrar el árbol en el panel
        double offsetX = (anchoDisponible - anchoArbol * escalaActual) / 2 - minX * escalaActual;
        double offsetY = (altoDisponible - altoArbol * escalaActual) / 2 - minY * escalaActual;

        contenidoArbol.setLayoutX(Math.max(0, offsetX));
        contenidoArbol.setLayoutY(Math.max(0, offsetY));
    }

    /**
     * Resetea el zoom a escala normal
     */
    public void resetearZoom() {
        escalaActual = 1.0;
        zoomManual = false;
        transformacionEscala.setX(escalaActual);
        transformacionEscala.setY(escalaActual);
        contenidoArbol.setLayoutX(0);
        contenidoArbol.setLayoutY(0);
    }

    public void setDatos(Nodo raiz, List<Nodo> camino) {
        this.raiz = raiz;
        this.caminoSolucion.clear();
        if (camino != null) {
            this.caminoSolucion.addAll(camino);
        }
        this.nodoActual = null;
        this.nodosVisibles.clear();
        if (raiz != null) {
            this.nodosVisibles.add(raiz);
        }
        redibujar();
    }

    public void resaltarNodoActual(Nodo actual) {
        this.nodoActual = actual;
        // Mostrar el nodo actual, su padre, y TODOS sus hijos
        if (actual != null) {
            this.nodosVisibles.add(actual);
            if (actual.getPadre() != null) {
                this.nodosVisibles.add(actual.getPadre());
            }
            // Agregar TODOS los hijos, no solo los del camino
            for (Nodo hijo : actual.getHijos()) {
                this.nodosVisibles.add(hijo);
            }
        }
        redibujar();
    }

    public void expandirDesde(Nodo desde) {
        // Expander recursivamente hasta el nodo desde
        expandirRecursivo(desde, raiz);
        this.nodoActual = desde;
        redibujar();
    }

    private void expandirRecursivo(Nodo objetivo, Nodo actual) {
        if (actual == null)
            return;
        nodosVisibles.add(actual);
        if (actual == objetivo)
            return;

        // Si hay hijos en el camino, seguir por ese camino
        for (Nodo hijo : actual.getHijos()) {
            if (caminoSolucion.contains(hijo)) {
                expandirRecursivo(objetivo, hijo);
                break;
            }
        }
    }

    private void redibujar() {
        contenidoArbol.getChildren().clear();
        if (raiz == null)
            return;

        Map<Nodo, double[]> posiciones = calcularPosiciones();

        Group edges = new Group();
        Group nodes = new Group();

        // Conexiones solo entre nodos visibles
        for (Map.Entry<Nodo, double[]> entry : posiciones.entrySet()) {
            Nodo n = entry.getKey();
            double[] p = entry.getValue();
            if (n.getPadre() != null && nodosVisibles.contains(n.getPadre())) {
                double[] pp = posiciones.get(n.getPadre());
                if (pp != null) {
                    Line line = new Line(pp[0] + NODE_WIDTH / 2, pp[1] + NODE_HEIGHT - 30,
                            p[0] + NODE_WIDTH / 2, p[1]);
                    line.setStroke(Color.web("#64748B"));
                    line.setStrokeWidth(1.5);
                    edges.getChildren().add(line);
                }
            }
        }

        // Nodos visibles
        for (Map.Entry<Nodo, double[]> entry : posiciones.entrySet()) {
            Nodo n = entry.getKey();
            double[] p = entry.getValue();
            Group g = crearNodoVisual(n);
            g.setLayoutX(p[0]);
            g.setLayoutY(p[1]);
            nodes.getChildren().add(g);
        }

        contenidoArbol.getChildren().addAll(edges, nodes);

        // Ajustar tamaño preferido para scroll
        double maxX = posiciones.values().stream().mapToDouble(v -> v[0]).max().orElse(0);
        double maxY = posiciones.values().stream().mapToDouble(v -> v[1]).max().orElse(0);
        setPrefSize(Math.max(1200, maxX + NODE_WIDTH + 50), Math.max(800, maxY + NODE_HEIGHT + 50));

        // Ajustar zoom automáticamente después de redibujar
        Platform.runLater(() -> ajustarZoomAutomatico());
    }

    private Group crearNodoVisual(Nodo n) {
        Group cont = new Group();

        PanelMatriz matriz = new PanelMatriz(n.getEstado());
        matriz.setLayoutX(10);
        matriz.setLayoutY(10);

        Rectangle marco = new Rectangle(NODE_WIDTH, NODE_HEIGHT);
        marco.setFill(Color.WHITE);
        marco.setStroke(Color.BLACK);
        marco.setStrokeWidth(1);
        marco.setArcWidth(8);
        marco.setArcHeight(8);

        // Estilos según tipo de nodo
        boolean esInicial = n.getPadre() == null;
        boolean esObjetivo = n.getEstado().esObjetivo();
        boolean enCamino = caminoSolucion.contains(n);
        if (esInicial) {
            marco.setStroke(Color.web("#2563EB"));
            marco.setStrokeWidth(3);
        }
        if (esObjetivo) {
            marco.setStroke(Color.web("#16A34A"));
            marco.setStrokeWidth(3);
        }
        if (enCamino) {
            marco.setFill(Color.web("#D1FAE5"));
            marco.setStroke(Color.web("#10B981"));
            marco.setStrokeWidth(2);
        }
        if (nodoActual == n) {
            marco.setStroke(Color.web("#F59E0B"));
            marco.setStrokeWidth(4);
        }

        Label lblEstado = new Label(n.getEstado().toString());
        lblEstado.setLayoutX(10);
        lblEstado.setLayoutY(matriz.getPrefHeight() + 15);
        lblEstado.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

    Label lblValores = new Label("g=" + n.getGreedyG() + " n=" + n.getGreedyN() + " H=" + n.getGreedyH() + "  (nivel=" + n.getNivel() + ")");
        lblValores.setLayoutX(10);
        lblValores.setLayoutY(matriz.getPrefHeight() + 32);
        lblValores.setStyle("-fx-font-size: 10px;");

        cont.getChildren().addAll(marco, matriz, lblEstado, lblValores);
        return cont;
    }

    private Map<Nodo, double[]> calcularPosiciones() {
        Map<Nodo, double[]> pos = new HashMap<>();
        if (nodosVisibles.isEmpty())
            return pos;
        if (raiz == null)
            return pos;

        // Agrupar por nivel
        Map<Integer, List<Nodo>> niveles = new HashMap<>();
        for (Nodo n : nodosVisibles) {
            niveles.computeIfAbsent(n.getNivel(), k -> new ArrayList<>()).add(n);
        }

        // Calcular posiciones de manera jerárquica
        double startX = 50;

        // Recorrer desde la raíz hacia abajo asignando posiciones
        asignarPosicionesRecursivo(raiz, null, pos, startX, 30);

        return pos;
    }

    private double asignarPosicionesRecursivo(Nodo nodo, Nodo padre, Map<Nodo, double[]> pos, double xInicial,
            double yActual) {
        if (!nodosVisibles.contains(nodo)) {
            return xInicial;
        }

        // Ya tiene posición asignada
        if (pos.containsKey(nodo)) {
            return pos.get(nodo)[0] + NODE_WIDTH / 2;
        }

        List<Nodo> hijosVisibles = new ArrayList<>();
        for (Nodo hijo : nodo.getHijos()) {
            if (nodosVisibles.contains(hijo)) {
                hijosVisibles.add(hijo);
            }
        }

        if (hijosVisibles.isEmpty()) {
            // Nodo hoja, centrarlo
            pos.put(nodo, new double[] { xInicial, yActual });
            return xInicial + NODE_WIDTH;
        } else {
            // Distribuir hijos horizontalmente
            double espacioNecesario = hijosVisibles.size() * H_SPACING;
            double xCentral = xInicial + espacioNecesario / 2 - NODE_WIDTH / 2;

            pos.put(nodo, new double[] { xCentral, yActual });

            // Asignar posiciones a los hijos
            double posHijo = xInicial;
            for (Nodo hijo : hijosVisibles) {
                posHijo = asignarPosicionesRecursivo(hijo, nodo, pos, posHijo, yActual + V_SPACING);
                posHijo += 50; // Espacio mínimo entre hermanos
            }

            return xInicial + espacioNecesario;
        }
    }
}
