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
import neembuu.uploader.accounts.UpLeaAccount;
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
import java.util.Random;
import neembuu.uploader.uploaders.common.StringUtils;


/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={UpLea.class,UpLeaAccount.class},
    interfaces={Uploader.class,Account.class},
    name="UpLea.com"

)
public class UpLea extends AbstractUploader{
    
    UpLeaAccount upLeaAccount = (UpLeaAccount) getAccountsProvider().getAccount("UpLea.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String uploadURL;
    private String userType;
    private String uploadid_s = "";
    private String upToken = "";
	
    private String downloadlink = "";
    private String deletelink = "";

    public UpLea() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "UpLea.com";

        if (upLeaAccount.loginsuccessful) {
            host = upLeaAccount.username + " | UpLea.com";
        }
        maxFileSizeLimit = 2147483648L; // 2048 MB (default)        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://api.uplea.com/api/get-best-node", httpContext);
        
        uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "\"name\":\"", "\"");
        uploadURL = "http://" +uploadURL+ "/";
        upToken = StringUtils.stringBetweenTwoStrings(responseString, "\"token\":\"", "\"");
    }

    @Override
    public void run() {
        try {
            if (upLeaAccount.loginsuccessful) {
                userType = "reg";
                httpContext = upLeaAccount.getHttpContext();
                maxFileSizeLimit = 2684354560L; // 2560 MB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 2147483648L; // 2048 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            long uploadID;
            Random random = new Random();
            uploadID = Math.round(random.nextFloat() * Math.pow(10,12));
            uploadid_s = String.valueOf(uploadID);
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            if (userType.equals("reg")) {
                mpEntity.addPart("api_key", new StringBody(upLeaAccount.api_key));
            }
            mpEntity.addPart("token", new StringBody(upToken));
            mpEntity.addPart("files[]", createMonitoredFileBody());
            mpEntity.addPart("file_id[]", new StringBody(uploadid_s));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into UpLea.com");
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