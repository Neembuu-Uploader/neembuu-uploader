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
import neembuu.uploader.accounts.IguanaShareAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
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
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={IguanaShare.class,IguanaShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="IguanaShare.com"
)
public class IguanaShare extends AbstractUploader implements UploaderAccountNecessary{
    
    IguanaShareAccount iguanaShareAccount = (IguanaShareAccount) getAccountsProvider().getAccount("IguanaShare.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    private String cTracker = "";
    private final String maxChunkSize = "2092152";
    
    private String downloadlink = "";
    private String deletelink = "";

    public IguanaShare() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "IguanaShare.com";
        if (iguanaShareAccount.loginsuccessful) {
            host = iguanaShareAccount.username + " | IguanaShare.com";
        }
        maxFileSizeLimit = 32212254720L; // 30 GB (default)
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://www.iguanashare.com/account_home.html", httpContext);

        uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "url: '", "'");
        sessionID = StringUtils.stringBetweenTwoStrings(responseString, "_sessionid: '", "'");
        cTracker = StringUtils.stringBetweenTwoStrings(responseString, "cTracker: '", "'");
    }

    @Override
    public void run() {
        try {
            if (iguanaShareAccount.loginsuccessful) {
                userType = "reg";
                httpContext = iguanaShareAccount.getHttpContext();
                maxFileSizeLimit = 32212254720L; // 30 GB
            } else {
                host = "IguanaShare.com";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("cTracker", new StringBody(cTracker));
            mpEntity.addPart("_sessionid", new StringBody(sessionID));
            mpEntity.addPart("maxChunkSize", new StringBody(maxChunkSize));
            mpEntity.addPart("folderId", new StringBody(""));
            mpEntity.addPart("files[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into IguanaShare.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            responseString = responseString.replaceAll("\\\\", "");
            
            //Read the links
            gettingLink();

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
