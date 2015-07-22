/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.httpclient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * Custom redirect handler. It is like <a href="https://svn.apache.org/repos/asf/httpcomponents/httpclient/tags/4.2.5/httpclient/src/main/java/org/apache/http/impl/client/DefaultRedirectStrategy.java">this</a>.
 * The edited methods are: createLocationURI(final String location)
 * @author davidepastore
 */
public class CustomRedirectStrategy implements RedirectStrategy{

    public static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";

    /**
     * Redirectable methods.
     */
    private static final String[] REDIRECT_METHODS = new String[] {
        HttpGet.METHOD_NAME,
        HttpHead.METHOD_NAME
    };

    public CustomRedirectStrategy() {
        super();
    }

    @Override
    public boolean isRedirected(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws ProtocolException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }

        int statusCode = response.getStatusLine().getStatusCode();
        String method = request.getRequestLine().getMethod();
        Header locationHeader = response.getFirstHeader("location");
        switch (statusCode) {
        case HttpStatus.SC_MOVED_TEMPORARILY:
            return isRedirectable(method) && locationHeader != null;
        case HttpStatus.SC_MOVED_PERMANENTLY:
        case HttpStatus.SC_TEMPORARY_REDIRECT:
            return isRedirectable(method);
        case HttpStatus.SC_SEE_OTHER:
            return true;
        default:
            return false;
        } //end of switch
    }

    public URI getLocationURI(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws ProtocolException, URISyntaxException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
        //get the location header to find out where to redirect to
        Header locationHeader = response.getFirstHeader("location");
        if (locationHeader == null) {
            // got a redirect response, but no location header
            throw new ProtocolException(
                    "Received redirect response " + response.getStatusLine()
                    + " but no location header");
        }
        String location = locationHeader.getValue();

        URI uri = createLocationURI(location);

        HttpParams params = request.getParams();
        // rfc2616 demands the location value be a complete URI
        // Location       = "Location" ":" absoluteURI
        try {
            // Drop fragment
            uri = URIUtils.rewriteURI(uri);
            if (!uri.isAbsolute()) {
                if (params.isParameterTrue(ClientPNames.REJECT_RELATIVE_REDIRECT)) {
                    throw new ProtocolException("Relative redirect location '"
                            + uri + "' not allowed");
                }
                // Adjust location URI
                HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if (target == null) {
                    throw new IllegalStateException("Target host not available " +
                            "in the HTTP context");
                }
                URI requestURI = new URI(request.getRequestLine().getUri());
                URI absoluteRequestURI = URIUtils.rewriteURI(requestURI, target, true);
                uri = URIUtils.resolve(absoluteRequestURI, uri);
            }
        } catch (URISyntaxException ex) {
            throw new ProtocolException(ex.getMessage(), ex);
        }

        RedirectLocations redirectLocations = (RedirectLocations) context.getAttribute(
                REDIRECT_LOCATIONS);
        if (redirectLocations == null) {
            redirectLocations = new RedirectLocations();
            context.setAttribute(REDIRECT_LOCATIONS, redirectLocations);
        }
        if (params.isParameterFalse(ClientPNames.ALLOW_CIRCULAR_REDIRECTS)) {
            if (redirectLocations.contains(uri)) {
                throw new CircularRedirectException("Circular redirect to '" + uri + "'");
            }
        }
        redirectLocations.add(uri);
        return uri;
    }

    /**
     * @since 4.1
     */
    protected URI createLocationURI(final String location) throws ProtocolException {
        try {
            URI uri;
            //NULogger.getLogger().log(Level.INFO, "location: {0}", location);
            
            //If it contains a special char we create our custom uri (it fails otherwise)
            if(location.contains("|")){
                uri = neembuu.uploader.utils.URIUtils.createURI(location);
            }
            else{
                uri = new URI(location);
            }
            return uri.normalize();
        } catch (URISyntaxException ex) {
            throw new ProtocolException("Invalid redirect URI: " + location, ex);
        }
    }

    /**
     * @since 4.2
     */
    protected boolean isRedirectable(final String method) {
        for (String m: REDIRECT_METHODS) {
            if (m.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public HttpUriRequest getRedirect(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws ProtocolException {
        URI uri = null;
        try {
            uri = getLocationURI(request, response, context);
        } catch (URISyntaxException ex) {
            Logger.getLogger(CustomRedirectStrategy.class.getName()).log(Level.SEVERE, null, ex);
        }
        String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
            return new HttpHead(uri);
        } else if (method.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
            return new HttpGet(uri);
        } else {
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_TEMPORARY_REDIRECT) {
                if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
                    return copyEntity(new HttpPost(uri), request);
                } else if (method.equalsIgnoreCase(HttpPut.METHOD_NAME)) {
                    return copyEntity(new HttpPut(uri), request);
                } else if (method.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
                    return new HttpDelete(uri);
                } else if (method.equalsIgnoreCase(HttpTrace.METHOD_NAME)) {
                    return new HttpTrace(uri);
                } else if (method.equalsIgnoreCase(HttpOptions.METHOD_NAME)) {
                    return new HttpOptions(uri);
                } else if (method.equalsIgnoreCase(HttpPatch.METHOD_NAME)) {
                    return copyEntity(new HttpPatch(uri), request);
                }
            }
            return new HttpGet(uri);
        }
    }

    private HttpUriRequest copyEntity(
            final HttpEntityEnclosingRequestBase redirect, final HttpRequest original) {
        if (original instanceof HttpEntityEnclosingRequest) {
            redirect.setEntity(((HttpEntityEnclosingRequest) original).getEntity());
        }
        return redirect;
    }
    
    
    
}
