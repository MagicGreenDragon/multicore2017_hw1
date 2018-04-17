package mergesort.nodebug;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import mergesort.MergeSortUtils;

public class NoDebugMergeSortParallel
{    
    @SuppressWarnings("serial")
    private class SortTask extends RecursiveTask<int[]>
    {
        private int[] array;
        
        public SortTask(int[] array)
        {
            this.array = array;
        }
        
        /**
         * Ordina parallelamente l'array dato con MergeSort.
         * Anche l'operazione di Merge è eseguita parallelamente.
         */
        @Override
        protected int[] compute()
        {
            if(array.length <= cutoff)
            {
                // Uso selection sort per la parte sotto cutoff sequenziale
                if(cutoff > 1)
                    MergeSortUtils.insertionSort(array);
            }
            else
            {   
                // Merge parte sinistra (in parallelo)
                SortTask right_ct = new SortTask(MergeSortUtils.take_half_right(array));
                right_ct.fork();
                
                // Merge parte sinistra (su questo thread)
                SortTask left_ct = new SortTask(MergeSortUtils.take_half_left(array));
                
                // Risultati dei mergesort sui sottoarray
                int[] sorted_left_array = left_ct.compute();
                int[] sorted_right_array = right_ct.join();
                
                // Unisco i risultati sul thread corrente in parallelo con altri thread
                array = new MergeTask(sorted_left_array, sorted_right_array).compute();
            }
            
            return array; 
        }        
    }
    
    @SuppressWarnings("serial")
    private class MergeTask extends RecursiveTask<int[]>
    {
        private final int[] a;
        private final int[] b;
        private int[] sub_a_result;
        private int[] sub_b_result;
        
        public MergeTask (int[] array_left, int[] array_right)
        {
            // Seleziona gli array "A" e "B" in base alla lunghezza
            if( array_left.length >= array_right.length )
            {
                this.a = array_left;
                this.b = array_right;
            }
            else
            {
                this.a = array_right;
                this.b = array_left;
            }
        }
        
        /**
         * Esegue parallelamente l'operazione di Merge 
         * di due array ordinati in modo crescente.
         */
        @Override
        protected int[] compute() 
        {
            // Se A è vuoto, lo è anche B (A è l'array più grande)
            if(this.a.length == 0)
                return new int[] {};
            
            // Se B è vuoto ritorna A (sicuro non vuoto)
            if(this.b.length == 0)
                return this.a;
            
            // Se A ha solo 1 elemento, lo ha anche B (A è l'array più grande, e sò già che B non è vuoto)
            if(this.a.length == 1)
                return (this.a[0] <= this.b[0]) ? new int[]{this.a[0], this.b[0]} : new int[]{this.b[0], this.a[0]};
            
            // Applico il cutoff al merge (se necessario)
            if( cutoff != 1 && (this.a.length+this.b.length) <= cutoff )
                return MergeSortUtils.merge_two_sorted_array(this.a, this.b);
            
            // Trovo l'indice dell'elemento intermedio nell'array A
            int median = this.a.length / 2;
            
            // Cerco nell'array B il primo numero maggiore o uguale di A[median]
            int d = MergeSortUtils.search_split_point(this.a[median], this.b);
            
            // Se la parte sinistra dell'array B è vuota
            if(d==0)
            {
                // Esegue sulle 2 metà destre (in questo thread)
                MergeTask subB = new MergeTask
                (
                    Arrays.copyOfRange(this.a, median, this.a.length), 
                    Arrays.copyOfRange(this.b, d, this.b.length)
                );
                
                sub_b_result = subB.compute();
                
                // La parte destra è data dalle due parti destre, mentre la parte sinistra è data dalla sola parte sinistra di A
                return MergeSortUtils.merge_arrays(Arrays.copyOfRange(this.a, 0, median), sub_b_result);
            }
            
            // Se la parte destra dell'array B è vuota
            if(d==this.b.length)
            {
                // Esegue sulle 2 metà siniste (in questo thread)
                MergeTask subA = new MergeTask
                (
                    Arrays.copyOfRange(this.a, 0, median), 
                    Arrays.copyOfRange(this.b, 0, d)
                );
                
                sub_a_result = subA.compute();
                
                // La parte sinistra è data dalle due parti sinistre, mentre la parte destra è data dalla sola parte destra di A
                return MergeSortUtils.merge_arrays(sub_a_result, Arrays.copyOfRange(this.a, median, this.a.length));
            }
            
            // Esegue sulle 2 metà siniste (in parallelo)
            MergeTask subA = new MergeTask
            (
                Arrays.copyOfRange(this.a, 0, median), 
                Arrays.copyOfRange(this.b, 0, d)
            );
            subA.fork();
            
            // Esegue sulle 2 metà destre (in questo thread)
            MergeTask subB = new MergeTask
            (
                Arrays.copyOfRange(this.a, median, this.a.length), 
                Arrays.copyOfRange(this.b, d, this.b.length)
            );
            
            // Attende i risultati
            sub_b_result = subB.compute();
            sub_a_result = subA.join();
            
            // Fonde i due array risultanti
            return MergeSortUtils.merge_arrays(sub_a_result, sub_b_result);
        }       
    }
    
    private volatile int cutoff;
    private final ForkJoinPool fj;
    
    public NoDebugMergeSortParallel()
    {
        this(1);
    }
    
    public NoDebugMergeSortParallel(int cutoff)
    {
        this.cutoff = cutoff;
        this.fj = new ForkJoinPool();
    }
    
    public int[] sort(int[] array)
    {
        SortTask ct = new SortTask(array);
        
        //necessaria pulizia forzata in caso di usi consecutivi della classe
        System.gc();
        
        return fj.invoke(ct);
    }

}

