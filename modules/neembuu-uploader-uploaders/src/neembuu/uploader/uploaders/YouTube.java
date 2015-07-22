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
import neembuu.uploader.accounts.YouTubeAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.uploaders.common.MonitoredFileEntity;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

/**
 * Using <a href="https://developers.google.com/youtube/2.0/developers_guide_protocol_clientlogin">API v.2.0</a>.
 * @author davidepastore
 */
@SmallModule(
    exports={YouTube.class,YouTubeAccount.class},
    interfaces={Uploader.class,Account.class},
    name="YouTube.com"
)
public class YouTube extends AbstractUploader implements UploaderAccountNecessary{
    
    YouTubeAccount youTubeAccount = (YouTubeAccount) getAccountsProvider().getAccount("YouTube.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private HttpPost httpPost;
    private String responseString;
    
    private String uploadURL;
    private String sid = "";
    
    private final String content_1 = "<?xml version=\"1.0\" encoding=\"utf-8\"?> <entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:gd=\"http://schemas.google.com/g/2005\"> <media:group xmlns:media=\"http://search.yahoo.com/mrss/\"> <media:title>%s</media:title> <media:description>%s</media:description> <media:keywords /> <media:category>Entertainment</media:category> </media:group> <title type=\"text\">%s</title> </entry>";
    
    private final String API_KEY = "AI39si47NjQorTh0JVJ_uJM5drdfB9DGDOmMO3Xu7tcYBe_ZSHgt5jWVQARyOjEU_-ViVA0CThYnOF4TiLDCJv8THDAy_qrb7w";
    private String downloadlink = "";
    
    private ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public YouTube() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "YouTube.com";
        if (youTubeAccount.loginsuccessful) {
            host = youTubeAccount.username + " | YouTube.com";
        }
        maxFileSizeLimit = 21474836480l; //20 GB
        
    }

    private void initialize() throws Exception {
        
    }

    @Override
    public void run() {
        try {
            if (youTubeAccount.loginsuccessful) {
                sid = youTubeAccount.getSid();
                maxFileSizeLimit = 21474836480l; //20 GB
            } else {
                host = "YouTube.com";
                uploadInvalid();
                return;
            }
            
            addExtensions();
            
            //Check content type ( https://support.google.com/youtube/troubleshooter/2888402?hl=en#ts=2888339 )
            
            //Check extension
            if(!FileUtils.checkFileExtension(allowedVideoExtensions, file)){
                throw new NUFileExtensionException(file.getName(), host);
            }

            //Check length
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            
            /* 1st step */
            //httpPost = new HttpPost(String.format("http://uploads.gdata.youtube.com/resumable/feeds/api/users/%s/uploads", code));
            httpPost = new HttpPost("http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads");
            String xml = String.format(content_1, file.getName(), file.getName(), file.getName());//, code);
            StringEntity stringEntity = new StringEntity(xml, Consts.UTF_8);

            httpPost.setHeader("Authorization", "GoogleLogin auth=" + sid);
            httpPost.setHeader("Content-Type", "application/atom+xml; charset=UTF-8");
            httpPost.setHeader("X-Upload-Content-Length", Long.toString(file.length()));
            httpPost.setHeader("X-Upload-Content-Type", FileUtils.getContentType(file));
            httpPost.setHeader("Slug", file.getName());
            httpPost.setHeader("GData-Version", "2.0");
            httpPost.setHeader("X-GData-Key", "key=" + API_KEY);
            
            httpPost.setEntity(stringEntity);
            httpResponse = httpclient.execute(httpPost);//, httpContext); 
            Header lastHeader = httpResponse.getLastHeader("Location");
            
            //NULogger.getLogger().log(Level.INFO, "XML: {0}", xml);
            
            uploadURL = lastHeader.getValue();
            
            NULogger.getLogger().log(Level.INFO, "uploadURL: {0}", uploadURL);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            
            /* 2nd step */
            httpPost = new HttpPost(uploadURL);
            
            httpPost.setHeader("X-GData-Key", "key=" + API_KEY);
            httpPost.setHeader("Authorization", "GoogleLogin auth=" + sid);
            httpPost.setHeader("Content-Range", "bytes 0-" + (file.length() - 1) + "/" + file.length());
            httpPost.setHeader("Content-Type", FileUtils.getContentType(file));
            //Content-Type: video/mp4
            
            MonitoredFileEntity fileEntity = createMonitoredFileEntity();
            httpPost.setEntity(fileEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into YouTube.com");
            uploading();
            httpResponse = httpclient.execute(httpPost);
            
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            
            /* 3rd step */
            //FileUtils.saveInFile("YouTubeUploadFinal.xml", responseString);
            
            gettingLink();
            downloadlink = "http://www.youtube.com/watch?v=" + StringUtils.stringBetweenTwoStrings(responseString, "<yt:videoid>", "</yt:videoid>");
            
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            downURL = downloadlink;
            
            
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
     * http://www.ifunia.com/youtube-column/best-youtube-video-format.html
     * https://support.google.com/youtube/troubleshooter/2888402?rd=1
     * http://stackoverflow.com/questions/5222146/supported-video-formats-for-youtube
     */
    private void addExtensions(){
        allowedVideoExtensions.add("wmv");
        allowedVideoExtensions.add("3gp");
        allowedVideoExtensions.add("3gpp");
        allowedVideoExtensions.add("webm");
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("mov");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("mpeg");
        allowedVideoExtensions.add("mpeg4");
        allowedVideoExtensions.add("mpegps");
        allowedVideoExtensions.add("mpg");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("swf");
        allowedVideoExtensions.add("mkv");
    }
    
}
 
