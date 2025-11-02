package edu.ai.misioneros.logicaVoraz;

import edu.ai.misioneros.modelo.Estado;
import edu.ai.misioneros.modelo.Nodo;
import edu.ai.misioneros.modelo.ResultadoBusqueda;

import java.util.*;

/**
 * Algoritmo Voraz: H = g + n, donde:
 * - g = misioneros + canibales en el lado izquierdo
 * - n = misioneros + canibales en el lado derecho
 * Genera todos los hijos para visualización y explora siempre el hijo con H
 * mínimo (seleccionando de izquierda a derecha en caso de empate).
 * Solo aplica cuando misioneros >= canibales en ambos lados.
 */
public class AlgoritmoVoraz {

    public ResultadoBusqueda resolver() {
        long inicio = System.nanoTime();

        Estado inicial = new Estado(3, 3, 0);
        int h0 = heuristica(inicial); // H = g + n
        int f0 = h0; // H = g + n, donde g = izquierda, n = derecha
        Nodo raiz = new Nodo(inicial, null, 0, h0, f0, "Inicio");

        List<Nodo> todos = new ArrayList<>();
        Set<Estado> visitados = new HashSet<>();
        todos.add(raiz);

        Nodo objetivo = vorazMinimoH(raiz, todos, visitados);

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

    private Nodo vorazMinimoH(Nodo actual, List<Nodo> todos, Set<Estado> visitados) {
        if (!actual.getEstado().esValido())
            return null;
        if (actual.getEstado().esObjetivo())
            return actual;
        
        // Solo continuar si misioneros >= canibales en ambos lados
        Estado estadoActual = actual.getEstado();
        int mL = estadoActual.getMisionerosIzquierda();
        int cL = estadoActual.getCanibalesIzquierda();
        int mR = estadoActual.getMisionerosDerecha();
        int cR = estadoActual.getCanibalesDerecha();
        
        boolean valido = (mL == 0 || mL >= cL) && (mR == 0 || mR >= cR);
        if (!valido) {
            return null;
        }
        
        visitados.add(estadoActual);

        List<Nodo> hijos = expandir(actual);

        for (Nodo h : hijos) {
            actual.agregarHijo(h);
            todos.add(h);
        }

        // Filtrar hijos válidos (donde misioneros >= canibales en ambos lados)
        List<Nodo> hijosValidos = new ArrayList<>();
        Map<Nodo, Integer> indiceOriginal = new HashMap<>();
        for (int i = 0; i < hijos.size(); i++) {
            Nodo h = hijos.get(i);
            Estado estadoHijo = h.getEstado();
            int mLH = estadoHijo.getMisionerosIzquierda();
            int cLH = estadoHijo.getCanibalesIzquierda();
            int mRH = estadoHijo.getMisionerosDerecha();
            int cRH = estadoHijo.getCanibalesDerecha();
            
            boolean validoHijo = estadoHijo.esValido() && 
                                (mLH == 0 || mLH >= cLH) && 
                                (mRH == 0 || mRH >= cRH);
            if (validoHijo && !visitados.contains(estadoHijo)) {
                hijosValidos.add(h);
                indiceOriginal.put(h, i);
            }
        }
        
        // Ordenar por H ascendente (menor primero), manteniendo orden izquierda-derecha
        final Map<Nodo, Integer> indiceMap = indiceOriginal;
        hijosValidos.sort(Comparator.comparingInt(Nodo::getFh).thenComparingInt(indiceMap::get));
        
        // Seleccionar el primero (menor H, o si hay empate, el más a la izquierda)
        for (Nodo h : hijosValidos) {
            Nodo sol = vorazMinimoH(h, todos, visitados);
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

    /**
     * Calcula H = g + n donde:
     * - g = misioneros + canibales en el lado izquierdo
     * - n = misioneros + canibales en el lado derecho
     */
    private int heuristica(Estado e) {
        // g = misioneros + canibales en el lado izquierdo
        int g = e.getMisionerosIzquierda() + e.getCanibalesIzquierda();
        // n = misioneros + canibales en el lado derecho
        int n = e.getMisionerosDerecha() + e.getCanibalesDerecha();
        // H = g + n
        return g + n;
    }

    private Nodo crearNodo(Nodo padre, Estado hijoEstado, String operador) {
        int nivel = padre.getNivel() + 1;
        int h = heuristica(hijoEstado); // H = g + n
        // Para voraz, FH = H (usamos H como el valor de fh)
        int fh = h; // FH = H = g + n
        return new Nodo(hijoEstado, padre, nivel, h, fh, operador);
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
