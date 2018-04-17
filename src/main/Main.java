package main;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jgrapht.graph.DefaultDirectedGraph;

import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DefaultAttribute;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.GraphMLExporter;
import org.jgrapht.io.GraphMLExporter.AttributeCategory;
import org.jgrapht.io.GraphMLImporter;
import org.jgrapht.io.IntegerComponentNameProvider;
import org.jgrapht.io.VertexProvider;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.AttributeType;

import gui.GraphVisualizer;
import mergesort.MergeSortParallel;
import mergesort.MergeSortSemiParallel;
import mergesort.MergeSortSequential;
import mergesort.MergeSortStage;
import mergesort.NoLabelDefaultEdge;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main 
{
    public enum SortType 
    {
       S, SP, P
    }
    
    private final static DateTimeFormatter file_time_formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
    private final static DateTimeFormatter export_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    
    private final static String DESCRIPTION_AND_CREDITS = "Analizzatore delle prestazioni del mergesort.\nAutori: Daniele Giudice, Gabriele Cavallaro";
    
    /*
     * Dati da riga di comando:
     * n		-> dimensione array
     * decr  	-> flag per generare array decrescente di n numeri (se non c'è il flag, genera n numeri casuali)
     * mode  	-> tipo di mergesort di sort da usare (seriale, semiparallelo, parallelo), se non settato errore
     * cutoff 	-> costante di cutoff da usare, intero >= 1 (defualt 1)
     * debug 	-> flag che se presente attiva il debug (di default è spento)
     */
    
    /**
     * Restituisce un array generato casualmente della lunghezza indicata, non ordinato
     * Asserisce che n sia maggiore di 0
     * 
     * @param n, numero degli elementi
     * @return array di int non ordinato
     */
	public static int[] genRandomIntArray(int n)
	{
	    assert n > 0;
	    
		return new Random().ints(n, -2147483648, 2147483647).toArray();
	}
	
	/**
     * Restituisce l'array dei numeri da n a 1 ordinati in modo decrescente.
     * Asserisce che n sia maggiore di 0.
     * 
     * @param n, numero degli elementi
     * @return array di int ordinato in modo decrescente
     */
	public static int[] genInverseRangeArray(int n)
	{
	    assert n > 0;
	    
	    int[] rtn_array = new int[n];
	    int i, k = n;
	    for(i = 0 ; i < n ; i++)
	    {
	    	rtn_array[i] = k;
	    	k--;
	    }   
		return rtn_array;
	}
	
	public static void main(String[] args)
	{
	    try
	    {
    		// Definisco argparser
    		ArgumentParser parser = ArgumentParsers.newFor("MergeSort Analyser").build().description(DESCRIPTION_AND_CREDITS);
            
    		// Visualizza credits
    		parser.addArgument("--credits")
        		.dest("credits")
        		.action(Arguments.storeTrue())
        		.setDefault(false)
        		.help("Mostra i credits (ignora ogni altra opzione)");
    		
    		// Argomenti per test e generazione del DAG
    		parser.addArgument("--n")
        		.dest("n")
                .type(Integer.class)
                .setDefault(1)
                .help("Numero di elementi dell'array, tale che n>=1 (Default: 1)");
    		parser.addArgument("--mode")
		        .dest("mode")
		        .type(Arguments.caseInsensitiveEnumType(SortType.class))
		        .setDefault(SortType.S)
		        .help("Tipo di algoritmo, fra 's' (seriale), 'sp' (semiparallelo), e 'p' (parallelo) (Default: sequenziale)");
    		parser.addArgument("--decr")
		        .dest("decr")
		        .action(Arguments.storeTrue())
		        .setDefault(false)
		        .help("Genera gli n elementi in ordine decrescente (Default: casuale)");
    		parser.addArgument("--cutoff")
		        .dest("cutoff")
		        .type(Integer.class)
		        .setDefault(1)
		        .help("Cutoff sequenziale, intero tale che 1<=cutoff<=n (Default: 1)");
    		parser.addArgument("--debug")
		        .dest("debug")
		        .action(Arguments.storeTrue())
		        .setDefault(false)
		        .help("Attiva debug");
    		parser.addArgument("--viewdag")
                .dest("viewdag")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Visualizza il dag in una GUI al termine della computazione. "
                		+ "L'opzione è ignorata se non c'e' l'opzione '--debug' o se ci sono le opzioni '--n 1' o '--mode s'.");
    		parser.addArgument("--savedag")
                .dest("savedag")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Salva su file il dag in formato xml al termine della computazione. "
                		+ "L'opzione è ignorata se non c'e' l'opzione '--debug' o se ci sono le opzioni '--n 1' o '--mode s'.");
    		
    		// Argomenti per la visualizzazione del DAG
    		parser.addArgument("--opendag")
                .dest("opendag")
                .type(String.class)
                .setDefault("")
                .help("Apre un DAG da un file indicato e lo mostra nella GUI (le altre opzioni saranno ignorate)");
            
            try
            {
                // Parsing: Carico parametri
                Namespace args_parsed = parser.parseArgs(args);
                
                // Parsing: Visualizza o no i credits
                boolean credits = args_parsed.getBoolean("credits").booleanValue();
                if(credits)
                {
                	System.out.println(DESCRIPTION_AND_CREDITS);
                	return;
                }
                
                // Parsing: Numero valori array
                int n = args_parsed.getInt("n").intValue();
                if (n <= 0)
                	throw new ArgumentParserException("Deve essere: n>=1", parser) ;
                
                // Parsing: Cutoff sequenziale
                int cutoff = args_parsed.getInt("cutoff").intValue();
                if (cutoff < 1 || cutoff > n)
                	throw new ArgumentParserException("Deve essere: 1<=cutoff<=n", parser) ;
                
                // Parsing: Ordinare o no in modo decrescente
                boolean decr = args_parsed.getBoolean("decr").booleanValue();
                
                // Parsing: Attivare o no il debug
                boolean debug = args_parsed.getBoolean("debug").booleanValue();
                
                // Parsing: Tipo di mergesort
                SortType mode = (SortType) args_parsed.get("mode");
                
                // Parsing: Visualizzare o no il grafo alla fine
                boolean viewdag = args_parsed.getBoolean("viewdag").booleanValue();
                
                // Parsing: Salvare o no il grafo alla fine
                boolean savedag = args_parsed.getBoolean("savedag").booleanValue();
                
                // Parsing: Path file di DAG da aprire
                String opendag = args_parsed.getString("opendag");
                
                /*
                 * Disattiva la creazione del DAG se vi è una di queste condizioni:
                 * - il debug è spento
                 * - la modalità è sequenziale
                 * - l'array è di un solo elemento
                 */
                if(!debug || mode.equals(SortType.S) || n==1)
                {
                	viewdag = false;
                	savedag = false;
                }
                
                // Genero array
                int[] input_array;
                if (decr)
                	input_array = genInverseRangeArray(n);
                else
                	input_array = genRandomIntArray(n);
                
                // Uso parametri
                
                if(!opendag.equals(""))
                {
                    File file_to_read = new File(opendag);
                    DefaultDirectedGraph<MergeSortStage, NoLabelDefaultEdge> dag = 
                    		new DefaultDirectedGraph<MergeSortStage, NoLabelDefaultEdge>(NoLabelDefaultEdge.class);
                    createImporter().importGraph(dag, file_to_read);
                    new GraphVisualizer(dag);
                    return;
                }
                
                // Lancio la computazione (segnando il tempo di inizio e fine)
                System.out.println("Esecuzione in corso...");
                
                MergeSortStage result = null;
                LocalDateTime start_time = LocalDateTime.now();
                switch(mode)
                {
                	case S:
                		result = new MergeSortSequential().sort(input_array, debug);
                		break;
                	case SP:
                		result = new MergeSortSemiParallel(cutoff).sort(input_array, debug);
                		break;
                	case P:
                		result = new MergeSortParallel(cutoff).sort(input_array, debug);
                		break;
                }
                LocalDateTime end_time = LocalDateTime.now();
                
                // Stampo l'eesito del controllo
                if(checkArray(result.getResult()))
                	System.out.println("Operazione completata (array ordinato correttamente)!");
                else
                	System.out.println("Operazione completata (array non ordinato correttamente)!");
                
                // Stampo il numero di fork effettuate (se non sono in sequenziale)
                if(debug && !mode.equals(SortType.S))
                	System.out.println("Numero di fork: " + MergeSortStage.getTotalForks());
                
                // Stampo il tempo impiegato
                System.out.println("Tempo impiegato: " + subtractTime(start_time, end_time));
                
                if(savedag)
                {
                    File file_to_save = 
                    		new File(Paths.get(".").toAbsolutePath().normalize().toString()+"\\Dag_"+file_time_formatter.format(LocalDateTime.now())+".xml");
                    createExporter().exportGraph(MergeSortStage.getDAG(), file_to_save);
                    System.out.println("Il Dag è stato salvato nel seguente file: " + file_to_save.getAbsoluteFile());
                }
                
                if(viewdag)
                {
                    if(debug)
                        new GraphVisualizer(MergeSortStage.getDAG());
                    else
                        System.err.print("Non posso mostrare il DAG di esecuzione se l'algoritmo non viene eseguito in debug mode!");
                }   
            }
            catch (ArgumentParserException e)
            {
                parser.handleError(e);
            }
	    }
	    catch (Exception e)
	    {
            System.out.print("Errore non previsto!");
            e.printStackTrace();
	    }
	    catch (AssertionError ae) 
	    {
            System.out.print("Asserzione violata!");
            ae.printStackTrace();
	    }
	}
	
	/**
	 * Crea un oggeto per esportare i grafi
	 * @return
	 */
    public static GraphMLExporter<MergeSortStage, NoLabelDefaultEdge> createExporter()
    {
        //identificatore univoco per i nodi
        ComponentNameProvider<MergeSortStage> vertexIdProvider = v -> String.valueOf(v.hashCode());
    
        ComponentNameProvider<MergeSortStage> vertexLabelProvider = null;
     
        //attributi da salvare durante l'esportazione 1/2
        ComponentAttributeProvider<MergeSortStage> vertexAttributeProvider = v -> 
        {
            Map<String, Attribute> m = new HashMap<>();
            m.put("id", DefaultAttribute.createAttribute(v.getTaskID()));
            m.put("stage_type", DefaultAttribute.createAttribute(v.getStageType().toString()));
            m.put("array", DefaultAttribute.createAttribute(Arrays.toString(v.getResult())));
            m.put("at_time", DefaultAttribute.createAttribute(export_formatter.format(v.getTime())));
            m.put("n_forks", DefaultAttribute.createAttribute(v.getSubForks()));
            return m;
        };  
    
        ComponentNameProvider<NoLabelDefaultEdge> edgeIdProvider = new IntegerComponentNameProvider<>();
    
        ComponentNameProvider<NoLabelDefaultEdge> edgeLabelProvider = null;
    
        // attributi dei nodi
        ComponentAttributeProvider<NoLabelDefaultEdge> edgeAttributeProvider = e -> {
            Map<String, Attribute> m = new HashMap<>();
            return m;
        };
    
        GraphMLExporter<MergeSortStage, NoLabelDefaultEdge> exporter = new GraphMLExporter<>
        (
            vertexIdProvider, 
            vertexLabelProvider, 
            vertexAttributeProvider, 
            edgeIdProvider,
            edgeLabelProvider, 
            edgeAttributeProvider
        );
    
        //attributi da salvare durante l'esportazione 2/2
        exporter.registerAttribute("id", AttributeCategory.NODE, AttributeType.INT);
        exporter.registerAttribute("stage_type", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("array", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("at_time", AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("n_forks", AttributeCategory.NODE, AttributeType.INT);
    
        return exporter;
    } 
    
    /**
     * Crea un oggeto per importare  i grafi
     * @return
     */
    public static GraphMLImporter<MergeSortStage, NoLabelDefaultEdge> createImporter()
    {
        //impostazioni con cui ricreare i nodi
        VertexProvider<MergeSortStage> vertexProvider = (id, attributes) -> {
            MergeSortStage v = new MergeSortStage
            (
             Integer.valueOf(attributes.get("id").toString()),
             attributes.get("stage_type").toString(),
             Arrays.stream(attributes.get("array").toString().substring(1, attributes.get("array").toString().length()-1).split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray(),
             LocalDateTime.parse(attributes.get("at_time").toString(), export_formatter),
             Integer.valueOf(attributes.get("n_forks").toString())
            );
            return v;
        };

        //impostazioni con cui ricreare gli archi
        EdgeProvider<MergeSortStage, NoLabelDefaultEdge> edgeProvider = (from, to, label, attributes) -> new NoLabelDefaultEdge();

        /*
         * Create the graph importer with a vertex and an edge provider.
         */
        GraphMLImporter<MergeSortStage, NoLabelDefaultEdge> importer =
            new GraphMLImporter<>(vertexProvider, edgeProvider);

        return importer;
    }
    
    /**
     * Controlla se l'array di interi passato è ordinato in modo crescente.
     * 
     * @param array array da verificare
     * @return true se l'array è ordinato in modo crescente, false altrimenti
     */
    public static boolean checkArray(int[] array)
    {
    	if(array.length<2)
    		return true;
    	
    	for (int i = 1; i < array.length; i++)
    	{
    		if (array[i-1] > array[i])
    			return false;
    	}
    	
    	return true;
    }
    
    /**
     * Ritorna sotto forma di stringa la differenza di tempo fra due {@link LocalDateTime}, nel seguente formato
     * HH:mm:ss.SSS
     * 
     * @param start_time, tempo di inizio
     * @param end_time, tempo di fine
     * @return
     */
    public static String subtractTime(LocalDateTime start_time, LocalDateTime end_time)
    {
        LocalDateTime fromTemp = LocalDateTime.from(start_time);

        long hours = fromTemp.until(end_time, ChronoUnit.HOURS);
        fromTemp = fromTemp.plusHours(hours);

        long minutes = fromTemp.until(end_time, ChronoUnit.MINUTES);
        fromTemp = fromTemp.plusMinutes(minutes);

        long seconds = fromTemp.until(end_time, ChronoUnit.SECONDS);
        fromTemp = fromTemp.plusSeconds(seconds);
        
        long millis = fromTemp.until(end_time, ChronoUnit.MILLIS);
        
        return String.format("%02d", hours)+":"+String.format("%02d",minutes)+":"+String.format("%02d",seconds)+"."+String.format("%03d",millis);
    }
}


