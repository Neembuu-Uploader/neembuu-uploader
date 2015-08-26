/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.util.ArrayList;
import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.GameFrontAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
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
import neembuu.uploader.uploaders.common.StringUtils;


/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={GameFront.class,GameFrontAccount.class},
    interfaces={Uploader.class,Account.class},
    name="GameFront.com"

)
public class GameFront extends AbstractUploader{
    
    GameFrontAccount gameFrontAccount = (GameFrontAccount) getAccountsProvider().getAccount("GameFront.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String callback = "";
    private String key = "";
    private String upHost = "";
    private String userId = "";
	
    private String downloadlink = "";
    private String deletelink = "";
    private final ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public GameFront() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "GameFront.com";

        if (gameFrontAccount.loginsuccessful) {
            host = gameFrontAccount.username + " | GameFront.com";
        }
        maxFileSizeLimit = 734003200L; // 700 MB (default)        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://www.gamefront.com/files/upload", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form[id=bku-form]").attr("action");
        callback = doc.select("form[id=bku-form]").select("input[name=callback]").attr("value");
        key = doc.select("form[id=bku-form]").select("input[name=key]").attr("value");
        upHost = doc.select("form[id=bku-form]").select("input[name=host]").attr("value");
        if (userType.equals("reg")) {
            userId = StringUtils.stringBetweenTwoStrings(responseString, "\"userId\":\"", "\"");
        }
    }

    @Override
    public void run() {
        try {
            if (gameFrontAccount.loginsuccessful) {
                userType = "reg";
                httpContext = gameFrontAccount.getHttpContext();
                maxFileSizeLimit = 734003200L; // 700 MB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 734003200L; // 700 MB
            }

            addExtensions();
            
            //Check extension
            if(!FileUtils.checkFileExtension(allowedVideoExtensions, file)){
                throw new NUFileExtensionException(file.getName(), host);
            }
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            // http://uploads.gamefront.com/api/upload
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("Filename", new StringBody(file.getName()));
            mpEntity.addPart("ttl", new StringBody("30"));
            mpEntity.addPart("key", new StringBody(key));
            mpEntity.addPart("host", new StringBody(upHost));
            mpEntity.addPart("callback", new StringBody(callback));
            if (userType.equals("reg")) {
                mpEntity.addPart("userId", new StringBody(userId));
            }
            mpEntity.addPart("Filedata", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into GameFront.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();
            
            // {"url":"http:\/\/www.gamefront.com\/files\/45604916\/putty.exe","errorCode":0,"filesId":45604916}
            responseString = responseString.replaceAll("\\\\", "");
            responseString = StringUtils.stringBetweenTwoStrings(responseString, "\"url\":\"", "\"");
            
            downloadlink = responseString;
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
    private void addExtensions(){
        // http://www.gamefront.com/files/upload
	// file_types : "*.rar;*.zip;*.7z;*.gz;*.exe;*.mov;*.mp4;*.avi;*.flv;*.wmv",
        allowedVideoExtensions.add("rar");
        allowedVideoExtensions.add("zip");
        allowedVideoExtensions.add("7z");
        allowedVideoExtensions.add("gz");
        allowedVideoExtensions.add("exe");
        allowedVideoExtensions.add("mov");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("wmv");
    }
}