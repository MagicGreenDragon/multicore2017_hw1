package mergesort;

public interface MergeSort 
{
    /**
     * Ordina l'array con un qualche tipo di MergeSort, ritornando il risultato.
     * 
     * @param array l'array da ordinare
     * @return
     */
	public MergeSortStage sort(int[] array, boolean debug_mode);
}
