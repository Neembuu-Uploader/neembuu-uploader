/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.nio.charset.Charset;
import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.TopUploadOneAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
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
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={TopUploadOne.class,TopUploadOneAccount.class},
    interfaces={Uploader.class,Account.class},
    name="TopUpload1.com"
)
public class TopUploadOne extends AbstractUploader{
    
    TopUploadOneAccount topUploadOneAccount = (TopUploadOneAccount) getAccountsProvider().getAccount("TopUpload1.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String uploadURL;
    private String sess_id = "";
    private String cTracker = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public TopUploadOne() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "TopUpload1.com";
        if (topUploadOneAccount.loginsuccessful) {
            host = topUploadOneAccount.username + " | TopUpload1.com";
        }
        maxFileSizeLimit = 104857600; // 100 MB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://topupload1.com/", httpContext);

        uploadURL = responseString;
        uploadURL = StringUtils.stringStartingFromString(uploadURL, "sequentialUploads: true");
        uploadURL = StringUtils.stringUntilString(uploadURL, "maxFileSize: uploaderMaxSize");
        uploadURL = StringUtils.stringBetweenTwoStrings(uploadURL, "'", "'");
        
        sess_id = StringUtils.stringBetweenTwoStrings(responseString, "_sessionid: '", "'");
        cTracker = StringUtils.stringBetweenTwoStrings(responseString, "cTracker: '", "'");
    }

    @Override
    public void run() {
        try {
            if (topUploadOneAccount.loginsuccessful) {
                // registered user
                httpContext = topUploadOneAccount.getHttpContext();
                maxFileSizeLimit = 7516192768L; // 7 GB
            } else {
                // anonymous user
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 104857600; // 100 MB
            }
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("_sessionid", new StringBody(sess_id));
            mpEntity.addPart("cTracker", new StringBody(cTracker));
            mpEntity.addPart("maxChunkSize", new StringBody("8383608"));
            mpEntity.addPart("folderId", new StringBody(""));
            mpEntity.addPart("files[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into TopUpload1.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            responseString = responseString.replaceAll("\\\\", "");
            downloadlink = StringUtils.stringBetweenTwoStrings(responseString, "\"url\":\"", "\"");
            deletelink = StringUtils.stringBetweenTwoStrings(responseString, "\"delete_url\":\"", "\"");
                
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
