package mergesort;

import java.util.Arrays;

public class MergeSortUtils 
{    
    /**
     * Restituisce l'array pari alla metà sinistra di quello dato in input.
     * 
     * @param array array di cui sarà presa la parte sinistra
     * @return parte sinistra dell'array dato
     */
    public static int[] take_half_left(int[] array)
    {
        return Arrays.copyOfRange(array, 0, array.length/2);
    }
    
    /**
     * Restituisce l'array pari alla metà destra di quello dato in input.
     * 
     * @param array array di cui sarà presa la parte destra
     * @return parte destra dell'array dato
     */
    public static int[] take_half_right(int[] array)
    {
        return Arrays.copyOfRange(array, array.length/2, array.length);
    }
    
    /**
     * Concatena i due array dati in input (nell'ordine dei parametri), 
     * e restituisce l'array risultante.
     * 
     * @param array_left array che sarà la parte sinistra del nuovo array
     * @param array_right array che sarà la parte destra del nuovo array
     * @return l'array risultate dalla concatenazione dei due array dati
     */
    public static int[] merge_arrays(int[] array_left, int[] array_right)
    {
        int[] rtn_array = new int[array_left.length+array_right.length];
        System.arraycopy(array_left, 0, rtn_array, 0, array_left.length);
        System.arraycopy(array_right, 0, rtn_array, array_left.length, array_right.length);
        return rtn_array;
    }
    
    /**
     * Unisce due array di interi primitivi in un nuovo array,
     * la funzione lavora supponendo che i due array passati nei parametri siano ordinati,
     * se uno dei due array passati tra i parametri, o tutti e due, sono nulli, o senza elementi,
     * la funzione restituscie una copia dell'unico array valido, o un nuovo array di zero elementi.
     * 
     * La funzione restitusce in modo sicuro un array di interi.
     * 
     * Se uno dei due array passati tra i parametri, o tutti e due, 
     * non sono correttamente ordinati in modo crescente, il comportamento è indefinito.
     * 
     * @param array_left, array di sinistra da unire (ordinato)
     * @param array_right, array di destra da unire (ordinato)
     * @return nuovo array unito (ordinato)
     */
    public static int[] merge_two_sorted_array(int[] array_left, int[] array_right)
    {         
        int[] rtn_array;
        
        if( (array_left.length == 0 || array_left == null) && (array_right.length == 0 || array_right == null) )
        {
            rtn_array = new int[] {};
        }
        else if(array_left.length > 0 && (array_right.length == 0 || array_right == null))
        {
            rtn_array = Arrays.copyOfRange(array_left, 0, array_left.length);
        }
        else if((array_left.length == 0 || array_left == null) && array_right.length > 0)
        {
            rtn_array = Arrays.copyOfRange(array_right, 0, array_right.length);
        }
        else
        {
            rtn_array = new int[array_left.length+array_right.length];
            int lft_i = 0, rgt_i = 0, dst_i = 0;
            
            // Unisco fino a che entrambi gli array non sono vuoti
            while(array_left.length > lft_i && array_right.length > rgt_i)
            {
                if(array_left[lft_i] < array_right[rgt_i])
                {
                    rtn_array[dst_i] = array_left[lft_i];
                    lft_i++;
                }
                else
                {
                    rtn_array[dst_i] = array_right[rgt_i];
                    rgt_i++;
                }
                dst_i++;
            }
            
            // Copio le eventuali parti avanzate (o a sinistra o a destra)
            if(array_left.length > lft_i)
                System.arraycopy(array_left, lft_i, rtn_array, dst_i, array_left.length-lft_i);
            else if(array_right.length > rgt_i)
                System.arraycopy(array_right, rgt_i, rtn_array, dst_i, array_right.length-rgt_i);       
        }
        
        return rtn_array;
    }
    
    /**
     * Dato un array, gli indici di inizio e fine di una sua sottosequenza 
     * e un indice interno a tale sottosequenza, fa il merge in loco delle sottosequenze:
     * left - center
     * center - right
     * 
     * Se le due sottosequenze non sono ordinate in modo crescente, il comportamento è indefinito.
     * 
     * @param a array su cui fare il emrge in loco
     * @param left indice di inizio della parte dell'array da considerare (inclusivo)
     * @param center indice di divisione delle due sottosequenze
     * @param right indice di fine della parte dell'array da considerare (inclusivo)
     * @throw RuntimeException se gli indici sono incongruenti (left>right || left<0 || right>=a.length || center<left || right<center)
     */
    public static void merge(int[] a, int left, int center, int right)
    {   
        if (left>right || left<0 || right>=a.length || center<left || right<center)
            throw new RuntimeException("Merge: indici errati ("+left+" ; "+center+" ; "+right+")");
        
        // Indici della parte d'inizio dei sottoarray
        int i = left;
        int j = center+1;
        
        // Array temporaneo
        int z = 0;
        int[] t = new int[right-left+1];
        
        // Unisco fino a che entrambi gli array non sono vuoti
        while(i<=center && j<right)
        {
            if(a[i] <= a[j])
            {
                t[z] = a[i];
                i++;
            }
            else
            {
                t[z] = a[j];
                j++;
            }
            z++;
        }
        
        // Copio le eventuali parti avanzate (o a sinistra o a destra)
        if(i<center)
            System.arraycopy(a, i, t, z, center-i+1);
        if(j<right)
            System.arraycopy(a, j, t, z, right-j);
        
        // Copio nell'array finale
        System.arraycopy(t, 0, a, left, right-left+1);
    }
    
    /**
     * Ordina l'array dato utilizzando l'argoritmo Insertion Sort
     * 
     * @param array
     */
    public static void insertionSort(int[] array) 
    {
        int i, j;
        for(i = 1; i < array.length; i++) 
        {
           int tmp = array[i];
         
           j = i - 1;
           while(j >= 0 && array[j] > tmp)
           {
               array[j + 1] = array[j];
               j--;
           }
           
           array[j + 1] = tmp; 
        }
   }
    
    /**
     * Dato un'array ordinato ed un'elemento, ritorna l'indice 'i' tali che:
     * - 0 <= i <= arr.length
     * - Tutti gli elementi con indice minore di 'i' sono strettamente minori dell'elemento dato.
     * - Tutti gli elementi con indice maggiore o uguale di 'i' sono maggiori o uguali dell'elemento dato.
     * 
     * Se 'i' = arr.length, allora tutti gli elementi sono strettamente minori dell'elemento dato.
     * Se 'i' = 0, allora tutti gli elementi sono maggiori o uguali all'elemento dato.
     * 
     * Se l'array passato non è ordinato in modo crescente, il comportamento è indefinito.
     * 
     * @param elem, elemento da cercare
     * @param arr, array su cui cercare (ordinato)
     * @return indice con le proprietà sopra descritte.
     */
    public static int search_split_point(int elem, int[] arr) 
    {
        int mid;
        int lo = 0;
        int hi = arr.length;
        while(lo < hi)
        {
            mid = (lo + hi)/2;
            if(elem <= arr[mid])
                hi = mid;
            else
                lo = mid + 1;
        }
        
        return hi;
    }
}
