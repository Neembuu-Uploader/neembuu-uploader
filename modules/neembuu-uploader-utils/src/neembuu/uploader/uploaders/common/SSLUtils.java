/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders.common;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class help you for SSL connections.
 * @author davidepastore
 */
public class SSLUtils {
    
    /**
     * This static method disable the Validation Certificate. Use it for
     * <b>HttpURLConnection</b>.
     */
    public static void disableCertificateValidation() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { 
          new X509TrustManager() {
              @Override
            public X509Certificate[] getAcceptedIssuers() { 
              return new X509Certificate[0]; 
            }
              @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
              @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }};

        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hv = new HostnameVerifier() {
            @Override
          public boolean verify(String hostname, SSLSession session) { return true; }
        };

        // Install the all-trusting trust manager
        try {
          SSLContext sc = SSLContext.getInstance("SSL");
          sc.init(null, trustAllCerts, new SecureRandom());
          HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
          HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (Exception e) {}
    }
}
