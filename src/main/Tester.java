package main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import mergesort.MergeSort;
import mergesort.MergeSortParallel;
import mergesort.MergeSortSemiParallel;
import mergesort.MergeSortSequential;
import mergesort.MergeSortStage;

// Classe per testare il corretto funzionamento delle Classi: MergeSortSequential

public class Tester
{
    final static DateTimeFormatter time_formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public enum SortType
    {
       S, SP, P
    }

    static class TestModel
    {
        class Test_Info
        {
            final int test_id;
            final String test_dest;

            Test_Info(int test_id, String test_dest)
            { this.test_id = test_id; this.test_dest = test_dest; }
        }

    	private int test_counter;
    	private List<Test_Info> fails;
    	private boolean global_result;
    	private boolean usable;
    	private LocalDateTime start_time;
    	private LocalDateTime end_time;

    	/**
    	 * Inizializza l'oggetto tester stampando una stringa per segnalare l'inizio dei tests
    	 */
    	public TestModel()
    	{
    		System.out.println("START TESTER");

    		this.test_counter = 0;
    		this.fails = new ArrayList<>();
    		this.global_result = true;
    		this.usable = true;
    	}

    	/**
    	 * Rende il tester inutilizzabile e stampa la conclusione dei tests
    	 */
    	public void close()
    	{
    		if(!this.usable)
    			return;

    		System.out.println("\n---------------------------------------------\n");
            System.out.println(this.getResultString());
            System.out.println("\nEND TESTER");
            this.usable = false;
    	}

    	// Utils

    	/**
    	 * Ritorna il risultato finale del test in una stringa
    	 *
    	 * @return {@link String} del risultato
    	 */
    	private String getResultString()
    	{
    		if( this.global_result )
            	return "ALL TESTS PASSED!";

    		String f = "";
    		for( Test_Info test_fail : this.fails )
    			f += "- " + test_fail.test_id+ ") " + test_fail.test_dest + "\n";

    		return "THESE TESTS ARE FAILED:\n"+f;
    	}

