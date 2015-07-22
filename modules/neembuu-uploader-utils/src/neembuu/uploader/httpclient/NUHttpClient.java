/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.httpclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import neembuu.uploader.exceptions.proxy.NUProxyException;
import neembuu.uploader.exceptions.proxy.NUProxyHostException;
import neembuu.uploader.exceptions.proxy.NUProxyPortException;
import neembuu.uploader.utils.IntegerUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;

/**
 * I'm working to use this everywhere (every connection).
 * This is a singleton class for the HttpClient.
 * With this you can handle proxy.
 * @author davidepastore
 */
public class NUHttpClient {
       
    private static DefaultHttpClient httpClient = null;
    
    /* Not instantiable */
    private NUHttpClient() {}
    
    /**
     * Return the httpclient.
     * @return the httpclient
     */
    public static HttpClient getHttpClient(){
        synchronized(HttpClient.class){
            if (httpClient == null) {
                httpClient = newHttpClient();
            }
        }
        return httpClient;
    }
        
    public static DefaultHttpClient newHttpClient(){
        //SSL http://javaskeleton.blogspot.it/2010/07/avoiding-peer-not-authenticated-with.html
        DefaultHttpClient httpClient_loc = null;
        SSLContext ctx;
        try {
            ctx = SSLContext.getInstance("TLS");

        X509TrustManager tm = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
            return null;
            }
        };
        ctx.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory ssf = new SSLSocketFactory(ctx);
        //ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        //Register schemes
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, ssf));
        ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry); // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e639


        httpClient_loc = new DefaultHttpClient(cm);
        httpClient_loc.getParams()
            .setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH) //CookiePolicy
            .setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true); //Circular Redirect

        //Set redirect strategy
        httpClient_loc.setRedirectStrategy(new CustomRedirectStrategy());

        } catch (KeyManagementException ex) {
            Logger.getLogger(NUHttpClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(NUHttpClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return httpClient_loc;
    }
    
    /**
     * Set the proxy.
     * @param hostname the hostname (IP or DNS name)
     * @param port the port number. -1 indicates the scheme default port.
     */
    public static void setProxy(String hostname, String port) throws NUProxyException, NUProxyPortException, NUProxyHostException{
        //Control the hostname
        checkProxyHost(hostname);
        
        //Control the port
        checkProxyPort(port);
        
        checkProxy(hostname, port);
        
        HttpHost proxy = new HttpHost(hostname, Integer.parseInt(port));
        httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }
    
    /**
     * Set the proxy.
     * @param hostname the hostname (IP or DNS name)
     * @param port the port number. -1 indicates the scheme default port.
     * @param scheme the name of the scheme. null indicates the default scheme
     */
    public static void setProxy(String hostname, String port, String scheme) throws NUProxyException, NUProxyPortException, NUProxyHostException{
        //Control the hostname
        checkProxyHost(hostname);
        
        //Control the port
        checkProxyPort(port);
        
        checkProxy(hostname, port);
        
        HttpHost proxy = new HttpHost(hostname, Integer.parseInt(port), scheme);
        httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }
    
    /**
     * Use default proxy of system.
     */
    public static void resetProxy(){
        httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, null);
    }   
    
    /**
     * Delete all the cookie given the domain domain.
     * @param domain the domain name.
     */
    public static void deleteCookies(String domain) {
        CookieStore cookieStore = httpClient.getCookieStore();
        CookieStore newCookieStore = new BasicCookieStore();
        List<Cookie> cookies = cookieStore.getCookies();
        for(int i = 0; i < cookies.size(); i++){
            if(!cookies.get(i).getDomain().contains(domain)){
                newCookieStore.addCookie(cookies.get(i));
            }
        }
        
        //Set the new cookie store
        httpClient.setCookieStore(newCookieStore);
    }
    
    
    /**
     * Check if a proxy is good.
     */
    private static void checkProxy(String hostname, String port) throws NUProxyException{
        try {
            //Crate the URI
            URIBuilder builder = new URIBuilder();
            builder.setHost(hostname)
                   .setScheme("http")
                   .setPort(Integer.parseInt(port));
            
            //Build the URI
            URI uri = builder.build();
            
            Logger.getLogger(NUHttpClient.class.getName()).log(Level.INFO, uri.toString());
            
            //Set the request
            HttpGet httpGet = new HttpGet(uri);
            HttpResponse httpResponse = httpClient.execute(httpGet);
        } catch (URISyntaxException ex) {
            Logger.getLogger(NUHttpClient.class.getName()).log(Level.SEVERE, null, ex);
            HttpHost proxy = (HttpHost) httpClient.getParams().getParameter(ConnRoutePNames.DEFAULT_PROXY);
            throw new NUProxyException(proxy.getHostName(), proxy.getPort());
        } catch (IOException ex) {
            Logger.getLogger(NUHttpClient.class.getName()).log(Level.SEVERE, null, ex);
            HttpHost proxy = (HttpHost) httpClient.getParams().getParameter(ConnRoutePNames.DEFAULT_PROXY);
            throw new NUProxyException(proxy.getHostName(), proxy.getPort());
        }
    }
    
    /**
     * Check the proxy host.
     * @param host the host.
     */
    private static void checkProxyHost(String host) throws NUProxyHostException{
        if("".equals(host)){
            throw new NUProxyHostException(host);
        }
    }
    
    /**
     * Check the proxy port.
     * @param host the port.
     */
    private static void checkProxyPort(String port) throws NUProxyPortException{
        if(!IntegerUtils.isInteger(port)){
            throw new NUProxyPortException(port);
        }
    }
    
}
