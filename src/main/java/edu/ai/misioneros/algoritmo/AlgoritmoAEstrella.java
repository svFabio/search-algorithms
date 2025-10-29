package edu.ai.misioneros.algoritmo;

import edu.ai.misioneros.modelo.Estado;
import edu.ai.misioneros.modelo.Nodo;
import edu.ai.misioneros.modelo.ResultadoBusqueda;

import java.util.*;

/**
 * Implementación del algoritmo A* para Misioneros y Caníbales.
 */
public class AlgoritmoAEstrella {

    public ResultadoBusqueda resolver() {
        long inicio = System.nanoTime();

        Estado inicial = new Estado(3, 3, 0);
        int h0 = heuristica(inicial);
        int f0 = h0; // g=0 y sin penalizaciones
        Nodo raiz = new Nodo(inicial, null, 0, h0, f0, "Inicio");

        PriorityQueue<Nodo> abiertos = new PriorityQueue<>(Comparator.comparingInt(Nodo::getFh).reversed());
        Set<Estado> cerrados = new HashSet<>();
        List<Nodo> todos = new ArrayList<>();

        abiertos.add(raiz);
        todos.add(raiz);

        Nodo objetivo = null;

        while (!abiertos.isEmpty()) {
            Nodo actual = abiertos.poll();
            cerrados.add(actual.getEstado());

            if (actual.getEstado().esObjetivo()) {
                objetivo = actual;
                break;
            }

            // Generar todos los hijos posibles
            List<Nodo> hijos = expandir(actual);

            // Ordenar hijos por FH descendente (mayor FH primero)
            hijos.sort(Comparator.comparingInt(Nodo::getFh).reversed());

            for (Nodo hijo : hijos) {
                // Siempre agregar el hijo al nodo padre para visualización
                actual.agregarHijo(hijo);
                todos.add(hijo);

                // Solo agregar a abiertos si es válido y no está en cerrados
                if (hijo.getEstado().esValido() && !cerrados.contains(hijo.getEstado())) {
                    abiertos.add(hijo);
                }
            }
        }

        List<Nodo> camino = reconstruirCamino(objetivo);
        long fin = System.nanoTime();

        return new ResultadoBusqueda(
                raiz,
                camino,
                todos,
                cerrados.size(),
                abiertos.size(),
                cerrados.size(),
                fin - inicio);
    }

    private int heuristica(Estado e) {
        // h(estado) = 6 - 2*M - 2*C
        int h = 6 - 2 * e.getMisionerosIzquierda() - 2 * e.getCanibalesIzquierda();
        return h;
    }

    private int penalizaciones(Estado e) {
        int mL = e.getMisionerosIzquierda();
        int cL = e.getCanibalesIzquierda();
        int mR = 3 - mL;
        int cR = 3 - cL;
        int pen = 0;
        if (cL > mL && mL > 0)
            pen -= 1000; // izquierda inválida
        if (mR > cR && mR > 0)
            pen -= 1000; // derecha inválida
        return pen;
    }

    private List<Nodo> expandir(Nodo nodo) {
        Estado e = nodo.getEstado();
        List<Nodo> hijos = new ArrayList<>();
        int M = e.getMisionerosIzquierda();
        int C = e.getCanibalesIzquierda();
        int L = e.getLanchaLado();

        // Generar operadores según lado de la lancha
        if (L == 0) {
            // 1. (M-1, C, 1)
            if (M > 0)
                hijos.add(crearNodo(nodo, new Estado(M - 1, C, 1), "Mover 1 misionero a la derecha"));
            // 2. (M-2, C, 1)
            if (M > 1)
                hijos.add(crearNodo(nodo, new Estado(M - 2, C, 1), "Mover 2 misioneros a la derecha"));
            // 3. (M, C-1, 1)
            if (C > 0)
                hijos.add(crearNodo(nodo, new Estado(M, C - 1, 1), "Mover 1 caníbal a la derecha"));
            // 4. (M, C-2, 1)
            if (C > 1)
                hijos.add(crearNodo(nodo, new Estado(M, C - 2, 1), "Mover 2 caníbales a la derecha"));
            // 5. (M-1, C-1, 1)
            if (M > 0 && C > 0)
                hijos.add(crearNodo(nodo, new Estado(M - 1, C - 1, 1), "Mover 1 misionero y 1 caníbal a la derecha"));
        } else {
            // 6. (M+1, C, 0)
            if (M < 3)
                hijos.add(crearNodo(nodo, new Estado(M + 1, C, 0), "Mover 1 misionero a la izquierda"));
            // 7. (M+2, C, 0)
            if (M < 2)
                hijos.add(crearNodo(nodo, new Estado(M + 2, C, 0), "Mover 2 misioneros a la izquierda"));
            // 8. (M, C+1, 0)
            if (C < 3)
                hijos.add(crearNodo(nodo, new Estado(M, C + 1, 0), "Mover 1 caníbal a la izquierda"));
            // 9. (M, C+2, 0)
            if (C < 2)
                hijos.add(crearNodo(nodo, new Estado(M, C + 2, 0), "Mover 2 caníbales a la izquierda"));
            // 10. (M+1, C+1, 0)
            if (M < 3 && C < 3)
                hijos.add(crearNodo(nodo, new Estado(M + 1, C + 1, 0), "Mover 1 misionero y 1 caníbal a la izquierda"));
        }

        return hijos;
    }

    private Nodo crearNodo(Nodo padre, Estado hijoEstado, String operador) {
        int g = padre.getNivel() + 1;
        int h = heuristica(hijoEstado);
        int f = g + h + penalizaciones(hijoEstado);
        return new Nodo(hijoEstado, padre, g, h, f, operador);
    }

    private List<Nodo> reconstruirCamino(Nodo objetivo) {
        List<Nodo> camino = new ArrayList<>();
        if (objetivo == null)
            return camino;
        Nodo cur = objetivo;
        while (cur != null) {
            camino.add(cur);
            cur = cur.getPadre();
        }
        Collections.reverse(camino);
        return camino;
    }
}
