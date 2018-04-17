package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultListenableGraph; 

import mergesort.MergeSortStage;
import mergesort.MergeSortStage.StageType;
import mergesort.NoLabelDefaultEdge;

/**
 * Classe che permette la visualizzazione del grafo.
 * La libreria JGraphT non supporta FX, e visto che non si necessitava un uso complesso dell'interfaccia,
 * non è stato utilizzato uno Swing su FX ma è stata utilizzata direttamente la Swing.
 */
@SuppressWarnings("serial")
public class GraphVisualizer extends JApplet
{
    // Parametri GUI
	private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
    private static final FontRenderContext FRC 	= new FontRenderContext(new AffineTransform(), true, true);
    private Dimension WINDOW_SIZE;
    
    // Altezza dei livelli dell'albero
    private static final double LEVEL_HEIGHT  = 100;
    
    // Oggetti DAG
    private JGraphModelAdapter<MergeSortStage, NoLabelDefaultEdge> jgAdapter;
    private JGraph jgraph;
    
    public GraphVisualizer(DefaultDirectedGraph<MergeSortStage, NoLabelDefaultEdge> dag)
    {
    	// Trovo i nodi iniziali e finali
        MergeSortStage startVertex = findVertexByType(dag, StageType.Start).iterator().next();
        MergeSortStage endVertex = findVertexByType(dag, StageType.End).iterator().next();
        
        // Trovo l'altezza dell'albero
        int treeHeight = dagHeight(dag, startVertex);
    	
    	// Sceglie il WINDOW_BORDER in base al numero dei nodi
    	int n_vertex = dag.vertexSet().size();
    	double WINDOW_BORDER; 
    	if(n_vertex <= 10)
    	{
    		// Max length array input = 4
    		WINDOW_BORDER = 40;
    	}
    	else if(n_vertex <= 22)
    	{
    		// Max length array input = 8
    		WINDOW_BORDER = 10;
    	}
    	else
    	{
    		// Max length array input >= 16
    		// n_vertex >= 46
    		WINDOW_BORDER = 5;
    	}
    	
        // Calcolo la dimensione della finestra
        double w = WINDOW_BORDER * Math.pow(2, treeHeight);
        double h = LEVEL_HEIGHT * treeHeight;
        WINDOW_SIZE = new Dimension((int)w, (int)h);
        
        // Inizializza l'oggetto grafico del DAG
        jgAdapter = new JGraphModelAdapter<>(new DefaultListenableGraph<>(dag));
        jgraph = new JGraph(jgAdapter);
        jgAdapter.setDefaultEdgeAttributes(null);
        
        // Disegno la parte superiore del DAG
        paintDagTop(dag, startVertex, w/2, 10, w/4);
        
        // Disegno la parte inferiore del DAG
        paintDagBottom(dag, endVertex, w/2, h - LEVEL_HEIGHT, w/4);
        
        // Inizializza e mostra la GUI
        this.init();
    }
    
