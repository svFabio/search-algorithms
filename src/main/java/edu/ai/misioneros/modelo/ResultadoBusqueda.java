package edu.ai.misioneros.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Contiene el árbol completo (desde la raíz) y el camino solución.
 */
public class ResultadoBusqueda {
    private final Nodo raiz;
    private final List<Nodo> caminoSolucion;
    private final List<Nodo> todosLosNodosGenerados;
    private final int nodosExplorados;
    private final int nodosAbiertos;
    private final int nodosCerrados;
    private final long tiempoNs;

    public ResultadoBusqueda(Nodo raiz,
                             List<Nodo> caminoSolucion,
                             List<Nodo> todosLosNodosGenerados,
                             int nodosExplorados,
                             int nodosAbiertos,
                             int nodosCerrados,
                             long tiempoNs) {
        this.raiz = raiz;
        this.caminoSolucion = caminoSolucion != null ? caminoSolucion : new ArrayList<>();
        this.todosLosNodosGenerados = todosLosNodosGenerados != null ? todosLosNodosGenerados : new ArrayList<>();
        this.nodosExplorados = nodosExplorados;
        this.nodosAbiertos = nodosAbiertos;
        this.nodosCerrados = nodosCerrados;
        this.tiempoNs = tiempoNs;
    }

    public Nodo getRaiz() { return raiz; }
    public List<Nodo> getCaminoSolucion() { return caminoSolucion; }
    public List<Nodo> getTodosLosNodosGenerados() { return todosLosNodosGenerados; }
    public int getNodosExplorados() { return nodosExplorados; }
    public int getNodosAbiertos() { return nodosAbiertos; }
    public int getNodosCerrados() { return nodosCerrados; }
    public long getTiempoMs() { return tiempoNs / 1_000_000; }
    public long getTiempoNs() { return tiempoNs; }
}



