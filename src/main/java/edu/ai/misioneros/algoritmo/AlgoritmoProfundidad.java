package edu.ai.misioneros.algoritmo;

import edu.ai.misioneros.modelo.Estado;
import edu.ai.misioneros.modelo.Nodo;
import edu.ai.misioneros.modelo.ResultadoBusqueda;

import java.util.*;

/**
 * Búsqueda en profundidad (DFS) simple para comparación visual.
 */
public class AlgoritmoProfundidad {

    public ResultadoBusqueda resolver() {
        long inicio = System.nanoTime();
        Estado inicial = new Estado(3, 3, 0);
        Nodo raiz = new Nodo(inicial, null, 0, 0, 0, "Inicio");

        List<Nodo> todos = new ArrayList<>();
        Set<Estado> visitados = new HashSet<>();
        Nodo objetivo = dfs(raiz, todos, visitados);

        List<Nodo> camino = reconstruir(objetivo);
        long fin = System.nanoTime();
        return new ResultadoBusqueda(raiz, camino, todos, visitados.size(), 0, visitados.size(), fin - inicio);
    }

    private Nodo dfs(Nodo nodo, List<Nodo> todos, Set<Estado> visitados) {
        todos.add(nodo);
        if (!nodo.getEstado().esValido()) return null;
        if (nodo.getEstado().esObjetivo()) return nodo;
        visitados.add(nodo.getEstado());

        for (Nodo hijo : expandir(nodo)) {
            nodo.agregarHijo(hijo);
            if (visitados.contains(hijo.getEstado())) continue;
            Nodo sol = dfs(hijo, todos, visitados);
            if (sol != null) return sol;
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
            if (M > 0) hijos.add(n(nodo, new Estado(M - 1, C, 1), "1M→"));
            if (M > 1) hijos.add(n(nodo, new Estado(M - 2, C, 1), "2M→"));
            if (C > 0) hijos.add(n(nodo, new Estado(M, C - 1, 1), "1C→"));
            if (C > 1) hijos.add(n(nodo, new Estado(M, C - 2, 1), "2C→"));
            if (M > 0 && C > 0) hijos.add(n(nodo, new Estado(M - 1, C - 1, 1), "1M1C→"));
        } else {
            if (M < 3) hijos.add(n(nodo, new Estado(M + 1, C, 0), "1M←"));
            if (M < 2) hijos.add(n(nodo, new Estado(M + 2, C, 0), "2M←"));
            if (C < 3) hijos.add(n(nodo, new Estado(M, C + 1, 0), "1C←"));
            if (C < 2) hijos.add(n(nodo, new Estado(M, C + 2, 0), "2C←"));
            if (M < 3 && C < 3) hijos.add(n(nodo, new Estado(M + 1, C + 1, 0), "1M1C←"));
        }
        return hijos;
    }

    private Nodo n(Nodo padre, Estado e, String op) {
        int g = padre.getNivel() + 1;
        return new Nodo(e, padre, g, 0, 0, op);
    }

    private List<Nodo> reconstruir(Nodo objetivo) {
        List<Nodo> camino = new ArrayList<>();
        if (objetivo == null) return camino;
        Nodo cur = objetivo;
        while (cur != null) { camino.add(cur); cur = cur.getPadre(); }
        Collections.reverse(camino);
        return camino;
    }
}



