/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import neembuu.uploader.accounts.BayFilesAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author Dinesh
 * @author davidepastore
 */
@SmallModule(
    exports={BayFiles.class,BayFilesAccount.class},
    interfaces={Uploader.class,Account.class},
    name="BayFiles.com",
    ignore = true
)
public class BayFiles extends AbstractUploader {

    BayFilesAccount bayFilesAccount = (BayFilesAccount) getAccountsProvider().getAccount("BayFiles.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private CookieStore cookieStore;
    private String postURL;
    private String uploadresponse;
    private String downloadlink;
    private String deletelink;
    
    private JSONObject jSonOjbject;

    public BayFiles() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "BayFiles.com";

        if (bayFilesAccount.loginsuccessful) {
            host = bayFilesAccount.username + " | BayFiles.com";
        }
        maxFileSizeLimit = 5368709120L; //5 GB for all users
    }

    private void initialize() throws Exception {
        NULogger.getLogger().info("Getting upload url from bayfiles.com");
        
        final String ajaxUrl = "http://bayfiles.net/ajax_upload?_=" + System.currentTimeMillis();
        
        uploadresponse = NUHttpClientUtils.getData(ajaxUrl, httpContext);
        
//        NULogger.getLogger().log(Level.INFO, "responsestring : {0}", uploadresponse);
        
        jSonOjbject = new JSONObject(uploadresponse);
        
        postURL = jSonOjbject.getString("upload_url");
        
        /*
        postURL = StringUtils.stringBetweenTwoStrings(responseString, "\"uploadUrl\":\"", "\"");
        postURL = postURL.replaceAll("\\\\", "");
        NULogger.getLogger().log(Level.INFO, "Post URL : {0}", postURL);
        if (postURL.contains("s6.")) {
            NULogger.getLogger().info("Got s6 domain. So re-initializing.....");
            initialize();
        }
        */
    }

    private void fileUpload() throws Exception {

        HttpPost httpPost = new HttpPost(postURL);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("file", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
        uploading();
        NULogger.getLogger().info("Now uploading your file into bayfiles.com");
        HttpResponse response = httpclient.execute(httpPost);
        gettingLink();
        uploadresponse = EntityUtils.toString(response.getEntity());

        NULogger.getLogger().info(response.getStatusLine().toString());

//  
//        NULogger.getLogger().log(Level.INFO, "Upload response : {0}", uploadresponse);
        jSonOjbject = new JSONObject(uploadresponse);
        downloadlink = jSonOjbject.getString("downloadUrl");
        deletelink = jSonOjbject.getString("deleteUrl");
        
        NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
        NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
        downURL = downloadlink;
        delURL = deletelink;

        uploadFinished();
    }

    @Override
    public void run() {
        try {

            if (bayFilesAccount.loginsuccessful) {
                httpContext = bayFilesAccount.getHttpContext();
                host = bayFilesAccount.username + " | BayFiles.com";
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                host = "Bayfiles.com";
            }

            /* Check max file size */
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(this.maxFileSizeLimit, file.getName(), host);
            }

        
            uploadInitialising();
            initialize();
            fileUpload();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            NULogger.getLogger().severe(e.toString());

            uploadFailed();
        } finally {
            postURL = null;
            uploadresponse = null;
            downloadlink = null;
            deletelink = null;
        }
    }
}
