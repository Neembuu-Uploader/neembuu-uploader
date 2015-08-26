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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.NetUAccount;
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
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.uploaders.common.FileUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={NetU.class,NetUAccount.class},
    interfaces={Uploader.class,Account.class},
    name="NetU.tv"
)
public class NetU extends AbstractUploader{
    
    NetUAccount netUAccount = (NetUAccount) getAccountsProvider().getAccount("NetU.tv");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String uploadURL;
    private String base_path = "";
    private String hash = "";
    private String time_hash = "";
    
    private String downloadlink = "";
    private String deletelink = "";
    private final ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public NetU() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "NetU.tv";
        if (netUAccount.loginsuccessful) {
            host = netUAccount.username + " | NetU.tv";
        }
        maxFileSizeLimit = 2097152000L; // 2,000 MB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://netu.tv", httpContext);
        
        base_path = StringUtils.stringBetweenTwoStrings(responseString, "var server_path = '", "';");
        hash = StringUtils.stringBetweenTwoStrings(responseString, "var hash = '", "';");
        time_hash = StringUtils.stringBetweenTwoStrings(responseString, "var time_hash = '", "';");
    }

    @Override
    public void run() {
        try {
            if (netUAccount.loginsuccessful) {
                httpContext = netUAccount.getHttpContext();
                maxFileSizeLimit = 2097152000L; // 2,000 MB
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 2097152000L; // 2,000 MB
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
            
            uploadURL = base_path + "/actions/file_uploader.php";
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("name", new StringBody(file.getName()));
            mpEntity.addPart("hash", new StringBody(hash));
            mpEntity.addPart("time_hash", new StringBody(time_hash));
            mpEntity.addPart("Filedata", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into NetU.tv");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            responseString = StringUtils.stringBetweenTwoStrings(responseString, "\"file_name\":\"", "\"");
            
            //Read the links
            gettingLink();
            httpPost = new NUHttpPost("http://netu.tv/actions/file_uploader.php");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("file_name", responseString));
            formparams.add(new BasicNameValuePair("insertVideo", "yes"));
            formparams.add(new BasicNameValuePair("server", uploadURL));
            formparams.add(new BasicNameValuePair("title", file.getName()));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            responseString = StringUtils.stringBetweenTwoStrings(responseString, "\"video_link\":\"", "\"");
            responseString = responseString.replaceAll("\\\\", "");
            
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
    
    /**
     * Add all the allowed extensions.
     * var fileExt = '*.wmv;*.avi;*.divx;*.3gp;*.mov;*.mpeg;*.mpg;*.xvid;
     * *.flv;*.asf;*.rm;*.dat;*.mp4;*.mkv;*.m4v;*.f4v;';
     */
    private void addExtensions(){
        allowedVideoExtensions.add("wmv");
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("divx");
        allowedVideoExtensions.add("3gp");
        allowedVideoExtensions.add("mov");
        allowedVideoExtensions.add("mpeg");
        allowedVideoExtensions.add("mpg");
        allowedVideoExtensions.add("xvid");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("asf");
        allowedVideoExtensions.add("rm");
        allowedVideoExtensions.add("dat");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("mkv");
        allowedVideoExtensions.add("m4v");
        allowedVideoExtensions.add("f4v");
    }
}
