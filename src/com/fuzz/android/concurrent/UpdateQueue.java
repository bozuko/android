package com.fuzz.android.concurrent;

import java.util.LinkedList;

public class UpdateQueue
{
	
	private static UpdateQueue wq = null;
    private final int nThreads;
    private final PoolWorker[] threads;
    @SuppressWarnings("unchecked")
	private final LinkedList queue;

    public static UpdateQueue getInstance(){
    	if(wq == null){
    		wq = new UpdateQueue(2);
    	}
    	return wq;
    }
    
    @SuppressWarnings("unchecked")
	public UpdateQueue(int nThreads)
    {
        this.nThreads = nThreads;
        queue = new LinkedList();
        threads = new PoolWorker[nThreads];

        for (int i=0; i<nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    @SuppressWarnings("unchecked")
	public void execute(Runnable r) {
        synchronized(queue) {
            queue.addLast(r);
            queue.notify();
        }
    }
    
    public void remove(Runnable r){
    	synchronized(queue) {
    		queue.remove(r);
    	}
    }

    public int getnThreads() {
		return nThreads;
	}

	private class PoolWorker extends Thread {
        public void run() {
            Runnable r;

            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try
                        {
                            queue.wait();
                        }
                        catch (InterruptedException ignored)
                        {
                        }
                    }

                    r = (Runnable) queue.removeFirst();
                }

                // If we don't catch RuntimeException, 
                // the pool could leak threads
                try {
                    r.run();
                }
                catch (RuntimeException e) {
                    // You might want to log something here
                }
            }
        }
    }
}