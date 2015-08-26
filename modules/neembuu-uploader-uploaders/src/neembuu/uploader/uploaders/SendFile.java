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
import neembuu.uploader.accounts.SendFileAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.Header;
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
 * @author Paralytic
 */
@SmallModule(
    exports={SendFile.class,SendFileAccount.class},
    interfaces={Uploader.class,Account.class},
    name="SendFile.su"

)
public class SendFile extends AbstractUploader{
    
    SendFileAccount sendFileAccount = (SendFileAccount) getAccountsProvider().getAccount("SendFile.su");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String userID = "";
	
    private String downloadlink = "";
    private String deletelink = "";

    public SendFile() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "SendFile.su";

        if (sendFileAccount.loginsuccessful) {
            host = sendFileAccount.username + " | SendFile.su";
        }
        maxFileSizeLimit = 629145600L; // 600 MB (default)        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://sendfile.su/", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form[id=upload_form]").first().attr("action");
        if (userType.equals("reg")) {
            userID = doc.select("form[id=upload_form]").first().select("input[name=user_id]").attr("value");
        } else {
            userID = "0";
        }
    }

    @Override
    public void run() {
        try {
            if (sendFileAccount.loginsuccessful) {
                userType = "reg";
                httpContext = sendFileAccount.getHttpContext();
                maxFileSizeLimit = 629145600L; // 600 MB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 629145600L; // 600 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            // http://s1.sendfile.su/upload.php?X-Progress-ID=alphanumeric
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            if (userType.equals("reg")) {
                mpEntity.addPart("user_id", new StringBody(userID));
            } else {
                mpEntity.addPart("user_id", new StringBody("0"));
            }
            
            mpEntity.addPart("category_id", new StringBody("0"));
            mpEntity.addPart("private", new StringBody("1"));
            mpEntity.addPart("f0", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into SendFile.su");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            Header lastHeader = httpResponse.getLastHeader("Location");
            
            if (lastHeader != null) {
                responseString = StringUtils.stringBetweenTwoStrings(lastHeader.toString(), "ok=", "&");
            } else {
                uploadFailed();
            }
            
            //Read the links
            gettingLink();

            downloadlink = "http://sendfile.su/" +responseString;
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