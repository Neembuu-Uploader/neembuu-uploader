/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.FilesFlashAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={FilesFlash.class,FilesFlashAccount.class},
    interfaces={Uploader.class,Account.class},
    name="FilesFlash.com"
)
public class FilesFlash extends AbstractUploader{
    
    FilesFlashAccount filesFlashAccount = (FilesFlashAccount) getAccountsProvider().getAccount("FilesFlash.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public FilesFlash() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "FilesFlash.com";
        if (filesFlashAccount.loginsuccessful) {
            host = filesFlashAccount.username + " | FilesFlash.com";
        }
        maxFileSizeLimit = 1073741824; //1 GB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://niger.filesflash.com/uploadform.php", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.getElementById("uploadform").attr("action");
        uploadURL += "?X-Progress-ID=" + uuid();
    }

    @Override
    public void run() {
        try {
            if (filesFlashAccount.loginsuccessful) {
                userType = "reg";
                httpContext = filesFlashAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "userid");
                maxFileSizeLimit = 1073741824; //1 GB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 1073741824; //1 GB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("refcode", new StringBody(sessionID));
            mpEntity.addPart("uploadfile", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into FilesFlash.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            
            //Read the links
            gettingLink();
            doc = Jsoup.parse(responseString);
            downloadlink = doc.select("input").eq(2).val();
            deletelink = doc.select("input").eq(3).val();
            
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
    
    
    /**
     * Generate a uuid.
     * @return Returns a uuid.
     */
    private String uuid(){
        String uuid = "";
        for (int i = 0; i < 32; i++){
            uuid += Integer.toString((int) (Math.random() * 16), 16);
        } 
        return uuid;
    }
    
}
