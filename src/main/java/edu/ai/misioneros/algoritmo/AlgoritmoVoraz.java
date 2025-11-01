package edu.ai.misioneros.algoritmo;

import java.util.*;

public class AlgoritmoVoraz {

    // ================== Modelo ==================
    static class Nodo {
        int misionerosIzq, canibalesIzq;
        int misionerosDer, canibalesDer;
        boolean boteIzquierda;
        Nodo padre;
        int nivel;
        String accion;

        Nodo(int mi, int ci, int md, int cd, boolean boteIzq, Nodo padre, String accion) {
            this.misionerosIzq = mi;
            this.canibalesIzq = ci;
            this.misionerosDer = md;
            this.canibalesDer = cd;
            this.boteIzquierda = boteIzq;
            this.padre = padre;
            this.nivel = (padre == null) ? 0 : padre.nivel + 1;
            this.accion = accion;
        }

        int heuristica() { return misionerosIzq + canibalesIzq; } // Greedy: minimizar personas en la izquierda

        boolean esValido() {
            if (misionerosIzq < 0 || canibalesIzq < 0 || misionerosDer < 0 || canibalesDer < 0) return false;
            if (misionerosIzq > 3 || canibalesIzq > 3 || misionerosDer > 3 || canibalesDer > 3) return false;
            if (misionerosIzq > 0 && canibalesIzq > misionerosIzq) return false; // C > M en izquierda
            if (misionerosDer > 0 && canibalesDer > misionerosDer) return false; // C > M en derecha
            return true;
        }

        boolean esObjetivo() { return misionerosIzq == 0 && canibalesIzq == 0; }

        String key() {
            return misionerosIzq + "," + canibalesIzq + "," + misionerosDer + "," + canibalesDer + "," + (boteIzquierda ? "L":"R");
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Nodo)) return false;
            Nodo other = (Nodo) o;
            return misionerosIzq == other.misionerosIzq &&
                   canibalesIzq == other.canibalesIzq &&
                   misionerosDer == other.misionerosDer &&
                   canibalesDer == other.canibalesDer &&
                   boteIzquierda == other.boteIzquierda;
        }
        @Override public int hashCode() {
            return Objects.hash(misionerosIzq, canibalesIzq, misionerosDer, canibalesDer, boteIzquierda);
        }

        void dibujarNodo() {
            System.out.println("+-------------------------------------------+");
            System.out.printf("| Nivel: %-2d  Accion: %-18s |\n", nivel, (accion != null ? accion : "Inicio"));
            System.out.println("+-------------------------------------------+");
            System.out.printf("| Izquierda:  %dM %dC %s\n", misionerosIzq, canibalesIzq, (boteIzquierda ? "[B]" : "   "));
            System.out.println("| ~~~~~~~~~~~~~  R I O  ~~~~~~~~~~~~~");
            System.out.printf("| Derecha:    %dM %dC %s\n", misionerosDer, canibalesDer, (!boteIzquierda ? "[B]" : "   "));
            System.out.println("+-------------------------------------------+");
        }

        void dibujarSimple() {
            String bote = boteIzquierda ? "I" : "D";
            System.out.printf("[(%dM,%dC)%s(%dM,%dC)]", misionerosIzq, canibalesIzq, bote, misionerosDer, canibalesDer);
            if (accion != null) System.out.printf(" <- %s", accion);
        }
    }

    // ================== Constantes de movimiento (A NIVEL DE CLASE) ==================
    private static final int[][] MOVIMIENTOS = {
        {1,0},{2,0},{0,1},{0,2},{1,1}
    };
    private static final String[] DESCRIPCION_MOVIMIENTOS = {
        "1M ->","2M ->","1C ->","2C ->","1M1C ->"
    };

    // ================== Búsqueda Greedy ==================
    public static List<Nodo> busquedaVoraz() {
        Nodo inicial = new Nodo(3,3,0,0,true,null,"Inicio");

        PriorityQueue<Nodo> cola = new PriorityQueue<>(Comparator.comparingInt(Nodo::heuristica));
        Set<String> visitados = new HashSet<>();

        cola.add(inicial);
        visitados.add(inicial.key());

        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();

            if (actual.esObjetivo()) return reconstruirCamino(actual);

            for (int i=0; i<MOVIMIENTOS.length; i++) {
                int m = MOVIMIENTOS[i][0];
                int c = MOVIMIENTOS[i][1];
                int aboard = m + c;
                if (aboard < 1 || aboard > 2) continue;

                Nodo hijo;
                String accion = DESCRIPCION_MOVIMIENTOS[i];

                if (actual.boteIzquierda) {
                    hijo = new Nodo(
                        actual.misionerosIzq - m, actual.canibalesIzq - c,
                        actual.misionerosDer + m, actual.canibalesDer + c,
                        false, actual, accion
                    );
                } else {
                    hijo = new Nodo(
                        actual.misionerosIzq + m, actual.canibalesIzq + c,
                        actual.misionerosDer - m, actual.canibalesDer - c,
                        true, actual, accion.replace("->","<-")
                    );
                }

                if (!hijo.esValido()) continue;
                if (visitados.contains(hijo.key())) continue;

                visitados.add(hijo.key());
                cola.add(hijo);
            }
        }
        return null;
    }

    private static List<Nodo> reconstruirCamino(Nodo objetivo) {
        List<Nodo> camino = new ArrayList<>();
        for (Nodo p = objetivo; p != null; p = p.padre) camino.add(0, p);
        return camino;
    }

    public static void imprimirArbolCompleto(List<Nodo> camino) {
        System.out.println("ARBOL DE BUSQUEDA VORAZ - MISIONEROS Y CANIBALES");
        System.out.println("================================================");
        if (camino == null) { System.out.println("No se encontro solucion"); return; }

        System.out.println("\nCAMINO SOLUCION (" + (camino.size()-1) + " pasos)");
        System.out.println("------------------------------------------------");
        for (int i=0;i<camino.size();i++) {
            System.out.printf("\nPASO %d:\n", i);
            camino.get(i).dibujarNodo();
        }

        System.out.println("\nSECUENCIA LINEAL:");
        System.out.println("-----------------");
        for (int i=0;i<camino.size();i++) {
            System.out.printf("Paso %d: ", i);
            camino.get(i).dibujarSimple();
            System.out.println();
        }
    }

    public static void mostrarEstadisticas(List<Nodo> camino) {
        System.out.println("\nESTADISTICAS:");
        System.out.println("-------------");
        System.out.println("Total de pasos: " + (camino.size()-1));
        System.out.println("Profundidad: " + (camino.size()-1));
        System.out.println("Heuristica: personas en orilla izquierda");
        System.out.println("Estrategia: Greedy (Voraz)");
    }

    // ================== main (UNICO) ==================
    public static void main(String[] args) {
        long t0 = System.nanoTime();
        List<Nodo> solucion = busquedaVoraz();
        long t1 = System.nanoTime();

        System.out.println("PROBLEMA: MISIONEROS Y CANIBALES");
        System.out.println("Algoritmo: Busqueda Voraz (Greedy)\n");

        imprimirArbolCompleto(solucion);
        if (solucion != null) mostrarEstadisticas(solucion);

        double ms = (t1 - t0)/1_000_000.0;
        System.out.printf("\nGreedy termino en %.6f ms (%d ns)\n", ms, (t1 - t0));
    }
}
