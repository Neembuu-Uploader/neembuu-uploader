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
import neembuu.uploader.accounts.DropVideoAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={DropVideo.class,DropVideoAccount.class},
    interfaces={Uploader.class,Account.class},
    name="DropVideo.com"
)
public class DropVideo extends AbstractUploader implements UploaderAccountNecessary{
    
    DropVideoAccount dropVideoAccount = (DropVideoAccount) getAccountsProvider().getAccount("DropVideo.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String user_hash = "";
    private String filename_host = "";
    
    private String downloadlink = "";
    private String deletelink = "";
    private final ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public DropVideo() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "DropVideo.com";
        if (dropVideoAccount.loginsuccessful) {
            host = dropVideoAccount.username + " | DropVideo.com";
        }
        maxFileSizeLimit = 4294967296L; // 4 GB (default)
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://dropvideo.com/manager/upload/", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form[id=fileupload]").attr("action");
        user_hash = doc.select("form[id=fileupload]").select("input[name=user_hash]").attr("value");
    }

    @Override
    public void run() {
        try {
            if (dropVideoAccount.loginsuccessful) {
                httpContext = dropVideoAccount.getHttpContext();
                maxFileSizeLimit = 4294967296L; // 4 GB
            }
            else {
                host = "DropVideo.com";
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
            
            // http://213.163.67.175/cgi-bin/upload.cgi?upload_id=178986301204&utype=reg&disk_id=01
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("doupload", new StringBody("1"));
            mpEntity.addPart("user_hash", new StringBody(user_hash));
            mpEntity.addPart("files", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into DropVideo.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            // {"files":[{"url":"","thumbnail_url":"","delete_url":"","delete_type":"DELETE",
            // "name":"bandicam 2015-01-05 00-20-02-369.avi","size":248842}]}
            
            //Read the links
            gettingLink();
            
            uploadURL = responseString;
            uploadURL = StringUtils.stringBetweenTwoStrings(uploadURL, "\"url\":\"", "\"");
            filename_host = responseString;
            filename_host = StringUtils.stringBetweenTwoStrings(filename_host, "\"name\":\"", "\""); 
            
            if (filename_host.isEmpty()) {
                NULogger.getLogger().info("** DropVideo.com ** => Unexpected response from server.");
                NULogger.getLogger().info("** DropVideo.com ** => Either the plugin is broken or the file-host's server(s) are offline.");
                uploadFailed();
            }
            
            if (uploadURL.isEmpty()) {
                NULogger.getLogger().info("** DropVideo.com ** => No download link sent by server.");
                NULogger.getLogger().info("** DropVideo.com ** => Retrieving manually ...");
                
                uploadURL = "http://dropvideo.com/manager/";
                httpPost = new NUHttpPost(uploadURL);
                mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                mpEntity.addPart("folder", new StringBody("0"));
                mpEntity.addPart("orderd", new StringBody("desc"));
                httpPost.setEntity(mpEntity);
                
                NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
                httpResponse = httpclient.execute(httpPost, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());
                
                doc = Jsoup.parse(responseString);
                
                downloadlink = "http://dropvideo.com" +doc.select("div[class=titleoverflow]").first().select("a").attr("href");
                deletelink = UploadStatus.NA.getLocaleSpecificString();
            } else {
                downloadlink = uploadURL;
                deletelink = UploadStatus.NA.getLocaleSpecificString();
            }
            
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
	// permitted formats are: flv, avi, rmvb, mkv, mp4, wmv
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("rmvb");
        allowedVideoExtensions.add("mkv");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("wmv");
    }
}
