package edu.ai.misioneros.logicaVoraz;

import edu.ai.misioneros.modelo.Estado;
import edu.ai.misioneros.modelo.Nodo;
import edu.ai.misioneros.modelo.ResultadoBusqueda;

import java.util.*;

/**
 * Algoritmo Voraz: FH = g + n, donde n = M + C (piezas restantes en la
 * izquierda).
 * Genera todos los hijos para visualización y explora siempre el hijo con FH
 * mínimo.
 */
public class AlgoritmoVoraz {

    public ResultadoBusqueda resolver() {
        long inicio = System.nanoTime();

        Estado inicial = new Estado(3, 3, 0);
        int h0 = heuristica(inicial); // n = M + C
        int f0 = h0; // FH = g + n, g=0
        Nodo raiz = new Nodo(inicial, null, 0, h0, f0, "Inicio");

        List<Nodo> todos = new ArrayList<>();
        Set<Estado> visitados = new HashSet<>();
        todos.add(raiz);

        Nodo objetivo = vorazMinimoFH(raiz, todos, visitados);

        List<Nodo> camino = reconstruir(objetivo);
        long fin = System.nanoTime();

        return new ResultadoBusqueda(
                raiz,
                camino,
                todos,
                visitados.size(),
                0,
                visitados.size(),
                fin - inicio);
    }

    private Nodo vorazMinimoFH(Nodo actual, List<Nodo> todos, Set<Estado> visitados) {
        if (!actual.getEstado().esValido())
            return null;
        if (actual.getEstado().esObjetivo())
            return actual;
        visitados.add(actual.getEstado());

        List<Nodo> hijos = expandir(actual);

        for (Nodo h : hijos) {
            actual.agregarHijo(h);
            todos.add(h);
        }

        hijos.sort(Comparator.comparingInt(Nodo::getFh));
        for (Nodo h : hijos) {
            if (!h.getEstado().esValido())
                continue;
            if (visitados.contains(h.getEstado()))
                continue;
            Nodo sol = vorazMinimoFH(h, todos, visitados);
            if (sol != null)
                return sol;
        }
        return null;
    }

    private List<Nodo> expandir(Nodo nodo) {
        Estado e = nodo.getEstado();
        List<Nodo> hijos = new ArrayList<>();
        int M = e.getMisionerosIzquierda();
        int C = e.getCanibalesIzquierda();
        int L = e.getLanchaLado();

        if (L == 0) {
            if (M > 0)
                hijos.add(crearNodo(nodo, new Estado(M - 1, C, 1), "Mover 1 misionero a la derecha"));
            if (M > 1)
                hijos.add(crearNodo(nodo, new Estado(M - 2, C, 1), "Mover 2 misioneros a la derecha"));
            if (C > 0)
                hijos.add(crearNodo(nodo, new Estado(M, C - 1, 1), "Mover 1 caníbal a la derecha"));
            if (C > 1)
                hijos.add(crearNodo(nodo, new Estado(M, C - 2, 1), "Mover 2 caníbales a la derecha"));
            if (M > 0 && C > 0)
                hijos.add(crearNodo(nodo, new Estado(M - 1, C - 1, 1), "Mover 1 misionero y 1 caníbal a la derecha"));
        } else {
            if (M < 3)
                hijos.add(crearNodo(nodo, new Estado(M + 1, C, 0), "Mover 1 misionero a la izquierda"));
            if (M < 2)
                hijos.add(crearNodo(nodo, new Estado(M + 2, C, 0), "Mover 2 misioneros a la izquierda"));
            if (C < 3)
                hijos.add(crearNodo(nodo, new Estado(M, C + 1, 0), "Mover 1 caníbal a la izquierda"));
            if (C < 2)
                hijos.add(crearNodo(nodo, new Estado(M, C + 2, 0), "Mover 2 caníbales a la izquierda"));
            if (M < 3 && C < 3)
                hijos.add(crearNodo(nodo, new Estado(M + 1, C + 1, 0), "Mover 1 misionero y 1 caníbal a la izquierda"));
        }

        return hijos;
    }

    private int heuristica(Estado e) {
        return e.getMisionerosIzquierda() + e.getCanibalesIzquierda();
    }

    private Nodo crearNodo(Nodo padre, Estado hijoEstado, String operador) {
        int g = padre.getNivel() + 1;
        int h = heuristica(hijoEstado);
        int f = g + h; // FH = g + n
        return new Nodo(hijoEstado, padre, g, h, f, operador);
    }

    private List<Nodo> reconstruir(Nodo objetivo) {
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

