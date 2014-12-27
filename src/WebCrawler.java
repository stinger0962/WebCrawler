/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.*; //to use collections
import java.io.*; //to use streams
import java.net.*; //to use sockets

/**
 *implement the main functionality of the application
 * this is where socket is created, request is sent, and response is 
 * @author Nio
 */
public class WebCrawler {
    private String url;
    private int depth;
    private Socket sock; // socket instance need to be used in both request and response functions
    //make port a constant since HTTP listens to port 80
    private static final int port = 80;
    //use two LinkedList to store URLDepthPairs
    private static LinkedList<URLDepthPair> urlAllVisited = new LinkedList<>();
    private static LinkedList<URLDepthPair> urlUnprocessed = new LinkedList<>();
    
    //accessors for two LinkedLists
    public LinkedList<URLDepthPair> getUnprocessedURLDepthPairs()
    {
        return this.urlUnprocessed;
    }
    public LinkedList<URLDepthPair> getAllURLDepthPairs()
    {
        return this.urlAllVisited;
    }
    //remove a URL that is processing from unprocessed list, return true if list is changed 
    public boolean removeFromUnprocessedList(URLDepthPair processingURL)
    {
        return urlUnprocessed.remove(processingURL);
    }
    
    public static final String HTML_LINK_PREFIX = "a href=\"";
    //return a list of all URLDepthPair visited to display result
    public WebCrawler(String url, int depth)
    {
        this.depth = depth;
        this.url = url;
    }
    
    
    public void request()
    {
        //if the URL is not valid, ignore it and return;
        boolean isValidURL = URLDepthPair.isValidURL(url);
        if(!isValidURL)
            return;
        try
        {
            sock = new Socket(URLDepthPair.getHostFromURL(url),port);
            //if no data received within 3 seconds, a SocketTimeOutException will be raised 
            sock.setSoTimeout(3000);
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
        catch(SocketException e)
        {
            e.printStackTrace();
        }
        catch(UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch(SocketTimeoutException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
    }
    /**
     * get the response from the server
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
     * this method detects and return all URLs found in a string which indicates a line of HTML codes
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
     * find all URL and return them as a collection
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
     * this method adds all VALID URL into two collections of URLDepthPair
     * one of which is all visited URL, the other unprocessed
     * this method should ONLY be called by the SAME WebCrawler instance which deals with the 1st page.
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
                //create a URLDepthPair, add it into both collections
                URLDepthPair pair = new URLDepthPair(url, depth);
                urlAllVisited.add(pair);
                urlUnprocessed.add(pair);
            }
        }
    }
    
    /**
     * 
     * @param args: take 2 parameters: 
     * first argument indicates initial URL, 
     * second argument indicates the max depth of URL
     * 
     */
    public static void main(String[] args)
    {
        int currentDepth = 0;
        int maxDepth = 0;
        //get mad depth from arguments
        try
        {
            maxDepth = Integer.parseInt(args[1]);
        }
        catch(NumberFormatException nfe)
        {
            //if the first argument is not a valid integer, print error message and exit with an error code.
            System.out.println("The first argument must be an integer.");
            System.exit(1);
        }
        //create the first WebCrawler instance, which is used to maintain collections of URLDepthPair 
        WebCrawler wcInitial = new WebCrawler(args[0], maxDepth);
        //Currently, only 1 URL is passed through argument, this can be developed into starting with multiple URLS later
        ArrayList<String> initialURL = new ArrayList<>();
        initialURL.add(args[0]);
        //add the initial URL into collections
        wcInitial.addValidURLToList(initialURL, currentDepth);
        //repeat web crawling until max depth is reached
        while(currentDepth <= maxDepth)
        {
            currentDepth ++ ;
            //in order to avoid concurrency modification
            //create a copy of current unprocessed URL list
            //then iterate through the copy list, while deleting, and adding new item to original list
            LinkedList<URLDepthPair> copyUnprocessedURL = new LinkedList<>(wcInitial.getUnprocessedURLDepthPairs());
            //iterate through the copy of unprocessed URL list
            Iterator<URLDepthPair> ite = copyUnprocessedURL.iterator();
            while(ite.hasNext())
            {
                //return and remove the first unprocessed URL pair from the copy list
                URLDepthPair nextURL = copyUnprocessedURL.removeFirst();
                //also remove the URL from the original unprocessed list
                boolean isRemovedFromUnprocessedList = wcInitial.removeFromUnprocessedList(nextURL);
                //create USL's own WebCrawler instance to process it
                WebCrawler wcNextURL = new WebCrawler(nextURL.getURL(), nextURL.getDepth());
                wcNextURL.request();
                ArrayList<String> contentsOfNextURL = wcNextURL.Response();
                ArrayList<String> urlFound = wcNextURL.findURLInPage(contentsOfNextURL);
                //add new URL to the original unprocessed list
                wcInitial.addValidURLToList(urlFound, currentDepth);
            }       
        }
        //Currently, it is simple, just printing out all URLs
        for(URLDepthPair pair: wcInitial.getAllURLDepthPairs())
        {
            System.out.println(pair.getURL().toLowerCase());
        }
    }
}
