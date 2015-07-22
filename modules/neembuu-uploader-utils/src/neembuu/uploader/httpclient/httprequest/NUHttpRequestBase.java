/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.httpclient.httprequest;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;

/**
 * Basic implementation of an HTTP request that can be modified.
 * It adds the headers.
 * @author davidepastore
 */
public abstract class NUHttpRequestBase extends HttpRequestBase{
    
    public NUHttpRequestBase(){
        super();
        this.setHeaders();
    }
    
    /**
     * Set the headers for the request.
     */
    private void setHeaders(){
        headergroup.addHeader(new BasicHeader("Connection", "Keep-Alive"));
        headergroup.addHeader(new BasicHeader("Cache-control", "no-cache"));
        headergroup.addHeader(new BasicHeader("Pragma", "no-cache"));
        headergroup.addHeader(new BasicHeader("Accept", "text/html, */*"));
        headergroup.addHeader(new BasicHeader("Accept-Charset", "iso-8859-1, utf-8, utf-16"));
        headergroup.addHeader(new BasicHeader("Accept-Charset", "deflate, gzip, identity"));
        headergroup.addHeader(new BasicHeader("Accept-Language", "en"));
        //Here you can change the user-agent : we can create a dynamic method to change the Browser header.
        headergroup.addHeader(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:20.0) Gecko/20100101 Firefox/20.0"));
    }
    
}
