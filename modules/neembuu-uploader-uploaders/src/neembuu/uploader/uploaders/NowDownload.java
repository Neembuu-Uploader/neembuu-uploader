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
import neembuu.uploader.accounts.NowDownloadAccount;
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

/**
 *
 * @author MNidhal
 */
@SmallModule(
    exports={NowDownload.class,NowDownloadAccount.class},
    interfaces={Uploader.class,Account.class},
    name="NowDownload.ch"
)
public class NowDownload extends AbstractUploader{
    
    NowDownloadAccount nowDownloadAccount = (NowDownloadAccount) getAccountsProvider().getAccount("NowDownload.ch");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String uploadURL;
    private String upload_key;
    private String uid;
    private String upload_server;
    
    private String downloadlink = "";
    private String deletelink = "";

    public NowDownload() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "NowDownload.ch";
        if (nowDownloadAccount.loginsuccessful) {
            host = nowDownloadAccount.username + " | NowDownload.ch";
        }
        maxFileSizeLimit = 2147483648l; //2 GB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://www.nowdownload.ch", httpContext);
        
        upload_key = StringUtils.stringBetweenTwoStrings(responseString, "var upload_key = '", "';");
        uid = StringUtils.stringBetweenTwoStrings(responseString, "var uid = ", ";");
        upload_server = StringUtils.stringBetweenTwoStrings(responseString, "var upload_server = '", "';");
        uploadURL = "http://"+upload_server+"/cgi-bin/upload.cgi";
    }

    @Override
    public void run() {
        try {
            if (nowDownloadAccount.loginsuccessful) {
                httpContext = nowDownloadAccount.getHttpContext();
                maxFileSizeLimit = 2147483648l; //2 GB
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 2147483648l; //2 GB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("upload_key", new StringBody(upload_key));
            mpEntity.addPart("uid", new StringBody(uid));
            mpEntity.addPart("upload_server", new StringBody(upload_server));
            mpEntity.addPart("fileselect", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into NowDownload.ch");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            String up_session = StringUtils.stringBetweenTwoStrings(responseString, "\"session\":\"", "\"}");          
            String vid_url = "http://" + upload_server + "/upload.php?s=nowdownload&dd=nowdownload.ch&upload_key=" + upload_key + "&uid=" + uid + "&session_id="+up_session;
            responseString = NUHttpClientUtils.getData(vid_url, httpContext);
            
            //Read the links
            gettingLink();
            downloadlink = "http://www.nowdownload.ch/dl/"+StringUtils.stringBetweenTwoStrings(responseString, "video_id\":\"", "\"}");
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
    
}
