/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import neembuu.uploader.accounts.OpenLoadAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={OpenLoad.class,OpenLoadAccount.class},
    interfaces={Uploader.class,Account.class},
    name="OpenLoad.co"

)
public class OpenLoad extends AbstractUploader{
    
    OpenLoadAccount openLoadAccount = (OpenLoadAccount) getAccountsProvider().getAccount("OpenLoad.co");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String uploadURL;
	
    private String downloadlink = "";
    private String deletelink = "";

    public OpenLoad() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "OpenLoad.co";

        if (openLoadAccount.loginsuccessful) {
            host = openLoadAccount.username + " | OpenLoad.co";
        }
        maxFileSizeLimit = 5368709120L; // 5120 MB (default)
        setupSsl();
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("https://openload.co/upload", httpContext);
        responseString = NUHttpClientUtils.getData("https://openload.co/getUpload", httpContext);
        uploadURL = responseString;
    }

    @Override
    public void run() {
        try {
            if (openLoadAccount.loginsuccessful) {
                // registered user
                httpContext = openLoadAccount.getHttpContext();
                maxFileSizeLimit = 5368709120L; // 5120 MB
            } else {
                // free/anon user
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 5368709120L; // 5120 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("file1", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into OpenLoad.co");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            responseString = responseString.replaceAll("\\\\", "");
            
            downloadlink = StringUtils.stringBetweenTwoStrings(responseString, "\"url\":\"", "\"");
            deletelink = UploadStatus.NA.getLocaleSpecificString();

            NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            downURL = downloadlink;
            delURL = deletelink;

            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            uploadFailed();
        }
    }
    
    private void setupSsl() {
        SSLSocketFactory sf = null;
        SSLContext sslContext = null;

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (NoSuchAlgorithmException e) {
            NULogger.getLogger().log(Level.SEVERE, "OpenLoad.co -> SSL error", e);
        } catch (KeyManagementException e) {
            NULogger.getLogger().log(Level.SEVERE, "OpenLoad.co -> SSL error", e);
        }

        try {
            sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            
        } catch (Exception e) {
            NULogger.getLogger().log(Level.SEVERE, "OpenLoad.co -> SSL error", e);
        }

        Scheme scheme = new Scheme("https", 443, sf);
        httpclient.getConnectionManager().getSchemeRegistry().register(scheme);
    }
}