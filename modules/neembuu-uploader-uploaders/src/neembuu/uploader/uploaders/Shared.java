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
import neembuu.uploader.accounts.SharedAccount;
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
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={Shared.class,SharedAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Shared.com"
)
public class Shared extends AbstractUploader implements UploaderAccountNecessary{
    
    SharedAccount sharedAccount = (SharedAccount) getAccountsProvider().getAccount("Shared.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String userType;
    private String authenticity_token = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public Shared() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Shared.com";
        if (sharedAccount.loginsuccessful) {
            host = sharedAccount.username + " | Shared.com";
        }
        maxFileSizeLimit = 1073741824L; // 1 GB (default)
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData(sharedAccount.member_upload_url, httpContext);
        doc = Jsoup.parse(responseString);
        authenticity_token = StringUtils.stringBetweenTwoStrings(responseString, "name=\"authenticity_token\" type=\"hidden\" value=\"", "\"");
    }

    @Override
    public void run() {
        try {
            if (sharedAccount.loginsuccessful) {
                userType = "reg";
                httpContext = sharedAccount.getHttpContext();
                //sessionID = CookieUtils.getCookieValue(httpContext, "xfss");
                maxFileSizeLimit = 1073741824L; // 1 GB
            }
            else {
                host = "Shared.com";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            httpPost = new NUHttpPost("http://shared.com/upload/process");
            httpPost.setHeader("Host", "shared.com");
            httpPost.setHeader("Referer", sharedAccount.member_upload_url);
            httpPost.setHeader("Accept", "application/json, text/javascript, */*;");
            httpPost.setHeader("X-CSRF-TOKEN", authenticity_token);
            httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
            
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("files[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into Shared.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            if (responseString.isEmpty()){
                uploadFailed();
            }
            
            //Read the links
            gettingLink();
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
