/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.*; //to use collections


/**
 * Features of URL Pool:
 * exclusive instance of this class
 * pair of URL and its depth
 * two lists storing pairs, one all URL seen and the other pending
 * all thread operations taken care of here
 * Count of waiting threads
 * @author Nio
 */
public class URLPool {
    //pool of pending URL-Depth pairs
    private LinkedList<URLDepthPair> pendingURL;
    //pool of all seen URL-Depth pairs
    private LinkedList<URLDepthPair> seenURL; //LinkedList: superior performance on add, remove
    //this collection is used to check if a URL is already visited, if so, ignore it
    private ArrayList<String> visitedURL; //ArrayList: good at random access, fast add to end
    private static int countWaitingThread;
    
    public LinkedList<URLDepthPair> getPending()
    {
        return this.pendingURL;
    }
    public LinkedList<URLDepthPair> getSeen()
    {
        return this.seenURL;
    }
    //initialize count of waiting threads to 0 in constructor
    public URLPool()
    {
        countWaitingThread = 0;
        pendingURL = new LinkedList<>();
        seenURL = new LinkedList<>();
        visitedURL = new ArrayList<>();
    }
    /**
     * get a URL-Depth pair from URL pool
     * pending if there is no items in the pool
     * keep track of the amount of waiting threads
     * @return 
     */
    public synchronized URLDepthPair get()
    {
        URLDepthPair pair = null;
        while(pendingURL.isEmpty())
        {
            try
            {
                countWaitingThread++;
                System.err.println("++++++++++++++++++++");
                this.wait();
                countWaitingThread--;
                System.err.println("--------------");
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        pair = pendingURL.removeFirst();
        return pair;
    }
    /**
     * check conditions: URL not appeared yet
     * and the max depth is not reached
     * if met, add URL to corresponding collections, call notify
     * @param pair
     * @return true if successfully added to pending pool, otherwise false
     */
    public synchronized boolean add(URLDepthPair pair, int maxDepth)
    {
        boolean added = false;
        if(pair instanceof URLDepthPair)
        {
            //only process if the URL is not already visited
            if(!visitedURL.contains(pair.getURL()))
            {
                //and only if the depth is not reached
                if(pair.getDepth() < maxDepth)
                {
                    visitedURL.add(pair.getURL());
                    seenURL.addLast(pair);
                    pendingURL.addLast(pair);
                    added = true;
                    this.notify();
                }
            }
            
        }
        return added;     
    }

    //synchronize this shared resource
    public synchronized int getNumWaitingThreads()
    {
        return this.countWaitingThread;
    }
}
