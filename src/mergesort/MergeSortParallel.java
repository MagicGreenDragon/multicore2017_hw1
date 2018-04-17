package mergesort;

import mergesort.debug.DebugMergeSortParallel;
import mergesort.nodebug.NoDebugMergeSortParallel;

public class MergeSortParallel implements MergeSort
{
    int cutoff;
    
    public MergeSortParallel(int cutoff)
    {
        this.cutoff = cutoff;
    }
    
    @Override
    public MergeSortStage sort(int[] array, boolean debug_mode) 
    {
        MergeSortStage mss = null;
        
        if(debug_mode)
        {
            mss = new DebugMergeSortParallel(this.cutoff).sort(array);
        }
        else
        {
            mss = new MergeSortStage(new NoDebugMergeSortParallel(this.cutoff).sort(array));
        }
        
        return mss;  
    }
}
