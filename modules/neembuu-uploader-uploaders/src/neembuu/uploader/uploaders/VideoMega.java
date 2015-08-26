/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.nio.charset.Charset;
import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.VideoMegaAccount;
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
    exports={VideoMega.class,VideoMegaAccount.class},
    interfaces={Uploader.class,Account.class},
    name="VideoMega.tv"
)
public class VideoMega extends AbstractUploader implements UploaderAccountNecessary{
    
    VideoMegaAccount videoMegaAccount = (VideoMegaAccount) getAccountsProvider().getAccount("VideoMega.tv");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String upload_hash = "";
    private String converter = "";
    private String mupload = "";
    
    private String downloadlink = "";
    private String deletelink = "";
    private final ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public VideoMega() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "VideoMega.tv";
        if (videoMegaAccount.loginsuccessful) {
            host = videoMegaAccount.username + " | VideoMega.tv";
        }
        maxFileSizeLimit = 5368709120L; // 5 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://videomega.tv/index_muploadv2.php", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form[id=fileupload]").attr("action");
	upload_hash = doc.select("form[id=fileupload]").select("input[name=upload_hash]").attr("value");
	converter = doc.select("form[id=fileupload]").select("input[name=converter]").attr("value");
	mupload = doc.select("form[id=fileupload]").select("input[name=mupload]").attr("value");
    }

    @Override
    public void run() {
        try {
            if (videoMegaAccount.loginsuccessful) {
                userType = "reg";
                httpContext = videoMegaAccount.getHttpContext();
                maxFileSizeLimit = 5368709120L; // 5 GB
            }
            else {
                host = "VideoMega.tv";
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
            
            // //convert23.videomega.tv/upload.php
            uploadURL = "http://" + StringUtils.removeFirstChars(uploadURL, 2);
            // http://convert23.videomega.tv/upload.php
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("upload_hash", new StringBody(upload_hash));
            mpEntity.addPart("converter", new StringBody(converter));
            mpEntity.addPart("files", createMonitoredFileBody());
            mpEntity.addPart("mupload", new StringBody(mupload));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into VideoMega.tv");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();
            responseString = responseString.replaceAll("\\\\", "");
            
            downloadlink = StringUtils.stringBetweenTwoStrings(responseString, "\"url\": \"", "\"");
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
     * You can upload the following video formats: AVI, RMVB, MKV, FLV, MP4, WMV, MPEG, MPG, MOV 
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
