/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 * Utility for URI.
 * @author davidepastore
 */
public class URIUtils {
    
    /**
     * Create an URI from the given String.
     * @param url the String with the url
     * @return URI instance
     * @throws URISyntaxException 
     */
    public static URI createURI(String url) throws URISyntaxException{
        
        //NULogger.getLogger().log(Level.INFO, "Init url: {0}", url);
        
        //Get scheme
        String scheme = StringUtils.stringUntilString(url, ":");
        //NULogger.getLogger().log(Level.INFO, "scheme: {0}", scheme);
        
        //Get authority
        String authority = StringUtils.stringBetweenTwoStrings(url, "//", "/");
        //NULogger.getLogger().log(Level.INFO, "authority: {0}", authority);
        
        //Get path
        int questionIndex = url.indexOf("?");
        String path = StringUtils.stringBetweenTwoStrings(url, authority, "?");
        //NULogger.getLogger().log(Level.INFO, "path: {0}", path);
        
        //Get query
        String query = url.substring(questionIndex+1);
        //NULogger.getLogger().log(Level.INFO, "query: {0}", query);

        URI uri = new URI(
            scheme, 
            authority, 
            path,
            query,
            null);
        NULogger.getLogger().log(Level.INFO, "result: {0}", uri);
        return uri;
    }
    
}
