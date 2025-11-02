package edu.ai.misioneros.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo del árbol de búsqueda.
 */
public class Nodo {
    private final Estado estado;
    private final Nodo padre;
    private final int nivel; // g
    private final int h;     // heurística
    private final int fh;    // f = g + h + penalizaciones
    private final String operador; // descripción del movimiento realizado para llegar aquí
    private final List<Nodo> hijos = new ArrayList<>();

    public Nodo(Estado estado, Nodo padre, int nivel, int h, int fh, String operador) {
        this.estado = estado;
        this.padre = padre;
        this.nivel = nivel;
        this.h = h;
        this.fh = fh;
        this.operador = operador;
    }

    public Estado getEstado() { return estado; }
    public Nodo getPadre() { return padre; }
    public int getNivel() { return nivel; }
    public int getH() { return h; }
    public int getFh() { return fh; }
    /**
     * g voraz: número de misioneros + caníbales en el lado izquierdo
     */
    public int getGreedyG() {
        int mL = estado.getMisionerosIzquierda();
        int cL = estado.getCanibalesIzquierda();
        return mL + cL;
    }

    /**
     * n voraz: número de misioneros + caníbales en el lado derecho
     */
    public int getGreedyN() {
        int mL = estado.getMisionerosIzquierda();
        int cL = estado.getCanibalesIzquierda();
        int mR = 3 - mL;
        int cR = 3 - cL;
        return mR + cR;
    }

    /**
     * H voraz = g + n según tu especificación
     */
    public int getGreedyH() {
        return getGreedyG() + getGreedyN();
    }
    public String getOperador() { return operador; }
    public List<Nodo> getHijos() { return hijos; }

    public void agregarHijo(Nodo hijo) { hijos.add(hijo); }
}



