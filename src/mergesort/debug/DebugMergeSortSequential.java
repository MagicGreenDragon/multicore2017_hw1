package mergesort.debug;

import mergesort.MergeSortStage;
import mergesort.MergeSortUtils;

public class DebugMergeSortSequential
{	
    public DebugMergeSortSequential() {}
	
    public MergeSortStage sort(int[] array)
    {
        MergeSortStage merge_result = new MergeSortStage(array);
        
        mergesort_seq(array, 0, array.length-1);
        
        return merge_result;
    }
    
    /**
     * Ordina sequenzialmente l'array dato con MergeSort.
     * 
     * @param a array da ordinare
     * @param left indice di inizio della parte dell'array da considerare (inclusivo)
     * @param right indice di fine della parte dell'array da considerare (inclusivo)
     */
    private void mergesort_seq(int[] a, int left, int right)
    {
    	if(left < right)
    	{
            int center = (left+right)/2;
            mergesort_seq(a, left, center);
            mergesort_seq(a, center+1, right);
            MergeSortUtils.merge(a, left, center, right);
        }
    }
}
