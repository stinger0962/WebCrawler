/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nio
 */
public class URLDepthPair {
    private String url;
    private int depth;
    public static final String URL_PREFIX = "http://";
    public URLDepthPair(String url, int depth)
    {
        this.url = url;
        this.depth = depth;
    }
    //accessor for private fields, mutators are not provided, thus prohibited
    public String getURL()
    {
        return url;
    }
    public int getDepth()
    {
        return depth;
    }
    
    //validation of the URL, if it doesn't start with "http://", simply ignore it for now
    public static boolean isValidURL(String url)
    {
        if(url.startsWith(URL_PREFIX))
            return true;
        else
            return false;
    }
    //get the host of a url starting with "http://", that is, a valid URL
    //e.g. "http://www.testurl.com/doc1"
    //host: "http://www.testurl.com"
    public static String getHostFromURL(String url)
    { 
        int indexOfSlash = 0;
        url = url.toLowerCase();
        //return the substring before the first "/"
        //if it doesn't contain "/" return the entire URL
        url = url.substring(URL_PREFIX.length());
        indexOfSlash = url.indexOf("/");
        if(!(indexOfSlash == 0))
            return url.substring(0,indexOfSlash);
        else
            return url;
    }
    //get the document path of a valid URL
    //e.g. "http://www.testurl.com/doc1"
    //docPath: "/doc1"
    public static String getDocPathFromURL(String url)
    {
        int indexOfSlash = 0;
        url = url.toLowerCase(); 
        //return the substring of URL, starting from "/"
        //if URL doesn't contain "/", simply return "/"
        url = url.substring(URL_PREFIX.length());
        indexOfSlash = url.indexOf("/");
        if(!(indexOfSlash == 0))
            return url.substring(indexOfSlash); 
        else
            return "/";
    }
}