    @Override
    public void init()
    {
    	// Aggiusta le dimensioni del visualizzatore e della finestra
    	jgraph.setPreferredSize(WINDOW_SIZE);
        resize(WINDOW_SIZE);
        
        // Setta il colore di background
        try
        {
        	String colorStr = getParameter("bgcolor");
        	if( colorStr != null )
        		jgraph.setBackground(Color.decode(colorStr));
        	
        }
        catch( Exception e )
        {
        	jgraph.setBackground(DEFAULT_BG_COLOR);
        }
        
        // Crea la finestra, inserisce il visualizzatore con srollbar, e applica le impostazioni
        JFrame frame = new JFrame();
        JScrollPane dagScrollPane = new JScrollPane(jgraph, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(dagScrollPane);
        frame.setTitle("DAG");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        
        // Rende la finestra visibile
        frame.setVisible(true);
    }

    /**
     * Posiziona il nodo dato alle cooordinate indicate.
     * 
     * @param vertex vertice da posizionare
     * @param x ascissa della posizione
     * @param y ordinata della posizione
     */
    @SuppressWarnings("unchecked")
	private void positionVertexAt(MergeSortStage vertex, double x, double y)
    {
        // Prendo gli attuali dati relativi alla cella
    	DefaultGraphCell cell = jgAdapter.getVertexCell(vertex);
        AttributeMap attributes = cell.getAttributes();
        
        // Cerco la linea più lunga della label
    	String lineForks = vertex.getSubForksString();
        String lineArray = vertex.getResultString();
        String longestLine = lineForks.length()>lineArray.length() ? lineForks : lineArray;
        
        // Calcolo l'altezza e la larghezza della cella (aggiungo "XXXX" alla fine per assicurarmi che tutto il testo stia nella cella)
        Rectangle2D txtDim = GraphConstants.getFont(attributes).getStringBounds("0", FRC);
        double w = txtDim.getWidth() * (longestLine.length() + 1);
        double h = txtDim.getHeight() * (vertex.toString().split("<br>").length + 1);
        
        // Modifico le misure della cella (tolgo alla 'x' metà di 'w', per compensare l'aumento di larghezza della calla)
        Rectangle2D bounds = new Rectangle2D.Double(x - w/2, y, w, h);
        GraphConstants.setBounds(attributes, bounds);

        // Applico le modifiche al DAG
        AttributeMap cellAttr = new AttributeMap();
        cellAttr.put(cell, attributes);
        jgAdapter.edit(cellAttr, null, null, null);
    }
    
    /**
     * Decide le posizioni dei vertici per la parte superiore del DAG, 
     * scorrendolo dall'alto verso il basso.
     * 
     * @param dag il dag da disegnare
     * @param v nodo attualmente in esame
     * @param x ascissa nodo attualmente in esame
     * @param y ordinata nodo attualmente in esame
     * @param stepX differenza sull'asse delle ascisse da applicare ai nodi figli
     */
    private void paintDagTop(
    		DefaultDirectedGraph<MergeSortStage, NoLabelDefaultEdge> dag, 
    		MergeSortStage v, 
    		double x, 
    		double y, 
    		double stepX
    		)
    {
    	// Termina se arriva all'altra metà del DAG
    	if(!(v.getStageType().equals(StageType.Start) || v.getStageType().equals(StageType.Divide)))
            return;
        
    	// Posiziona il nodo corrente
    	positionVertexAt(v, x, y);
    	
    	// Prendi la lista (ordinata per taskID) dei nodi figli
    	List<MergeSortStage> children = dag.outgoingEdgesOf(v).stream()
    			.map(e -> dag.getEdgeTarget(e))
    			.sorted((v1, v2) -> Integer.compare(v1.getTaskID(), v2.getTaskID()))
    			.collect(Collectors.toList());
        
    	// Scorre ricorsivamente il DAG, scegliendo le posizioni dei nodi figli
    	if(children.size() >= 1)
    		paintDagTop(dag, children.get(0), x+stepX, y+LEVEL_HEIGHT, stepX/2);
    	if(children.size() >= 2)
    		paintDagTop(dag, children.get(1), x-stepX, y+LEVEL_HEIGHT, stepX/2);
    }
    
    /**
     * Decide le posizioni dei vertici per la parte inferiore del DAG, 
     * scorrendolo dal basso verso l'alto.
     * 
     * @param dag il dag da disegnare
     * @param v nodo attualmente in esame
     * @param x ascissa nodo attualmente in esame
     * @param y ordinata nodo attualmente in esame
     * @param stepX differenza sull'asse delle ascisse da applicare ai nodi padri
     */
    private void paintDagBottom(
    		DefaultDirectedGraph<MergeSortStage, NoLabelDefaultEdge> dag, 
    		MergeSortStage v, 
    		double x, 
    		double y, 
    		double stepX
    		)
    {
    	// Termina se arriva all'altra metà del DAG
    	if(!(v.getStageType().equals(StageType.End) || v.getStageType().equals(StageType.Merge)))
            return;
        
    	// Posiziona il nodo corrente
    	positionVertexAt(v, x, y);
    	
    	// Prendi la lista (ordinata per taskID) dei nodi padri
    	List<MergeSortStage> children = dag.incomingEdgesOf(v).stream()
    			.map(e -> dag.getEdgeSource(e))
    			.sorted((v1, v2) -> Integer.compare(v1.getTaskID(), v2.getTaskID()))
    			.collect(Collectors.toList());
    	
    	// Scorre ricorsivamente il DAG, scegliendo le posizioni dei nodi padri
    	if(children.size() >= 1)
    		paintDagBottom(dag, children.get(0), x+stepX, y-LEVEL_HEIGHT, stepX/2);
    	if(children.size() >= 2)
    		paintDagBottom(dag, children.get(1), x-stepX, y-LEVEL_HEIGHT, stepX/2);
    }
    
    /**
     * Ritorna tutti i nodi del DAG che hano il tipo indicato.
     * 
     * @param dag il dag in cui cercare
     * @param type il tipo da cercare
     * @return tutti i nodi del DAG che hano il tipo indicato
     */
    private Set<MergeSortStage> findVertexByType(DefaultDirectedGraph<MergeSortStage, NoLabelDefaultEdge> dag, StageType type)
    {
    	return dag.vertexSet().stream().filter(p -> p.getStageType().equals(type)).collect(Collectors.toSet());
    }
    
    /**
     * Calcola l'altezza del DAG (calcolata a partire dal nodo fornito).
     * 
     * @param dag il dag di cui calcolare l'altezza
     * @param vertex il nodo da cui partire
     * @return l'altezza del DAG (calcolata a partire dal nodo fornito)
     */
    private int dagHeight(DefaultDirectedGraph<MergeSortStage, NoLabelDefaultEdge> dag, MergeSortStage vertex)
    {
    	// Termina direttamente se arrivato al nodo finale
    	if(vertex.getStageType().equals(StageType.End))
    		return 1;
    	
    	// Scorre ricorsivamente il DAG
    	int t, height = 1;
    	for(NoLabelDefaultEdge e : dag.outgoingEdgesOf(vertex))
        {
    		t = dagHeight(dag, dag.getEdgeTarget(e)) + 1;
    		if( t > height )
    			height = t;
        }
    	
    	return height;
    }
}
