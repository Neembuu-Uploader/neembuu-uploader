/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.httpclient.httprequest;

import java.net.URI;

/**
 * Custom HttpPost.
 * It is like <a href="https://svn.apache.org/repos/asf/httpcomponents/httpclient/tags/4.2.3/httpclient/src/main/java/org/apache/http/client/methods/HttpPost.java">this</a>.
 * @author davidepastore
 */
public class NUHttpPost extends NUHttpEntityEnclosingRequestBase{
    
    public final static String METHOD_NAME = "POST";

    public NUHttpPost() {
        super();
    }

    public NUHttpPost(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public NUHttpPost(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
    
}
