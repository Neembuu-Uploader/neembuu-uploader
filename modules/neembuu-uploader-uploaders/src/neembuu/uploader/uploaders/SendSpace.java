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
import neembuu.uploader.accounts.SendSpaceAccount;
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
import neembuu.uploader.utils.RemoveCryptographyRestrictions;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={SendSpace.class,SendSpaceAccount.class},
    interfaces={Uploader.class,Account.class},
    name="SendSpace.com"
)
public class SendSpace extends AbstractUploader{
    
    SendSpaceAccount sendSpaceAccount = (SendSpaceAccount) getAccountsProvider().getAccount("SendSpace.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String progressURL = "";
    private String signature = "";
    private int formSequence = 0;
    private String userID = "";
    private String hostName = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public SendSpace() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "SendSpace.com";
        if (sendSpaceAccount.loginsuccessful) {
            host = sendSpaceAccount.username + " | SendSpace.com";
        }
        maxFileSizeLimit = 314572800; // 300 MB (default)
        
        RemoveCryptographyRestrictions.removeCryptographyRestrictions();
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("https://www.sendspace.com/", httpContext);
        doc = Jsoup.parse(responseString);
        
        if (userType.equals("reg")) {
            formSequence = 1;
            userID = doc.select("form").eq(formSequence).select("input[name=userid]").attr("value");
        } else {
            formSequence = 3;
        }
        
        uploadURL = doc.select("form").eq(formSequence).attr("action");
        signature = doc.select("form").eq(formSequence).select("input[name=signature]").attr("value");
        progressURL = doc.select("form").eq(formSequence).select("input[name=PROGRESS_URL]").attr("value");
    }

    @Override
    public void run() {
        try {
            if (sendSpaceAccount.loginsuccessful) {
                userType = "reg";
                httpContext = sendSpaceAccount.getHttpContext();
                maxFileSizeLimit = 314572800; // 300 MB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 314572800; // 300 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            // https://fs08u.sendspace.com/upload?SPEED_LIMIT=0&MAX_FILE_SIZE=314572800&UPLOAD_IDENTIFIER=x.x.x.x.0&DESTINATION_DIR=xx
            // fs08u.sendspace.com/upload?SPEED_LIMIT=0&MAX_FILE_SIZE=314572800&UPLOAD_IDENTIFIER=x.x.x.x.0&DESTINATION_DIR=xx
            hostName = StringUtils.stringStartingFromString(uploadURL, "https://");
            // fs08u.sendspace.com
            hostName = StringUtils.stringUntilString(hostName, "sendspace.com") + "sendspace.com";
            
            // https://fs08u.sendspace.com/upload?SPEED_LIMIT=0&MAX_FILE_SIZE=314572800&UPLOAD_IDENTIFIER=910609187.1440099567.3BB289C9.22.0&DESTINATION_DIR=22
            httpPost = new NUHttpPost(uploadURL);
            httpPost.setHeader("Host", hostName);
            httpPost.setHeader("Referer", "https://www.sendspace.com/");
            
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("PROGRESS_URL", new StringBody(progressURL));
            mpEntity.addPart("js_enabled", new StringBody("1"));
            mpEntity.addPart("signature", new StringBody(signature));
            mpEntity.addPart("upload_files", new StringBody(""));
            if (userType.equals("reg")) {
                mpEntity.addPart("userid", new StringBody(userID));
                mpEntity.addPart("folder_id", new StringBody("0"));
            }
            mpEntity.addPart("terms", new StringBody("1"));
            mpEntity.addPart("file[]", new StringBody(""));
            mpEntity.addPart("description[]", new StringBody(""));
            mpEntity.addPart("upload_file[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into SendSpace.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();
            downloadlink = doc.select("div[class=file_description]").select("a").first().attr("href");
            deletelink = doc.select("a[class=link]").attr("href");

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