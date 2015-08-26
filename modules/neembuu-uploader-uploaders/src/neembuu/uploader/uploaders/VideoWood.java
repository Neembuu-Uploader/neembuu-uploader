/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.VideoWoodAccount;
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
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={VideoWood.class,VideoWoodAccount.class},
    interfaces={Uploader.class,Account.class},
    name="VideoWood.tv"
)
public class VideoWood extends AbstractUploader{
    
    VideoWoodAccount videoWoodAccount = (VideoWoodAccount) getAccountsProvider().getAccount("VideoWood.tv");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String uploadURL;
    private String uploadid_s = "";
    
    private String downloadlink = "";
    private String deletelink = "";
    private final ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public VideoWood() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "VideoWood.tv";
        if (videoWoodAccount.loginsuccessful) {
            host = videoWoodAccount.username + " | VideoWood.tv";
        }
        maxFileSizeLimit = 5368709120L; // 5 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://videowood.tv", httpContext);
        uploadid_s = StringUtils.stringBetweenTwoStrings(responseString, "'upload_id': '", "'");
    }

    @Override
    public void run() {
        try {
            if (videoWoodAccount.loginsuccessful) {
                httpContext = videoWoodAccount.getHttpContext();
                maxFileSizeLimit = 5368709120L; // 5 GB
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 5368709120L; // 5 GB
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
            
            // http://upl.videowood.tv/
            uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "url : '", "'");
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("name", new StringBody(file.getName()));
            mpEntity.addPart("upload_id", new StringBody(uploadid_s));
            mpEntity.addPart("file", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into VideoWood.tv");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            // http://videowood.tv/poll-upload/<upload_id>
            uploadURL = "http://videowood.tv/poll-upload/" + uploadid_s;
            responseString = NUHttpClientUtils.getData(uploadURL, httpContext);
            responseString = responseString.replaceAll("\\\\", "");
            
            downloadlink = StringUtils.stringBetweenTwoStrings(responseString, "\"page_url\":\"", "\"");
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
    
    /**
     * Add all the allowed extensions.
     * Supported: AVI, RMVB, MKV, FLV, MP4, WMV, MPEG, MPG, MOV
     */
    private void addExtensions(){
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("rmvb");
        allowedVideoExtensions.add("mkv");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("wmv");
        allowedVideoExtensions.add("mpeg");
        allowedVideoExtensions.add("mpg");
        allowedVideoExtensions.add("mov");
    }
}
