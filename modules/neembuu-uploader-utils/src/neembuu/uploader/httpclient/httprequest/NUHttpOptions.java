/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.httpclient.httprequest;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;

/**
 * Custom HttpOptions.
 * It is like <a href="https://svn.apache.org/repos/asf/httpcomponents/httpclient/tags/4.2.3/httpclient/src/main/java/org/apache/http/client/methods/HttpOptions.java">this</a>.
 * @author davidepastore
 */
public class NUHttpOptions extends NUHttpRequestBase{
    
    /**
     * Method name.
     */
    public final static String METHOD_NAME = "OPTIONS";

    public NUHttpOptions() {
        super();
    }

    public NUHttpOptions(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public NUHttpOptions(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

    public Set<String> getAllowedMethods(final HttpResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }

        HeaderIterator it = response.headerIterator("Allow");
        Set<String> methods = new HashSet<String>();
        while (it.hasNext()) {
            Header header = it.nextHeader();
            HeaderElement[] elements = header.getElements();
            for (HeaderElement element : elements) {
                methods.add(element.getName());
            }
        }
        return methods;
    }
    
}
