package edu.ai.misioneros;

import java.util.*;

/**
 * Árbol BFS (Misioneros-Caníbales) - Versión ASCII (compatible con Git Bash / VS Code)
 * - Nodos etiquetados A,B,C,... (luego AA, AB, ... si hace falta)
 * - Árbol ASCII por niveles (BFS), con pausa entre niveles.
 * - Aristas muestran el movimiento del bote (-> IZQ / -> DER) y la carga.
 * - Impresión de subárbol por id o por etiqueta.
 */
public class MainBFS {

    // ---------- ESTADO / NODO ----------
    static class State {
        final int ci, mi, cd, md;      // caníbales/misioneros izquierda & derecha
        final boolean bote;            // true: bote en derecha, false: izquierda
        State padre;
        final List<State> hijos = new ArrayList<>();
        String edgeLabelFromPadre = ""; // texto en arista padre->hijo
        int depth = 0;
        int id = -1;

        State(int ci, int mi, int cd, int md, boolean bote) {
            this.ci = ci; this.mi = mi; this.cd = cd; this.md = md; this.bote = bote;
        }

        boolean esFinal() { return cd == 0 && md == 0; }

        String clave() { return ci + "," + mi + "," + cd + "," + md + "," + (bote ? 1 : 0); }

        boolean esValido() {
            if (ci < 0 || mi < 0 || cd < 0 || md < 0) return false;
            if (ci > 3 || mi > 3 || cd > 3 || md > 3) return false;
            // si hay misioneros en una orilla, no pueden ser menos que caníbales
            if (mi > 0 && ci > mi) return false;  // izquierda
            if (md > 0 && cd > md) return false;  // derecha
            return true;
        }

        List<State> generarHijos() {
            // movimientos posibles del bote (C, M)
            int[][] moves = { {0,2}, {2,0}, {1,1}, {0,1}, {1,0} };
            List<State> out = new ArrayList<>();
            for (int[] mv : moves) {
                int c = mv[0], m = mv[1];
                State s;
                if (bote) { // bote en derecha -> va a izquierda
                    s = new State(ci + c, mi + m, cd - c, md - m, false);
                } else {    // bote en izquierda -> va a derecha
                    s = new State(ci - c, mi - m, cd + c, md + m, true);
                }
                if (s.esValido()) {
                    s.padre = this;
                    String dir = s.bote ? "-> DER" : "-> IZQ";
                    s.edgeLabelFromPadre = String.format("%s (%dC,%dM)",
                            dir, Math.abs(s.ci - this.ci), Math.abs(s.mi - this.mi));
                    out.add(s);
                }
            }
            return out;
        }
    }

    // ---------- BFS + IMPRESORES ----------
    static class BFSPrinter {
        private int nextId = 0;
        private final Map<Integer, State> byId = new HashMap<>();
        private final Map<String, State> byLabel = new HashMap<>();

        private int asignarId(State s) {
            if (s.id == -1) s.id = nextId++;
            byId.put(s.id, s);
            byLabel.put(labelOf(s.id), s);
            return s.id;
        }

        // 0->A, 25->Z, 26->AA, ...
        private String labelOf(int id) {
            StringBuilder sb = new StringBuilder();
            int n = id;
            do {
                int r = n % 26;
                sb.append((char)('A' + r));
                n = n / 26 - 1;
            } while (n >= 0);
            return sb.reverse().toString();
        }

        private String resumen(State s) {
            String lado = s.bote ? "DER" : "IZQ";
            return String.format("%s [d=%d] CI=%d,MI=%d | CD=%d,MD=%d | B:%s",
                    labelOf(s.id), s.depth, s.ci, s.mi, s.cd, s.md, lado);
        }

        /** Ejecuta BFS, imprime árbol por niveles (con pausa). Devuelve meta si la encuentra. */
        State run(State raiz, Scanner sc) {
            Queue<State> q = new ArrayDeque<>();
            Set<String> visitados = new HashSet<>();

            raiz.depth = 0; asignarId(raiz);
            q.add(raiz);
            visitados.add(raiz.clave());

            int nivel = 0;
            System.out.println("\n==============================");
            System.out.println("  BUSQUEDA EN AMPLITUD (BFS)");
            System.out.println("==============================");
            imprimirArbolHastaNivel(raiz, nivel);

            while (!q.isEmpty()) {
                int size = q.size();
                List<State> actual = new ArrayList<>(size);
                for (int i = 0; i < size; i++) actual.add(q.poll());

                // ¿meta en el nivel?
                for (State n : actual) if (n.esFinal()) return n;

                // generar siguiente nivel
                boolean genero = false;
                for (State n : actual) {
                    for (State h : n.generarHijos()) {
                        if (visitados.add(h.clave())) {
                            asignarId(h);
                            h.depth = n.depth + 1;
                            n.hijos.add(h);
                            q.add(h);
                            genero = true;
                        }
                    }
                }
                if (!genero) break;

                nivel++;
                imprimirArbolHastaNivel(raiz, nivel);

                // pausa por nivel
                System.out.print("\nContinuar con el siguiente nivel? (Enter/\"si\" para seguir, otra tecla para salir): ");
                String line = sc.nextLine().trim().toLowerCase(Locale.ROOT);
                if (!(line.isEmpty() || line.equals("si") || line.equals("sí"))) return null;

                for (State n : actual) for (State h : n.hijos) if (h.esFinal()) return h;
            }
            return null;
        }

