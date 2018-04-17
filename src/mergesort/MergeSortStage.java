package mergesort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * 
 * Classe costruita per restituire un risultato di un MergeSort,
 * inserendo durante l'ordinamento i dati di esecuzione dell'algoritmo e la sua evoluzione in modo dettagliato.
 * 
 */
public class MergeSortStage 
{	
    //DateTimeFormatter per la generazione del risultato nel toString()
    final static DateTimeFormatter time_formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
   
    /**
     * Identifica il tipo di stage della computazione:
     * 
     * Start, appena iniziato
     * Divide, in fase di divisione dell'array e di ordinamento paziale
     * Merge, unendo due array precedentemente ordinati
     * End, l'array è completamente ordinato ed è stata effettuata l'ultima unione
     */
    public enum StageType
    {
        Start,
        Divide,
        Merge,
        End
    }
    
    // Attributi distintivi della classe
    //------
    
	private int[] array;
	private LocalDateTime time;
	private StageType stagetype;
	private int task_index;
	private int forks;
	
	//------
    
    // Componenti statiche della classe
    //------
    
    /**
     * Oggetto del DAG
     */
	private static DefaultDirectedGraph<MergeSortStage, NoLabelDefaultEdge> dag;
	
	/**
	 * Contatore taskID
	 */
    private static final AtomicInteger id_counter = new AtomicInteger(1);
    
    /**
     * Ritorna il DAG della computazione allo stato corrente
     * 
     * @return {@link DefaultDirectedGraph<V, E>} 
     */
    public static DefaultDirectedGraph<MergeSortStage, NoLabelDefaultEdge> getDAG()
    {    
        synchronized(dag)
        {
            return dag;
        }
    }
    
    /**
     * Calcola il numero totale di fork effettuate nel DAG.
     * 
     * @return il numero totale di fork effettuate nel DAG
     */
    public static int getTotalForks()
    {
    	if(dag==null || dag.vertexSet().size()<=1)
    		return 0;
    	
    	return computeTotalForks(dag.vertexSet().stream().filter(p -> p.getStageType().equals(StageType.Start)).findFirst().orElse(null));
    }
    
    /**
     * Ritorna il nemro di fork nel sotto-DAG radicato nel nodo dato.
     * 
     * @param v il nodo radice del sotto-DAG
     * @return il numero di fork nel sotto-DAG
     */
    private static int computeTotalForks(MergeSortStage v)
    {
    	if(v==null)
    		return 0;
    	
    	// Se il numero di forks è negativo, fallo diventare positivo
    	int local_forks = v.forks>=0 ? v.forks : 0;
    	
    	// Termina se arrivato al nodo finale
    	if(v.getStageType().equals(StageType.End))
    		return local_forks;
    	
    	// Se c'è un solo arco uscente somma le local fork a quelle del figlio (senza aggiungerne un'altra)
    	Set<NoLabelDefaultEdge> edges = dag.outgoingEdgesOf(v);
    	if(edges.size()==1)
    		return local_forks + computeTotalForks(dag.getEdgeTarget(edges.iterator().next()));
    	
    	// Scorre ricorsivamente il DAG (si assume che il nodo attuale faccia una sola fork oltre alle subfork)
    	int fork_sum = local_forks + 1;
    	for(NoLabelDefaultEdge e : dag.outgoingEdgesOf(v))
    		fork_sum += computeTotalForks(dag.getEdgeTarget(e));
    	
    	return fork_sum;
    }
    
    /**
     * Resetta il contatore dei taskID al valore iniziale
     */
    private static void counterReset()
    {
    	synchronized(dag)
    	{
    		id_counter.set(1);
    	}
    }
    
    /**
     * Restituisce il valore attuale del contatore e poi lo incrementa.
     * 
     * @return il valore del contatore prima dell'incremento
     */
    private static int counterGetAndIncrement()
    {
    	synchronized(dag)
    	{
    		return id_counter.getAndAdd(1);
    	}
    }
        
