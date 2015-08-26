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
import neembuu.uploader.accounts.MediaFireAccount;
import neembuu.uploader.exceptions.NUException;
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
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.uploaders.common.StringUtils;


/**
 *
 * @author Paralytic (rewritten completely, original script by Davide)
 */
@SmallModule(
    exports={MediaFire.class,MediaFireAccount.class},
    interfaces={Uploader.class,Account.class},
    name="MediaFire.com"

)
public class MediaFire extends AbstractUploader implements UploaderAccountNecessary{
    
    MediaFireAccount mediaFireAccount = (MediaFireAccount) getAccountsProvider().getAccount("MediaFire.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private String uploadURL;
    private String uploadKey = "";
    private String uploadStatus = "";
	
    private String downloadlink = "";
    private String deletelink = "";

    public MediaFire() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "MediaFire.com";

        if (mediaFireAccount.loginsuccessful) {
            host = mediaFireAccount.username + " | MediaFire.com";
        }
        maxFileSizeLimit = 209715200L; // 200 MB
    }

    private void initialize() throws Exception {
        uploadURL = "http://www.mediafire.com/api/1.3/upload/simple.php?session_token=" +mediaFireAccount.sessionToken;
    }

    @Override
    public void run() {
        try {
            if (mediaFireAccount.loginsuccessful) {
                if(mediaFireAccount.isPremium()){
                    httpContext = mediaFireAccount.getHttpContext();
                    // PREMIUM user
                    maxFileSizeLimit = 21474836480L; // 20 GB
                    NULogger.getLogger().info("** MediaFire.com ** => Proceeding as a premium user.");
                } else {
                    // Regular user
                    httpContext = mediaFireAccount.getHttpContext();
                    maxFileSizeLimit = 209715200L; // 200 MB
                    NULogger.getLogger().info("** MediaFire.com ** => Proceeding as a regular user (non-premium)");
                }
            } else {
                host = "MediaFire.com";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("Filedata", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("** MediaFire.com ** => Uploading your file now ...");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            uploadKey = StringUtils.stringBetweenTwoStrings(responseString, "<key>", "</key>");
            responseString = StringUtils.stringStartingFromString(responseString, "</doupload>");
            responseString = StringUtils.stringBetweenTwoStrings(responseString, "<result>", "</result>");
            
            if (responseString.equals("Success") && !uploadKey.isEmpty()){
                NULogger.getLogger().info("** MediaFire.com ** => Received the upload key ... now retrieving the link");
                //Read the links
                gettingLink();
                
                int i=0;
                do {
                    responseString = "https://www.mediafire.com/api/upload/poll_upload.php?key=" +uploadKey;
                    responseString = NUHttpClientUtils.getData(responseString, httpContext);
                    uploadStatus = StringUtils.stringBetweenTwoStrings(responseString, "<status>", "</status>");
                    if (!uploadStatus.equals("99")) {
                        NULogger.getLogger().log(Level.INFO, "** MediaFire.com ** => Attempt {0}: File not processed yet at MediaFire's end.", i+1);
                    }
                    i++;
                    Thread.sleep(1000);
                } while(!uploadStatus.equals("99") && i<10);
                
                responseString = StringUtils.stringBetweenTwoStrings(responseString, "<quickkey>", "</quickkey>");
                
                if (!responseString.isEmpty()){
                    NULogger.getLogger().info("** MediaFire.com ** => File uploaded successfully!");
                } else {
                    NULogger.getLogger().log(Level.INFO, "** MediaFire.com ** => Retry timeout! Server did not send back a proper response even after {0} retries", i);
                    uploadFailed();
                }
                
                downloadlink = "http://www.mediafire.com/download/" +responseString+ "/";
                deletelink = UploadStatus.NA.getLocaleSpecificString();

                NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
                NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
                downURL = downloadlink;
                delURL = deletelink;

                uploadFinished();
            }
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            uploadFailed();
        }
    }
}