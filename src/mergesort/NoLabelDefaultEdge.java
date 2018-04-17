package mergesort;

import org.jgrapht.graph.DefaultEdge;

/**
 * Questa classe è un {@link DefaultEdge} a tutti gli effetti,
 * escluso che restituisce una stringa vuota dal metodo toString() invece dei due nodi collegati,
 * questo per rendere il processo di visualizzazione del grafo migliore e più leggero per questo programma.
 */
@SuppressWarnings("serial")
public class NoLabelDefaultEdge extends DefaultEdge
{
    @Override
    public String toString() { return ""; }
}
