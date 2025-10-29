package edu.ai.misioneros.modelo;

import java.util.Objects;

/**
 * Representa un estado del problema: (misionerosIzquierda, canibalesIzquierda, lanchaLado)
 * L: 0 = izquierda, 1 = derecha
 */
public class Estado {
    private final int misionerosIzquierda;
    private final int canibalesIzquierda;
    private final int lanchaLado; // 0 izquierda, 1 derecha

    public Estado(int misionerosIzquierda, int canibalesIzquierda, int lanchaLado) {
        this.misionerosIzquierda = misionerosIzquierda;
        this.canibalesIzquierda = canibalesIzquierda;
        this.lanchaLado = lanchaLado;
    }

    public int getMisionerosIzquierda() { return misionerosIzquierda; }
    public int getCanibalesIzquierda() { return canibalesIzquierda; }
    public int getLanchaLado() { return lanchaLado; }

    public int getMisionerosDerecha() { return 3 - misionerosIzquierda; }
    public int getCanibalesDerecha() { return 3 - canibalesIzquierda; }

    public boolean esValido() {
        int mL = misionerosIzquierda;
        int cL = canibalesIzquierda;
        int mR = 3 - mL;
        int cR = 3 - cL;

        boolean izqOk = (mL == 0) || (mL >= cL);
        boolean derOk = (mR == 0) || (mR >= cR);
        return mL >= 0 && mL <= 3 && cL >= 0 && cL <= 3 && (lanchaLado == 0 || lanchaLado == 1) && izqOk && derOk;
    }

    public boolean esObjetivo() {
        return misionerosIzquierda == 0 && canibalesIzquierda == 0 && lanchaLado == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estado estado = (Estado) o;
        return misionerosIzquierda == estado.misionerosIzquierda && canibalesIzquierda == estado.canibalesIzquierda && lanchaLado == estado.lanchaLado;
    }

    @Override
    public int hashCode() {
        return Objects.hash(misionerosIzquierda, canibalesIzquierda, lanchaLado);
    }

    @Override
    public String toString() {
        return "(" + misionerosIzquierda + "," + canibalesIzquierda + "," + lanchaLado + ")";
    }
}



