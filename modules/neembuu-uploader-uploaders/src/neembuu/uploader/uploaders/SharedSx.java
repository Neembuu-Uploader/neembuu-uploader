/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.nio.charset.Charset;
import java.util.ArrayList;
import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.SharedSxAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
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
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.CookieUtils;


/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={SharedSx.class,SharedSxAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Shared.sx"

)
public class SharedSx extends AbstractUploader implements UploaderAccountNecessary{
    
    SharedSxAccount sharedSxAccount = (SharedSxAccount) getAccountsProvider().getAccount("Shared.sx");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private String uploadURL;
    private String sessionID = "";
    private String SID = "";
	
    private String downloadlink = "";
    private String deletelink = "";
    private final ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public SharedSx() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Shared.sx";

        if (sharedSxAccount.loginsuccessful) {
            host = sharedSxAccount.username + " | Shared.sx";
        }
        maxFileSizeLimit = 5368709120L; // 5120 MB (default)        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://shared.sx/upload", httpContext);
        uploadURL = "http://shared.sx/push";
        sessionID = CookieUtils.getCookieValue(httpContext, "user");
    }

    @Override
    public void run() {
        try {
            if (sharedSxAccount.loginsuccessful) {
                httpContext = sharedSxAccount.getHttpContext();
                maxFileSizeLimit = 5368709120L; // 5120 MB
            } else {
                host = "Shared.sx";
                uploadInvalid();
                return;
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
            
            // action=upload&size=248842&session=alphanumeric
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("action", new StringBody("upload"));
            mpEntity.addPart("size", new StringBody(Long.toString(file.length())));
            mpEntity.addPart("session", new StringBody(sessionID));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("** Shared.sx ** --> Retrieving PUSH url");
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            // {"SID":8,"PUSH":"http:\/\/lisa.storage.shared.sx:8080\/push"}
            responseString = responseString.replaceAll("\\\\", "");
            SID = StringUtils.stringBetweenTwoStrings(responseString, "\"SID\":", ",");
            uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "\"PUSH\":\"", "\"");
            
            httpPost = new NUHttpPost(uploadURL);
            mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("Filename", new StringBody(file.getName()));
            mpEntity.addPart("name", new StringBody(file.getName()));
            mpEntity.addPart("session", new StringBody(sessionID));
            mpEntity.addPart("SID", new StringBody(SID));
            mpEntity.addPart("action", new StringBody("push"));
            mpEntity.addPart("file", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("** Shared.sx ** --> Uploading your file to the server ...");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
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
        // No publicly available extension restrictions are known about this host
        // It simply declares "video and music files" as allowed, leaving the details open for ambiguity
        // Therefore using the most common video and music file formats.
        // For this reason, one or more of these formats may not actually be allowed.
        // Will clean this up as and when users report about an incorrect extension
        allowedVideoExtensions.add("3gp");
        allowedVideoExtensions.add("avchd");
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("dat");
        allowedVideoExtensions.add("divx");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("m2v");
        allowedVideoExtensions.add("m4v");
        allowedVideoExtensions.add("mkv");
        allowedVideoExtensions.add("mov");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("mpe");
        allowedVideoExtensions.add("mpg");
        allowedVideoExtensions.add("mpeg");        
        allowedVideoExtensions.add("ogg");
        allowedVideoExtensions.add("ogv");
        allowedVideoExtensions.add("rm");
        allowedVideoExtensions.add("rmvb");
        allowedVideoExtensions.add("vob");
        allowedVideoExtensions.add("webm");
        allowedVideoExtensions.add("wmv");
        allowedVideoExtensions.add("xvid");
        // audio formats follow
        allowedVideoExtensions.add("aac");
        allowedVideoExtensions.add("flac");
        allowedVideoExtensions.add("m4a");
        allowedVideoExtensions.add("mp3");
        allowedVideoExtensions.add("ra");
        allowedVideoExtensions.add("rm");
        allowedVideoExtensions.add("wav");
        allowedVideoExtensions.add("wma");
    }
}