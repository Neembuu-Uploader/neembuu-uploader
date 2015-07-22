/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.UpStoreAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import neembuu.uploader.uploaders.common.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={UpStore.class,UpStoreAccount.class},
    interfaces={Uploader.class,Account.class},
    name="UpStore.net"
)
public class UpStore extends AbstractUploader{
    
    UpStoreAccount upStoreAccount = (UpStoreAccount) getAccountsProvider().getAccount("UpStore.net");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    private String upload_post_url = "";
    private String upload_crossdomain_url = "";

    private int i;
    
    private String downloadlink = "";
    private String deletelink = "";

    public UpStore() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "UpStore.net";
        if (upStoreAccount.loginsuccessful) {
            host = upStoreAccount.username + " | UpStore.net";
        }
        maxFileSizeLimit = 1073741824L; // 1 GB (default)
        
    }

    private void initialize() throws Exception {
        while (upload_post_url.isEmpty()){
            responseString = NUHttpClientUtils.getData("http://upstore.net", httpContext);
            upload_post_url = StringUtils.stringBetweenTwoStrings(responseString, "script     : '", "'");
            
            i++;
            if (i==3) {
                break;
            }
        }
        
        upload_crossdomain_url = upload_post_url;
        upload_crossdomain_url = StringUtils.removeLastChars(upload_post_url, 7) + "crossdomain.xml";
    }

    @Override
    public void run() {
        try {
            if (upStoreAccount.loginsuccessful) {
                userType = "reg";
                httpContext = upStoreAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "usid");
                maxFileSizeLimit = 2147483648L; // 2 GB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 1073741824L; // 1 GB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            responseString = NUHttpClientUtils.getData(upload_crossdomain_url, httpContext);
            
            httpPost = new NUHttpPost(upload_post_url);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("Filename", new StringBody(file.getName()));
            mpEntity.addPart("folder", new StringBody("/"));
            mpEntity.addPart("fileext", new StringBody("*.*"));
            if (userType.equals("reg")) {
                mpEntity.addPart("usid", new StringBody(sessionID));
            }
            mpEntity.addPart("file", createMonitoredFileBody());
            mpEntity.addPart("Upload", new StringBody("Submit Query"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into UpStore.net");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            gettingLink();
            
            if (responseString.isEmpty()) {
                uploadFailed();
            }
            
            downloadlink = "http://upstore.net/" + StringUtils.stringBetweenTwoStrings(responseString, "\"hash\":\"", "\"") + "/";
            deletelink = "NA";
            
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
    
}
