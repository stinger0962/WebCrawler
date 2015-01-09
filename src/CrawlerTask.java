
import java.util.*; //to use collections
import java.io.*; //to use streams
import java.net.*; //to use sockets

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *each CrawlerTask has an instance of a shared URL pool
 *CrawlerTask repeats following steps until URL pool is empty
 *      retrieve a pair from pool
 *      retrieve the web page by URL
 *      search the page for URL
 *      add new URL pair to pool
 *      
 * @author Nio
 */
public class CrawlerTask implements Runnable{
    
    
    private URLPool pool;
    private int maxDepth;
    private Socket sock;
    //80 is the port that HTTP listens to
    public static final int port = 80;
    //the prefix of a URL from HTML code
    public static final String HTML_LINK_PREFIX = "a href=\"";
    //pass pool and max depth through constructor
    public CrawlerTask(URLPool pool, int maxDepth)
    {
        this.pool = pool;
        this.maxDepth = maxDepth;
    }
    /**
     * when a class implements Runnable is used to create a thread
     * thread.start() calls run automatically 
     * this method do the following things:
     *      retrieve a pair from pool
     *      retrieve the web page by URL
     *      search the page for URL
     *      add new URL pair to pool
     */
    @Override
    public void run()
    {
        //loop until there are no more pairs in the pool
        //while(!pool.getPending().isEmpty())
        while(true)    
        {
            //retrieve a URL_Depth pair from pool
            URLDepthPair nextURL = pool.get();
            if(nextURL == null)
                break;         
            request(nextURL.getURL());
            ArrayList<String> lineContents = Response();
            ArrayList<String> linksInPage = findURLInPage(lineContents);
            addValidURLToList(linksInPage, nextURL.getDepth()+1);
            //Following prints only serve as a tool during debug.
            System.out.println("depth: " + nextURL.getDepth() 
                    + " URL: " + nextURL.getURL());
            System.out.println(" #DoneTh: " + pool.getNumWaitingThreads() 
                    + " pending:  " + pool.getPending().size() 
                    + " all:  " + pool.getSeen().size() 
                    + " proceeded: " + (pool.getSeen().size() - pool.getPending().size()));
        }
        
    }
    /**
     * send request to server through socket
     * @param url the URL which contains server address
     */
    public void request(String url)
    {
        //if the URL is not valid, ignore it and return;
        boolean isValidURL = URLDepthPair.isValidURL(url);
        if(!isValidURL)
            return;
        try
        {
            sock = new Socket(URLDepthPair.getHostFromURL(url),port);
            //if no data received within 3 seconds, a SocketTimeOutException will be raised 
            sock.setSoTimeout(5000);
            //outputstream works with byte
            //wrap it in a printwriter, which deals with chars
            OutputStream os = sock.getOutputStream();
            //true indicates autoflush - println will autoflush buffer, 
            //any data in buffer immediately be written to their destination
            PrintWriter pwriter = new PrintWriter(os,true);
            //send request to server
            pwriter.println("GET " + URLDepthPair.getDocPathFromURL(url) + " HTTP/1.1");
            pwriter.println("Host: " + URLDepthPair.getHostFromURL(url));
            pwriter.println("Connection: close");
            pwriter.println();
        }
        catch(UnknownHostException e)
        {
            System.err.println("Unknown host");
        }
        catch(ConnectException e)
        {
            System.err.println("Connection refused");
        }
        catch(IOException e)
        {
            System.err.println("Other Connection Exception: " + e.getMessage());
        }
        
    }
    
    /**
     * get the response from the server, using the same socket instance in request
     * read contents by lines
     * @return contents of the page, by lines 
     */
    public ArrayList<String> Response()
    {
        //this is where to store the html contents by lines of the requesting page 
        ArrayList<String> contentsOfPageByLine = new ArrayList<>();
        try
        {
            //InputStream works with byte
            //wrap InputSteam in a InputStreamReader, which works with chars
            //finally, wrap InputStreamReader in a BufferedReader, which directly reads lines
            if(sock == null)
                return contentsOfPageByLine;
            InputStream is = sock.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            while(true)
            {
                String line = br.readLine();
                //if encountering the last line
                if(line==null)
                {
                    break;
                }
                //add this line of text into collection
                contentsOfPageByLine.add(line);
            }
            //close socket when every interaction is done
            sock.close();
        }
        catch(SocketException e)
        {
            System.err.println("Socket closed due to refuse to connect");
        }
        catch(SocketTimeoutException e)
        {
            System.err.println("Time out.");
        }
        
        catch(IOException e)
        {
            e.printStackTrace();
        }
        //for now, this block is empty
        //but if any exceptions are possibly missing from being catched, move return statement here
        finally
        {
            
        }
        return contentsOfPageByLine;
    }
    
    /**
     * this method detects and return all URLs found in a string which indicates a LINE of HTML codes
     * it is called in find all URLS in a page method
     * @param line representing a line of HTML codes
     * @return all URL found in that line
     */       
    public ArrayList<String> findURLInString(String line)
    {
        ArrayList<String> urlAllInLine = new ArrayList<>();
        String urlCurrent = "";
        line = line.toLowerCase();
        //search line beginning from char of index
        //index moves forward as urls are found so that identical urls are not counted twice
        int index = 0; 
        //beginning index of url
        int urlBeginAt = 0;
        //ending index of url
        int urlEndAt = 0;
        while(true)
        {
            //index move to the beginning of next "a href="\""
            index = line.indexOf(HTML_LINK_PREFIX,index);
            //if there is no url, quit loop
            if(index == -1)
                break;
            //url begins after the "a href=\""
            urlBeginAt = index + HTML_LINK_PREFIX.length();
            //url ends before the next "\"" is found
            urlEndAt = urlBeginAt + line.substring(index + HTML_LINK_PREFIX.length()).indexOf("\"");
            //url is a substring with beginning and ending index
            urlCurrent = line.substring(urlBeginAt, urlEndAt);
            //add found url to collection
            urlAllInLine.add(urlCurrent);
            //index move forward to the end of a found url
            index = index + HTML_LINK_PREFIX.length() + urlCurrent.length();
        }
        return urlAllInLine;
    }
    
    /**
     * this method parse contents of a page line by line
     * find all URLs and return them as a collection
     * @param contents indicates the contents of an entire page returned by response()
     * @return all URL in the page as a collection
     */
    public ArrayList<String> findURLInPage(ArrayList<String> contents)
    {
        ArrayList<String> urlInPage = new ArrayList<>();
        //iterate through the lines
        Iterator<String> ite = contents.iterator();
        while(ite.hasNext())
        {
            //if URL are found in a line, add it into the collection
            ArrayList<String> urlInLine = findURLInString(ite.next());
            urlInPage.addAll(urlInLine);
        }
        //return the collection that includes all URL in the page
        return urlInPage;
    }
    
    /**
     * this method check valid of all URLs found in a page
     * create URL pair based on validation
     * then call pool add on each of these pairs
     * @param urlInPage the collection of all URL found in that page
     * @param depth URL depth of current page
     */
    public void addValidURLToList(ArrayList<String> urlInPage, int depth)
    {
        //loop through all URLS found in the page
        for(String url : urlInPage)
        {
            //check if a URL is valid
            if(URLDepthPair.isValidURL(url))
            {
                //create a URLDepthPair, call pool's add method
                URLDepthPair pair = new URLDepthPair(url, depth);
                
                pool.add(pair, maxDepth);
            }
        }
    }
    
}
