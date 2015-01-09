/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.*; //to use collections
import java.io.*; //to use streams
import java.net.*; //to use sockets

/**
 * entry of the program
 * main function implements the following steps:
 *      process command-line arguments, inform user of any input errors
 *      create instance of URL pool, add initial URL into pool
 *      create numbers of crawler task and threads to run them, starting the threads
 *      wait for the crawling to complete
 *      print out all URLs
 * @author Nio
 */
public class WebCrawler 
{
   
    /**
     * 
     * @param args: take 3 parameters: 
     * the first argument specifies initial URL, 
     * the second argument specifies the max depth of URL
     * the third argument specifies the number of crawler threads
     */
    public static void main(String[] args)
    {
        int currentDepth = 0;
        int maxDepth = 0;
        int numThreads = 0;
        //get mad depth from arguments
        try
        {
            maxDepth = Integer.parseInt(args[1]);
        }
        catch(NumberFormatException nfe)
        {
            //if the first argument is not a valid integer, print error message and exit with an error code.
            System.out.println("The second argument must be an integer to specify the max depth.");
            System.exit(1);
        }
        
        //get numbers of threads from arguments
        try
        {
            numThreads = Integer.parseInt(args[2]);
        }
        catch(NumberFormatException nfe)
        {
            //if the first argument is not a valid integer, print error message and exit with an error code.
            System.out.println("The third argument must be an integer to specify the number of threads.");
            System.exit(1);
        }
        
        //create an instance of URL pool, put the initial URL into pool with depth 0
        URLPool pool = new URLPool();
        URLDepthPair initialURL = new URLDepthPair(args[0], 0);
        pool.add(initialURL, maxDepth);
        //create the number of crawler tasks, and threads to run them
        for(int i = 0; i < numThreads; i++)
        {
//            try
//            {
//                Thread.sleep(100);
//            }
//            catch(InterruptedException e)
//            {
//                e.printStackTrace();
//            }
            CrawlerTask task = new CrawlerTask(pool, maxDepth);
            Thread thread = new Thread(task);
            thread.start();
        }
        //set a short time between each check of finish condition to avoid dreaded polling
        while( (pool.getNumWaitingThreads() != numThreads) || (!pool.getPending().isEmpty() ) )
        //while(!pool.getPending().isEmpty())
        {
            try
            {
                Thread.sleep(100);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        if(pool.getPending().isEmpty())
        {
            for(URLDepthPair pair : pool.getSeen())
            {
                System.out.println(pair.getURL().toLowerCase());
            }
            System.exit(0);
        }
        else
        {
            System.exit(1);
        }
    }
        
}
