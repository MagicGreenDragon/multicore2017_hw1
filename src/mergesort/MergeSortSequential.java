package mergesort;

import mergesort.debug.DebugMergeSortSequential;
import mergesort.nodebug.NoDebugMergeSortSequential;

public class MergeSortSequential implements MergeSort
{
    @Override
    public MergeSortStage sort(int[] array, boolean debug_mode)
    {
        MergeSortStage mss = null;
        
        if(debug_mode)
        {
            mss = new DebugMergeSortSequential().sort(array);
        }
        else
        {
            mss = new MergeSortStage(new NoDebugMergeSortSequential().sort(array));
        }
        
        return mss;            
    }
}
