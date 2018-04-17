package mergesort.nodebug;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import mergesort.MergeSortUtils;

public class NoDebugMergeSortSemiParallel
{
	@SuppressWarnings("serial")
	class MergeSortThread extends RecursiveTask<int[]>
    {
        private int[] array;
        
        public MergeSortThread(int[] array)
        {
            this.array = array;
        }
        
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
                //creo e avvio su un altro thread il merge della parte sinistra
                MergeSortThread right_msst = new MergeSortThread(MergeSortUtils.take_half_right(array));
                right_msst.fork();
                
                //creo e avvio su questo thread il merge della parte destra                
                MergeSortThread left_msst = new MergeSortThread(MergeSortUtils.take_half_left(array));
                
                //risultati dei mergesort sui sottoarray
                int[] sorted_left_array = left_msst.compute();
                int[] sorted_right_array = right_msst.join();   
                
                //unisco i risultati sul thread corrente                    
                array = MergeSortUtils.merge_two_sorted_array(sorted_left_array, sorted_right_array);    
            }   
            
            return array;
        }        
    }
    
    private volatile int cutoff;
    private final ForkJoinPool fj;
    
    public NoDebugMergeSortSemiParallel()
    {
        this(1);
    }
    
    public NoDebugMergeSortSemiParallel(int cutoff)
    {
        this.cutoff = cutoff;
        this.fj = new ForkJoinPool();
    }
    
    public int[] sort(int[] array)
    {
        MergeSortThread msst = new MergeSortThread(array);
        
        return fj.invoke(msst);
    }
}