    //------
    
	/**
	 * Costruisce l'oggetto {@link MergeSortStage}, generando autonomamente il time stamp.
	 * Questo metodo deve essere utilizzato per creare il primo {@link MergeSortStage} dell'esecuzione dell'algoritmo.
	 * 
	 * @param array iniziale
	 */
    public MergeSortStage(int[] array)
    {        
    	// Essendo la prima computazione dell'algoritmo resetto il DAG ed il contatore
    	dag = new DefaultDirectedGraph<MergeSortStage, NoLabelDefaultEdge>(NoLabelDefaultEdge.class);
        counterReset();
        
        // Creo il primo nodo
    	this.array = array;
        this.time = LocalDateTime.now();
        this.stagetype = StageType.Start;
        
        // Genero il taskID tramite il contatore
        this.task_index = counterGetAndIncrement();
        
        // Aggiungo il nodo al grafo
        dag.addVertex(this);
    }
    
    /**
     * Costruisce l'oggetto {@link MergeSortStage}, collegandolo ai precedenti stage dell'algoritmo.
     * Assume che non si vogliano memorizzare il numero di fork effettuate.
     * Genera il timestamp autonomamente.
     * 
     * @param array, array allo stato attuale
     * @param stagetype, tipo dello stage corrente 
     * @param task_id, id della task che ha elaborato questo elemento (se negativo, lo prende dal contatore interno)
     * @param previous_stages, gli stage precedenti a quello corrente, richiesti per essere collegati a questo
     */
    public MergeSortStage(int[] array, StageType stagetype, int task_id, MergeSortStage... previous_stages)
    {
        this(array, LocalDateTime.now(), stagetype, task_id, -1, previous_stages);
    }
    
    /**
     * Costruisce l'oggetto {@link MergeSortStage}, collegandolo ai precedenti stage dell'algoritmo.
     * Assume che non si vogliano memorizzare il numero di fork effettuate.
     * 
     * @param array, array allo stato attuale
     * @param time, tempo in cui è stato effettuato il calcolo corrente
     * @param stagetype, tipo dello stage corrente 
     * @param task_id, id della task che ha elaborato questo elemento (se negativo, lo prende dal contatore interno)
     * @param previous_stages, gli stage precedenti a quello corrente, richiesti per essere collegati a questo
     */
    public MergeSortStage(int[] array, LocalDateTime time, StageType stagetype, int task_id, MergeSortStage... previous_stages)
    {
        this(array, time, stagetype, task_id, -1, previous_stages);
    }
    
    /**
     * Costruisce l'oggetto {@link MergeSortStage}, collegandolo ai precedenti stage dell'algoritmo.
     * Genera il timestamp autonomamente.
     * 
     * @param array, array allo stato attuale
     * @param stagetype, tipo dello stage corrente 
     * @param task_id, id della task che ha elaborato questo elemento (se negativo, lo prende dal contatore interno)
     * @param forks, numero di fork effettuate nella computazione del nodo
     * @param previous_stages, gli stage precedenti a quello corrente, richiesti per essere collegati a questo
     */
    public MergeSortStage(int[] array, StageType stagetype, int task_id, int forks, MergeSortStage... previous_stages)
    {
        this(array, LocalDateTime.now(), stagetype, task_id, forks, previous_stages);
    }
    
