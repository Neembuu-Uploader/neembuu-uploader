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
import neembuu.uploader.accounts.RapidUAccount;
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
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import neembuu.uploader.interfaces.UploaderAccountNecessary;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={RapidU.class,RapidUAccount.class},
    interfaces={Uploader.class,Account.class},
    name="RapidU.net"
)
public class RapidU extends AbstractUploader implements UploaderAccountNecessary{
    
    RapidUAccount rapidUAccount = (RapidUAccount) getAccountsProvider().getAccount("RapidU.net");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private String uploadURL;
    private String sessionID = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public RapidU() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "RapidU.net";
        if (rapidUAccount.loginsuccessful) {
            host = rapidUAccount.username + " | RapidU.net";
        }
        maxFileSizeLimit = 2147483648L; // 2 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("https://rapidu.net/upload/", httpContext);
        
        uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "'uploadScript' : '", "',");
        sessionID = StringUtils.stringBetweenTwoStrings(responseString, "'session' : '", "'");
    }

    @Override
    public void run() {
        try {
            if (rapidUAccount.loginsuccessful) {
                httpContext = rapidUAccount.getHttpContext();
                maxFileSizeLimit = 2147483648L; // 2 GB
            } else {
                host = "RapidU.net";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("files", createMonitoredFileBody());
            mpEntity.addPart("session", new StringBody(sessionID));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into RapidU.net");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            
            if (responseString.contains("error")){
                throw new Exception("Download link not found! Upload Failed!");
            } else {
                downloadlink = responseString;
            }
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