    	/**
    	 * Aggiorna lo stato del tester,
    	 * se la stringa passata tra i paramentri è nulla, il test ha avuto successo,
    	 * altrimenti il tester segna il nome del test fallito e il suo id,
    	 * cambiando lo stato di (global_result) segnando che non tutti i test hanno avuto successo.
    	 *
    	 * @param test_descr
    	 */
    	private void update(String test_descr)
    	{
    		this.test_counter++;

    		if(Objects.nonNull(test_descr))
    		{
    		    this.global_result = false;
        		this.fails.add(new Test_Info(this.test_counter, test_descr));
    		}
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

    	//metodo principale Tester

    	public void runTest(int elem_n, SortType type, boolean decr, int cutoff, boolean debug)
    	{
    		if(!this.usable)
    			return;

    		// Variabili di lavoro, pre-inizializzazione
            MergeSort ms = null;

    		//creo descrizione del test richiesto
    		String test_descr = "array of "+elem_n+" elements with "+(cutoff>1?cutoff+" of":"no")+" cutoff and "+(debug?"in":"not in")+" debug mode";

            switch(type)
            {
                case S:
                    ms = new MergeSortSequential();
                    test_descr="MERGESORT SEQUENTIAL{S}   -> "+test_descr;
                    break;
                case SP:
                    ms = new MergeSortSemiParallel(cutoff);
                    test_descr="MERGESORT SEMIPARALLEL{SP} -> "+test_descr;
                    break;
                case P:
                    ms = new MergeSortParallel(cutoff);
                    test_descr="MERGESORT PARALLEL{P}     -> "+test_descr;
                    break;
            }

    		if(elem_n<1 || cutoff<1 || cutoff>elem_n)
        	{
        		System.out.println((this.test_counter+1)+") Invalid test -> elem_n = "+elem_n+" ; cutoff = "+cutoff+" ; debug = "+debug);
        		this.update(test_descr);
        	}

            System.out.println("\n---------------------------------------------\n");

            // Inizializzo classe di sort

            // Variabili di lavoro
            MergeSortStage msr = null;
            int[] unsorted_array = null;
            int[] sorted_array = null;

            try {
                Thread.sleep(100);
            } catch (InterruptedException ee) {}

            //inizio i test
            System.out.println((this.test_counter+1)+") "+test_descr);

            String spacer = String.join("", Collections.nCopies(((this.test_counter+1)+") ").length(), " "));

            try
            {
                for(int try_counter=1; try_counter<4; try_counter++)
                {
                    System.out.println(spacer+"- Try: "+try_counter+" / 3");

                    start_time = LocalDateTime.now();

                    // Genero array casuale
            		unsorted_array = decr ? genInverseRangeArray(elem_n) : genRandomIntArray(elem_n);


                	// Eseguo il MergeSort
                    msr = ms.sort(unsorted_array, debug);

                    // Confronto gli arrays
                    sorted_array = checkAndSort(msr.getResult());
                    if(sorted_array!=null)
                    	throw new IllegalStateException();

                    end_time = LocalDateTime.now();

                    System.out.println(spacer+"  "+"SUCCESSFUL!");
                    System.out.println(spacer+"  "+"Start sorting at : "+time_formatter.format(start_time));
                	System.out.println(spacer+"  "+"End   sorting at : "+time_formatter.format(end_time));
                    System.out.println(spacer+"  "+"Time Elapsed     : "+subtractTime(start_time, end_time));
                }

                this.update(null);
            }
            catch(IllegalStateException ise)
            {
                System.err.println(spacer+"  "+"FAIL!");
                System.err.println(spacer+"  "+"Sorting error: ");
                System.err.println(spacer+"  "+"Expected: "+arraytoString(sorted_array));
                System.err.println(spacer+"  "+"Result  : "+arraytoString(msr.getResult()));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ee) {}
                this.update(test_descr);
            }
            catch(Exception e)
            {
                System.err.println(spacer+"  "+"FAIL!");
                System.err.println(spacer+"  "+"Error: ");
                e.printStackTrace();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ee) {}
                this.update(test_descr);
            }
    	}
    }

    public static void main(String[] args)
    {
    	// Inizializzo classe test
    	TestModel tm = new TestModel();

    	//Inizio test

    	//TESTS ALGORIRMO SEQUENZIALE
    	//S

    	//no debug

    	tm.runTest(10, SortType.S, false, 1, false);         //1
    	tm.runTest(10, SortType.S, true, 1, false);          //2

    	tm.runTest(1000, SortType.S, false, 1, false);       //3
    	tm.runTest(1000, SortType.S, false, 1, false);       //4

    	tm.runTest(2000, SortType.S, false, 1, false);       //5
    	tm.runTest(2000, SortType.S, false, 1, false);       //6

    	tm.runTest(4000, SortType.S, false, 1, false);       //7
    	tm.runTest(4000, SortType.S, false, 1, false);       //8

    	//with debug

        tm.runTest(10, SortType.S, false, 1, true);          //9
        tm.runTest(10, SortType.S, true, 1, true);           //10

        tm.runTest(1000, SortType.S, false, 1, true);        //11
        tm.runTest(1000, SortType.S, false, 1, true);        //12

        tm.runTest(2000, SortType.S, false, 1, true);        //13
        tm.runTest(2000, SortType.S, false, 1, true);        //14

        tm.runTest(4000, SortType.S, false, 1, true);        //15
        tm.runTest(4000, SortType.S, false, 1, true);        //16

    	//TESTS ALGORIRMO SEMI-PARALLELO
    	//SP

        //no debug

        tm.runTest(10, SortType.SP, false, 1, false);        //17
        tm.runTest(10, SortType.SP, false, 10, false);       //18

        tm.runTest(1000, SortType.SP, false, 1, false);      //19
        tm.runTest(1000, SortType.SP, false, 10, false);     //20

        tm.runTest(10000, SortType.SP, false, 1, false);     //21
        tm.runTest(10000, SortType.SP, false, 20, false);    //22

        tm.runTest(200000, SortType.SP, false, 1, false);    //23
        tm.runTest(200000, SortType.SP, false, 20, false);   //24

        tm.runTest(400000, SortType.SP, false, 1, false);    //25
        tm.runTest(400000, SortType.SP, false, 20, false);   //26

        //with debug

        tm.runTest(10, SortType.SP, false, 1, true);         //27
        tm.runTest(10, SortType.SP, false, 10, true);        //28

        tm.runTest(1000, SortType.SP, false, 1, true);       //29
        tm.runTest(1000, SortType.SP, false, 10, true);      //30

        tm.runTest(10000, SortType.SP, false, 1, true);      //31
        tm.runTest(10000, SortType.SP, false, 20, true);     //32

        tm.runTest(200000, SortType.SP, false, 1, true);     //33
        tm.runTest(200000, SortType.SP, false, 20, true);    //34

        tm.runTest(400000, SortType.SP, false, 1, true);     //35
        tm.runTest(400000, SortType.SP, false, 20, true);    //36


        //TESTS ALGORIRMO PARALLELO
        //P

        //no debug

    	tm.runTest(1000, SortType.P, false, 1, false);       //37
        tm.runTest(1000, SortType.P, true, 1, false);        //38

        tm.runTest(10000, SortType.P, false, 1, false);      //39
        tm.runTest(10000, SortType.P, false, 20, false);     //40

        tm.runTest(200000, SortType.P, false, 1, false);     //41
        tm.runTest(200000, SortType.P, false, 20, false);    //42

        tm.runTest(400000, SortType.P, false, 1, false);     //43
        tm.runTest(400000, SortType.P, false, 20, false);    //44

    	tm.runTest(1000000, SortType.P, false, 1, false);    //45
    	tm.runTest(1000000, SortType.P, false, 200, false);  //46

    	tm.runTest(5000000, SortType.P, false, 200, false);  //47
    	tm.runTest(10000000, SortType.P, false, 200, false); //48

    	//with debug

        tm.runTest(1000, SortType.P, false, 1, true);        //49
        tm.runTest(1000, SortType.P, true, 1, true);         //50

        tm.runTest(10000, SortType.P, false, 1, true);       //51
        tm.runTest(10000, SortType.P, false, 20, true);      //52

        tm.runTest(200000, SortType.P, false, 1, true);      //53
        tm.runTest(200000, SortType.P, false, 20, true);     //54

        tm.runTest(400000, SortType.P, false, 1, true);      //55
        tm.runTest(400000, SortType.P, false, 20, true);     //56

        tm.runTest(1000000, SortType.P, false, 1, true);     //57
        tm.runTest(1000000, SortType.P, false, 200, true);   //58

        tm.runTest(5000000, SortType.P, false, 200, true);   //59
        tm.runTest(10000000, SortType.P, false, 200, true);  //60

    	// Fine test
    	tm.close();
    }

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
     * Restituisce un array generato casualmente della lunghezza indicata, ordinato in ordine decrescente
     * Asserisce che n sia maggiore di 0
     *
     * @param n, numero degli elementi
     * @return array di int non ordinato
     */
	public static int[] genInverseRangeArray(int n)
	{
	    assert n > 0;

        int[] rtn_array = new Random().ints(n, -2147483648, 2147483647).toArray();
        Arrays.parallelSort(rtn_array);
        int i=0, k=rtn_array.length-1, tmp;
        while (i < k)
        {
            tmp = rtn_array[i];
            rtn_array[i] = rtn_array[k];
            rtn_array[k] = tmp;
            i++;
            k--;
        }
        return rtn_array;
	}

    /**
     * Ritorna un array di int in formato stringa stampabile
     *
     * @param array
     * @return array in stringa
     */
    public static String arraytoString(int[] array)
    {
        String rtn_str="[";

        for(int num : array)
            rtn_str+=num+", ";

        rtn_str = rtn_str.substring(0,rtn_str.length()-2);
        return rtn_str+"]";
    }

    /**
     * Controlla se l'array di interi passato è ordinato in modo crescente, e nel caso lo ordina.
     *
     * @param array array da verificare
     * @return null se l'array è ordinato in modo crescente, l'array ordinato altrimenti
     */
    public static int[] checkAndSort(int[] array)
    {
    	if(array==null || array.length<2)
    		return null;

    	boolean sorted = true;
    	for (int i = 1; i < array.length; i++)
    	{
    		if (array[i-1] > array[i])
    		{
    			sorted = false;
    			break;
    		}
    	}

    	if(sorted)
    		return null;

    	// Ritorna array ordinato
    	int[] s = new int[array.length];
    	System.arraycopy(array, 0, s, 0, array.length);
    	Arrays.parallelSort(s);
    	return s;
    }
}
