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
import neembuu.uploader.accounts.BeStreamTvAccount;
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
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={BeStreamTv.class,BeStreamTvAccount.class},
    interfaces={Uploader.class,Account.class},
    name="BeStream.tv"
)
public class BeStreamTv extends AbstractUploader implements UploaderAccountNecessary{
    
    BeStreamTvAccount beStreamTvAccount = (BeStreamTvAccount) getAccountsProvider().getAccount("BeStream.tv");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private String uploadURL;
    private String sessionID = "";
    private String cTracker = "";
    private final String maxChunkSize = "2092152";
    
    private String downloadlink = "";
    private String deletelink = "";
    private final ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public BeStreamTv() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "BeStream.tv";
        if (beStreamTvAccount.loginsuccessful) {
            host = beStreamTvAccount.username + " | BeStream.tv";
        }
        maxFileSizeLimit = 2147483648L; // 2 GB (default)
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://bestream.tv/account_home.html", httpContext);
        
        uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "url: '", "'");
        sessionID = StringUtils.stringBetweenTwoStrings(responseString, "_sessionid: '", "'");
        cTracker = StringUtils.stringBetweenTwoStrings(responseString, "cTracker: '", "'");
    }

    @Override
    public void run() {
        try {
            if (beStreamTvAccount.loginsuccessful) {
                httpContext = beStreamTvAccount.getHttpContext();
                maxFileSizeLimit = 2147483648L; // 2 GB
            }
            else {
                host = "BeStream.tv";
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
            
            // http://bs23b.bestream.tv/core/page/ajax/file_upload_handler.ajax.php?r=bestream.tv&p=http&csaKey1=somestring&csaKey2=somestring
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("cTracker", new StringBody(cTracker));
            mpEntity.addPart("_sessionid", new StringBody(sessionID));
            mpEntity.addPart("maxChunkSize", new StringBody(maxChunkSize));
            mpEntity.addPart("folderId", new StringBody(""));
            mpEntity.addPart("files[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into BeStream.tv");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            responseString = responseString.replaceAll("\\\\", "");
            
            //Read the links
            gettingLink();

            downloadlink = StringUtils.stringBetweenTwoStrings(responseString, "\"url\":\"", "\"");
            deletelink = StringUtils.stringBetweenTwoStrings(responseString, "\"delete_url\":\"", "\"");

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
	//  3gp, avi, dfxp, flv, gif, jpg, m4v, mj2, mkv, mov, mp3,
        //  mp4, mpeg, mpg, ogg, png, srt, webm, wmv, xml, vtt.
        allowedVideoExtensions.add("3gp");
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("dfxp");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("gif");
        allowedVideoExtensions.add("jpg");
        allowedVideoExtensions.add("m4v");
        allowedVideoExtensions.add("mj2");
        allowedVideoExtensions.add("mkv");
        allowedVideoExtensions.add("mov");
        allowedVideoExtensions.add("mp3");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("mpeg");
        allowedVideoExtensions.add("mpg");
        allowedVideoExtensions.add("ogg");
        allowedVideoExtensions.add("png");
        allowedVideoExtensions.add("srt");
        allowedVideoExtensions.add("webm");
        allowedVideoExtensions.add("wmv");
        allowedVideoExtensions.add("xml");
        allowedVideoExtensions.add("vtt");
    }
}
