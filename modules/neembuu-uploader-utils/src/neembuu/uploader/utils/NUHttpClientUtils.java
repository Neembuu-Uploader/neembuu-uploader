/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * Utils for common tasks.
 * @author davidepastore
 */
public class NUHttpClientUtils {
    
    /**
     * Get the content of a page.
     * @param url url from which to read
     * @return the String content of the page
     * @throws Exception 
     */
    public static String getData(String url) throws Exception {
        NUHttpGet httpGet = new NUHttpGet(url);
        HttpResponse httpResponse = NUHttpClient.getHttpClient().execute(httpGet);
        return EntityUtils.toString(httpResponse.getEntity());
    }
    
    /**
     * Get the content of a page.
     * @param url url from which to read
     * @param httpContext the httpContext in which to make the request
     * @return the String content of the page
     * @throws Exception 
     */
    public static String getData(String url, HttpContext httpContext) throws Exception {
        NUHttpGet httpGet = new NUHttpGet(url);
        HttpResponse httpResponse = NUHttpClient.getHttpClient().execute(httpGet, httpContext);
        return EntityUtils.toString(httpResponse.getEntity());
    }
    
    
    /**
     * Read the content of a page. It uses EntityUtils.consumeQuietly().
     * @param url url from which to read
     * @param httpContext the httpContext in which to make the request
     */
    public static void getDataQuietly(String url, HttpContext httpContext) {
        try {
            NUHttpGet httpGet = new NUHttpGet(url);
            HttpResponse httpResponse = NUHttpClient.getHttpClient().execute(httpGet, httpContext);
            EntityUtils.consumeQuietly(httpResponse.getEntity());
        } catch (Exception ex) {
            Logger.getLogger(NUHttpClientUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