        // ---------- Impresión del árbol ASCII (sin Unicode) ----------
        private void imprimirArbolHastaNivel(State raiz, int maxDepth) {
            System.out.println("\n--- Arbol hasta nivel " + maxDepth + " ---");
            System.out.println(resumen(raiz));
            for (int i = 0; i < raiz.hijos.size(); i++) {
                boolean last = (i == raiz.hijos.size() - 1);
                imprimirRec(raiz.hijos.get(i), "", last, maxDepth);
            }
        }

        private void imprimirRec(State n, String pref, boolean last, int maxDepth) {
            // ramas ASCII: "+--" y "\--" (sin caracteres de caja)
            String branch = last ? "\\--" : "+--";
            String line = pref + branch + " " + n.padre.edgeLabelFromPadre + "  " + resumen(n);
            System.out.println(line);

            if (n.depth >= maxDepth) return;

            String nextPref = pref + (last ? "   " : "|  ");
            for (int i = 0; i < n.hijos.size(); i++) {
                boolean childLast = (i == n.hijos.size() - 1);
                imprimirRec(n.hijos.get(i), nextPref, childLast, maxDepth);
            }
        }

        // ---------- SUBARBOL: por id o por etiqueta ----------
        void imprimirSubarbolPorId(int id, int profundidadMax) {
            State root = byId.get(id);
            if (root == null) { System.out.println("No existe nodo con id=" + id); return; }
            imprimirSubarbol(root, profundidadMax);
        }

        void imprimirSubarbolPorEtiqueta(String label, int profundidadMax) {
            State root = byLabel.get(label.toUpperCase(Locale.ROOT));
            if (root == null) { System.out.println("No existe nodo con etiqueta " + label); return; }
            imprimirSubarbol(root, profundidadMax);
        }

        private void imprimirSubarbol(State root, int profundidadMax) {
            System.out.println("\n=== SUBARBOL desde " + labelOf(root.id) + " (prof. max " + profundidadMax + ") ===");
            System.out.println(resumen(root));
            for (int i = 0; i < root.hijos.size(); i++) {
                boolean last = (i == root.hijos.size() - 1);
                imprimirRecSub(root.hijos.get(i), "", last, profundidadMax, root.depth);
            }
        }

        private void imprimirRecSub(State n, String pref, boolean last, int maxDepth, int baseDepth) {
            String branch = last ? "\\--" : "+--";
            System.out.println(pref + branch + " " + n.padre.edgeLabelFromPadre + "  " + resumen(n));
            if ((n.depth - baseDepth) >= maxDepth) return;
            String nextPref = pref + (last ? "   " : "|  ");
            for (int i = 0; i < n.hijos.size(); i++) {
                boolean childLast = (i == n.hijos.size() - 1);
                imprimirRecSub(n.hijos.get(i), nextPref, childLast, maxDepth, baseDepth);
            }
        }
    }

    // ---------- MAIN ----------
    public static void main(String[] args) {
        // Estado inicial clásico: 3C y 3M en la derecha, bote en derecha
        State inicial = new State(0, 0, 3, 3, true);

        Scanner sc = new Scanner(System.in);
        BFSPrinter bfs = new BFSPrinter();
        State meta = bfs.run(inicial, sc);

        if (meta == null) {
            System.out.println("\nNo se encontro solucion (o se detuvo la exploracion).");
            return;
        }

        // Camino solucion
        List<State> camino = new ArrayList<>();
        for (State s = meta; s != null; s = s.padre) camino.add(s);
        Collections.reverse(camino);

        System.out.println("\n=== CAMINO SOLUCION (BFS) ===");
        for (int i = 0; i < camino.size(); i++) {
            State s = camino.get(i);
            System.out.printf("Paso %d: %s%n", i, bfs.resumen(s));
            if (i > 0) System.out.println("   Movimiento: " + s.edgeLabelFromPadre);
        }
        System.out.println("\nCosto (nro de movimientos): " + (camino.size() - 1));

        // --- DEMO SUBARBOL (opcional): descomenta una de estas lineas ---
         bfs.imprimirSubarbolPorEtiqueta("C", 2);
        bfs.imprimirSubarbolPorId(5, 3);
    }
}
