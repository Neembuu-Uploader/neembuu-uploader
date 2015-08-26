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
import neembuu.uploader.accounts.NitroFlareAccount;
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
    exports={NitroFlare.class,NitroFlareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="NitroFlare.com"
)
public class NitroFlare extends AbstractUploader implements UploaderAccountNecessary{
    
    NitroFlareAccount nitroFlareAccount = (NitroFlareAccount) getAccountsProvider().getAccount("NitroFlare.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private String uploadURL;
    private String nf_user = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public NitroFlare() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "NitroFlare.com";
        if (nitroFlareAccount.loginsuccessful) {
            host = nitroFlareAccount.username + " | NitroFlare.com";
        }
        maxFileSizeLimit = 10737418240L; // 10 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("https://www.nitroflare.com", httpContext);
        uploadURL = "http://www.nitroflare.com/plugins/fileupload/index.php" + StringUtils.stringBetweenTwoStrings(responseString, "plugins/fileupload/index.php", "\"");
        
        responseString = NUHttpClientUtils.getData(uploadURL, httpContext);
        uploadURL = StringUtils.stringUntilString(responseString, "var links = '");
        nf_user = StringUtils.stringBetweenTwoStrings(uploadURL, "user: '", "'");
        
        uploadURL = StringUtils.stringBetweenTwoStrings(uploadURL, "url: \"", "\"");
    }

    @Override
    public void run() {
        try {
            if (nitroFlareAccount.loginsuccessful) {
                httpContext = nitroFlareAccount.getHttpContext();
                maxFileSizeLimit = 10737418240L; // 10 GB
            } else {
                host = "NitroFlare.com";
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
            mpEntity.addPart("user", new StringBody(nf_user));
            mpEntity.addPart("files[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into NitroFlare.com");
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
}