    /**
     * Costruisce l'oggetto {@link MergeSortStage}, collegandolo ai precedenti stage dell'algoritmo.
     * Genera il taskID autonomamente
     * 
     * @param array, array allo stato attuale
     * @param time, tempo in cui è stato effettuato il calcolo corrente
     * @param stagetype, tipo dello stage corrente 
     * @param task_id, id della task che ha elaborato questo elemento (se negativo, lo prende dal contatore interno)
     * @param forks, numero di fork effettuate nella computazione del nodo
     * @param previous_stages, gli stage precedenti a quello corrente, richiesti per essere collegati a questo
     */
    public MergeSortStage(int[] array, LocalDateTime time, StageType stagetype, int task_id, int forks, MergeSortStage... previous_stages)
    {
        assert(previous_stages != null);
        
        synchronized(dag)
        {
	        this.array = array;
	        this.time = time;
	        this.stagetype = stagetype;
	        
	        // Genero il taskID tramite il contatore
	        this.task_index = (task_id<0) ? counterGetAndIncrement() : task_id;
	        
	        // Setta il numero di froks solo se è un numero positivo e il nodo è di tipo 'Merge' o 'End'
	        if((stagetype == StageType.Merge || stagetype == StageType.End) && forks>=0)
	    		this.forks = forks;
	        else
	        	this.forks = -1;
	        
        	// Creo il nodo del dag corrispondete alla computazione corrente e lo aggiungo al grafo
            dag.addVertex(this);
            
            // Aggiungo tuti gli archi
            for(MergeSortStage stages : previous_stages)
                dag.addEdge(stages, this);
        }
    }
    
    /**
     * Costruisce l'oggetto {@link MergeSortStage} con i parametri passati in ingresso,
     * Questo costruttore deve essere utilizzato quando si vuole creare un oggetto {@link MergeSortStage} senza toccare il dag di esecuzione, 
     * quindi gli oggetti {@link MergeSortStage} creati con questo costruttore non saranno inclusi nel dag autonomamente.
     * 
     * @param task_id id della task che ha elaborato questo elemento
     * @param stagetype, deve essere passato in formato stringa, la conversione verrà effettuata dal costruttore
     * @param array array allo stato attuale
     * @param time tempo in cui è stato effettuato il calcolo corrente
     * @param forks numero di forks fatte nella computazione del nodo
     */
    public MergeSortStage(int task_id, String stagetype, int[] array, LocalDateTime time, int forks)
    {
        this.task_index = task_id;
        this.stagetype = StageType.valueOf(stagetype);
        this.array = array;
        this.time  = time;
        this.forks = forks;
    }
    
    // METODI DI MODIFICA DEL NODO
    
    /**
     * Ordina direttamente l'array del nodo con insertion sort, e aggiorna il timestamp.
     */
    public void directSort()
    {
        synchronized(dag)
    	{
    		// Salvo tutti i nodi padri del nodo attuale ed i relativi archi entranti
    		Set<NoLabelDefaultEdge> inEdges = dag.incomingEdgesOf(this);
    		Set<MergeSortStage> parents = inEdges.stream()
    				.map(e -> dag.getEdgeSource(e))
    				.collect(Collectors.toSet());
    		
    		// Salvo tutti i nodi figli del nodo attuale ed i relativi archi uscenti
    		Set<NoLabelDefaultEdge> outEdges = dag.outgoingEdgesOf(this);
    		Set<MergeSortStage> children = outEdges.stream()
    				.map(e -> dag.getEdgeTarget(e))
    				.collect(Collectors.toSet());
    		
    		// Rimuovo il nodo finale e tutti gli archi
    		dag.removeVertex(this);
    		dag.removeAllEdges(inEdges);
    		dag.removeAllEdges(outEdges);
    		
    		// Ordino l'array direttamente
    		MergeSortUtils.insertionSort(this.array);
    		
    		// Aggiorno il timestamp
    		this.time = LocalDateTime.now();
    		
        	// Riaggiungo il nodo, con i relativi archi entranti e uscenti
        	dag.addVertex(this);
        	for(MergeSortStage v : parents)
        		dag.addEdge(v, this);
        	for(MergeSortStage v : children)
        		dag.addEdge(this, v);
    	}
    }
    
