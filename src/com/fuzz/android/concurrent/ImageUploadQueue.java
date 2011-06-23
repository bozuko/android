package com.fuzz.android.concurrent;

import java.util.LinkedList;

public class ImageUploadQueue
{
	
	private static ImageUploadQueue wq = null;
    @SuppressWarnings("unused")
	private final int nThreads;
    private final PoolWorker[] threads;
    @SuppressWarnings("unchecked")
	private final LinkedList queue;
    boolean canAdd = true;;

    public static ImageUploadQueue getInstance(){
    	if(wq == null){
    		wq = new ImageUploadQueue(2);
    	}
    	return wq;
    }
    
    @SuppressWarnings("unchecked")
	public ImageUploadQueue(int nThreads)
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
        	//if(canAdd){
        		queue.addLast(r);
        		queue.notify();
        	//}
        }
    }
    
    public void remove(Runnable r){
    	synchronized(queue) {
    		queue.remove(r);
    	}
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
	
    public void destroy() {
		// TODO Auto-generated method stub
    	canAdd = false;
	}

	public void allow() {
		// TODO Auto-generated method stub
		canAdd = true;
	}

	public void empty() {
		// TODO Auto-generated method stub
		synchronized(queue) {
			queue.clear();
		}
	}
}