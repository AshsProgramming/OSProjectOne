package nachos.threads;

import nachos.machine.Lib;
	
	public class MinHeap {
		 public long key[];
	 public int n;
	 public Object thread[];
	    public MinHeap()
	    {
	        n = -1;
	        key = new long[1024];
	        thread = new Object[1024];
	    }

	    public long firstKey()
	    {
	        if (n >= 0)
	            return key[0];
	        else
	            return -1;
	    }
	    public void add(long inkey, Object inthread) //heap sort
	    {
	        int allocated = key.length;
	       

	        if ( n + 1 >= allocated)
	        {
	            allocated *=2;
	            long newKey[] = new long[allocated];
	            Object newThread[] = new Object[allocated];
	            System.arraycopy(key, 0, newKey, 0, n);
	            System.arraycopy(thread, 0, newThread, 0, n);
	            key = newKey;
	            thread = newThread;
	        }
	         n++;
	        int i = n;
	        while (i > 0)
	        {
	            if (inkey >= key[i/2])
	                break;
	            key[i] = key[i/2];
	            thread[i] = thread[i/2];
	            i /= 2;
	        }
	    thread[i] = inthread;
	    key[i] = inkey;

	    }
	    public Object removeFirst()
	    {
	        Lib.assertTrue(n >= 0);
	        Object x = thread[0];
	        long k = key[n];
	        int i = 0, j= 1;
	        n--;
	        for (; j<=n;)
	        {
	            if (j<n)
	                if (key[j] > key[j+1]) j++;

	        if (k <= key[j])
	            break;

	        key[i] = key[j];
	        thread[i] = thread[j];
	        i = j;
	        j*=2;
	    }
	    key[i] = key[n+1];
	    thread[i] = thread[n+1];

	    return x;
	    }
	    public long size()
	    {
	        return n+1;
	    }
	    
	}

