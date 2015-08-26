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
import neembuu.uploader.accounts.BillionUploadsAccount;
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
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 * @author Paralytic - plugin re-written
 */
@SmallModule(
    exports={BillionUploads.class,BillionUploadsAccount.class},
    interfaces={Uploader.class,Account.class},
    name="BillionUploads.com",
    ignore = true
)
public class BillionUploads extends AbstractUploader{
    
    BillionUploadsAccount billionUploadsAccount = (BillionUploadsAccount) getAccountsProvider().getAccount("BillionUploads.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String uploadURL;
    private String sessionID = "";
    private String srv_tmp_url = "";
    private Document doc;
    
    private String downloadlink = "";
    private String deletelink = "";

    public BillionUploads() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "BillionUploads.com";
        if (billionUploadsAccount.loginsuccessful) {
            host = billionUploadsAccount.username + " | BillionUploads.com";
        }
        maxFileSizeLimit = 4294967296L; // 4 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://billionuploads.com/upload", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form[name=file]").attr("action");
        uploadURL += StringUtils.uuid(12, 10);
        sessionID = doc.select("form[name=file]").select("input[name=sess_id]").attr("value");
        srv_tmp_url = doc.select("form[name=file]").select("input[name=srv_tmp_url]").attr("value");
    }

    @Override
    public void run() {
        try {
            if (billionUploadsAccount.loginsuccessful) {
                httpContext = billionUploadsAccount.getHttpContext();
                maxFileSizeLimit = 4294967296L; // 4 GB
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 4294967296L; // 4 GB
            }
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            
            mpEntity.addPart("upload_type", new StringBody("file"));
            mpEntity.addPart("sess_id", new StringBody(sessionID));
            mpEntity.addPart("srv_tmp_url", new StringBody(srv_tmp_url));
            mpEntity.addPart("file", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into BillionUploads.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            
            // http://billionuploads.com/?fn=59p850on5bjo&st=OK&op=upload_result
            uploadURL = "http://billionuploads.com/?fn=" +responseString+ "&st=OK&op=upload_result";
            responseString = NUHttpClientUtils.getData(uploadURL, httpContext);
            
            downloadlink = StringUtils.stringBetweenTwoStrings(responseString, "\"link\":\"", "\"");
            deletelink = StringUtils.stringBetweenTwoStrings(responseString, "\"del\":\"", "\"");
            
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