    /**
     * Setta questo nodo come finale.
     * Il metodo è progettato per essere chiamato sull'ultimo nodo del grafo,
     * quindi usarlo su altri nodi potrebbe creare errori di connessione nel grafo.
     */
    public void setEndStage()
    {
    	synchronized(dag)
    	{
    		// Salvo tutti i nodi padri del nodo attuale ed i relativi archi entranti
    		// Essendo il nodo finale, non possono esserci archi uscenti
    		Set<NoLabelDefaultEdge> inEdges = dag.incomingEdgesOf(this);
    		Set<MergeSortStage> parents = inEdges.stream()
    				.map(e -> dag.getEdgeSource(e))
    				.collect(Collectors.toSet());
    		
    		// Rimuovo il nodo finale e tutti gli archi
    		dag.removeVertex(this);
    		dag.removeAllEdges(inEdges);
    		
    		// Setto il nodo attuale come finale
        	this.stagetype = StageType.End;
        	
        	// Riaggiungo il nodo, con i relativi archi entranti
        	dag.addVertex(this);
        	for(MergeSortStage v : parents)
        		dag.addEdge(v, this);
    	}
    }
    
    // GETTER
    
    /**
     * Ritorna il numero identificativo del thread che si è occupato di questa computazione
     * 
     * @return int
     */
    public int getTaskID()
    {
        return task_index;
    }
    
    /**
     * Ritorna l'array allo stato corrente dell'ordinamento
     * 
     * @return int[]
     */
    public int[] getResult()
    {
        return array;
    }
    
    /**
     * Ritorna l'array allo stato corrente dell'ordinamento in forma di stringa
     * 
     * @return String
     */
    public String getResultString()
    {
        return Arrays.toString(this.array);
    }
    
    /**
     * Ritorna il time stamp di quando è stato raggiunto questo risultato
     * 
     * @return {@link LocalDateTime}
     */
    public LocalDateTime getTime()
    {
        return time;
    }
    
    /**
     * Ritorna il tipo dello stage corrente 
     * 
     * @return {@link StageType}
     */
    public StageType getStageType()
    {
        return stagetype;
    }
    
    /**
     * Ritorna il numero di fork effettuate in questo nodo durante la sua computazione.
     * 
     * @return int
     */
    public int getSubForks()
    {
        return this.forks;
    }
    
    /**
     * Se il numero di fork è >=0 e il nodo attuale è di tipo 'Merge' o 'End', ritorna la seguente stringa:
     * SubFork = [getSubForks()]
     * 
     * Altrimenti ritorna la stringa vuota.
     * 
     * @return String stringa che caratterizza il numero di fork aggiuntive eseguite nel nodo attuale.
     */
    public String getSubForksString()
    {
        if((this.stagetype == StageType.Merge || this.stagetype == StageType.End) && this.forks>=0)
        	return "SubFork = " + this.forks;
        else
        	return "";
    }
    
    //override
    
    @Override
    public boolean equals(Object o) 
    {        
        if(!(o instanceof MergeSortStage)) 
        {
            return false;
        }        
        if(!(Arrays.equals(this.array, ((MergeSortStage)(o)).array)))
            return false;
        if(!(this.time.equals(((MergeSortStage)(o)).time)))
            return false;
        if(!(this.stagetype.equals(((MergeSortStage)(o)).stagetype)))
            return false;
        
        return true;
    }
    
    /**
     * Restituisce una descrizione del nodo in formato HTML
     */
    @Override
    public String toString() 
    {
    	String subforks = this.getSubForksString();
    	if(!subforks.isEmpty())
    		subforks += "<br>";
    	
    	return "<html>"
    			+ "<center>"
	    			+ this.task_index
	    			+ "<br>" 
	    			+ subforks
	    			+ Arrays.toString(this.array)
    			+ "</center>" 
    		+ "</html>";
    }
    
    @Override
    public int hashCode() 
    {
        return Objects.hash(this.task_index, this.stagetype, Arrays.hashCode(this.array), this.forks);
    }
}
