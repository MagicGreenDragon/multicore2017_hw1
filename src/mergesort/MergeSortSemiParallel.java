package mergesort;

import mergesort.debug.DebugMergeSortSemiParallel;
import mergesort.nodebug.NoDebugMergeSortSemiParallel;

public class MergeSortSemiParallel implements MergeSort
{
    int cutoff;
    
    public MergeSortSemiParallel(int cutoff)
    {
        this.cutoff = cutoff;
    }
    
    @Override
    public MergeSortStage sort(int[] array, boolean debug_mode) 
    {
        MergeSortStage mss = null;
        
        if(debug_mode)
        {
            mss = new DebugMergeSortSemiParallel(this.cutoff).sort(array);
        }
        else
        {
            mss = new MergeSortStage(new NoDebugMergeSortSemiParallel(this.cutoff).sort(array));
        }
        
        return mss;  
    }
}
