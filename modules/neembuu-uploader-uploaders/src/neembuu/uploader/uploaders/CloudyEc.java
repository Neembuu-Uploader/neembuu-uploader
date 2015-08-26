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
import neembuu.uploader.accounts.CloudyEcAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
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
    exports={CloudyEc.class,CloudyEcAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Cloudy.ec"
)
public class CloudyEc extends AbstractUploader implements UploaderAccountNecessary{
    
    CloudyEcAccount cloudyEcAccount = (CloudyEcAccount) getAccountsProvider().getAccount("Cloudy.ec");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String sessionID = "";
    private String upload_key = "";
    private String uid = "";
    private String upload_server = "";
    private String site_id = "";
    private String get_links_url = "";
    
    private String downloadlink = "";
    private String deletelink = "";
    private final ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public CloudyEc() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Cloudy.ec";
        if (cloudyEcAccount.loginsuccessful) {
            host = cloudyEcAccount.username + " | Cloudy.ec";
        }
        maxFileSizeLimit = 1048576000L; // 1,000 MB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://www.cloudy.ec/upload", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form[id=upload]").attr("action");
        upload_key = StringUtils.stringBetweenTwoStrings(responseString, "upload_key='", "'");
        uid = StringUtils.stringBetweenTwoStrings(responseString, "uid=", ";");
        upload_server = StringUtils.stringBetweenTwoStrings(responseString, "upload_server='", "'");
        site_id = StringUtils.stringBetweenTwoStrings(responseString, "site_id='", "'");
    }

    @Override
    public void run() {
        try {
            if (cloudyEcAccount.loginsuccessful) {
                httpContext = cloudyEcAccount.getHttpContext();
                maxFileSizeLimit = 1048576000L; // 1,000 MB
            }
            else {
                host = "Cloudy.ec";
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
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("fileselect", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into Cloudy.ec");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            sessionID = StringUtils.stringBetweenTwoStrings(responseString, "\"session\":\"", "\"");
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();
            // http://154.43.166.14/upload.php?site=0&upload_key=061449f3d36f4341cf3e98c76ca28b57&uid=40276&session_id=b6cb84b81c7ed2e9643a285d96f45215
            get_links_url = "http://" + upload_server + "/upload.php?site=" + site_id + "&upload_key=" + upload_key + "&uid=" + uid + "&session_id=" + sessionID;
            
            responseString = NUHttpClientUtils.getData(get_links_url, httpContext);
            get_links_url = StringUtils.stringBetweenTwoStrings(responseString, "\"video_url\":\"", "\"");
            get_links_url = get_links_url.replaceAll("\\\\", "");
            
            doc = Jsoup.parse(responseString);
            downloadlink = get_links_url;
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
	// http://www.cloudy.ec/assets/js/script.js
	// (asf|divx|wmv|flv|avi|mpeg|mp4|3gp|mpg|mkv|mp3|m4v|f4v)
        allowedVideoExtensions.add("asf");
        allowedVideoExtensions.add("divx");
        allowedVideoExtensions.add("wmv");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("mpeg");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("3gp");
        allowedVideoExtensions.add("mpg");
        allowedVideoExtensions.add("mkv");
        allowedVideoExtensions.add("mp3");
        allowedVideoExtensions.add("m4v");
        allowedVideoExtensions.add("f4v");
    }
}
