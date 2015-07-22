/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import neembuu.uploader.accounts.SockShareAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={SockShare.class,SockShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="SockShare.com"
)
public class SockShare extends AbstractUploader{

    SockShareAccount sockShareAccount = (SockShareAccount) getAccountsProvider().getAccount("SockShare.com");
    String uploadGetURL = "http://www.sockshare.com/upload_form.php";
    String apiURL = "http://upload.sockshare.com/uploadapi.php";
    HttpClient httpclient = NUHttpClient.getHttpClient();
    HttpContext httpContext;
    CookieStore cookieStore;
    
    private long loggedInFileSizeLimit = 1073741824l; //1 GB
    private long notLoggedInFileSizeLimit = 157286400l; //150 MB
    
    public SockShare() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "SockShare.com";

        if (sockShareAccount.loginsuccessful) {
            host = sockShareAccount.username + " | SockShare.com";
        }
    }
    
    /**
     * Upload with <a href="http://www.sockshare.com/apidocs.php">API</a>.
     */
    private void apiUpload() throws UnsupportedEncodingException, IOException, Exception{
        uploading();
        ContentBody cbFile = createMonitoredFileBody();
        NUHttpPost httppost = new NUHttpPost(apiURL);
        MultipartEntity mpEntity = new MultipartEntity();
        
        mpEntity.addPart("file", cbFile);
        mpEntity.addPart("user", new StringBody(sockShareAccount.username));
        mpEntity.addPart("password", new StringBody(sockShareAccount.password));
        httppost.setEntity(mpEntity);
        HttpResponse response = httpclient.execute(httppost);
        String reqResponse = EntityUtils.toString(response.getEntity());
        //NULogger.getLogger().info(reqResponse);
        
        if(reqResponse.contains("File Uploaded Successfully")){
            gettingLink();
            downURL = StringUtils.stringBetweenTwoStrings(reqResponse, "<link>", "</link>");
        }
        else{
            //Handle the errors
            status = UploadStatus.GETTINGERRORS;
            throw new Exception(StringUtils.stringBetweenTwoStrings(reqResponse, "<message>", "</message>"));
        }
    }
    
    /**
     * Upload with normal uploader.
     */
    public void normalUpload() throws IOException, Exception{
        String uploadPostUrl;
        NUHttpPost httppost;
        HttpResponse response;
        String reqResponse;
        String doneURL;
        String sessionID;
        String authHash;
        
        //Set the cookie store
        cookieStore = new BasicCookieStore();
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH); //CookiePolicy
        
        //Get the url for upload
        reqResponse = NUHttpClientUtils.getData(uploadGetURL, httpContext);
        
        //Read various strings
        uploadPostUrl = StringUtils.stringBetweenTwoStrings(reqResponse, "'script' : '", "'");
        authHash = StringUtils.stringBetweenTwoStrings(reqResponse, "auth_hash':'", "'");
        doneURL = "http://www.sockshare.com/cp.php?uploaded=upload_form.php?done="+StringUtils.stringBetweenTwoStrings(reqResponse, "upload_form.php?done=", "'"); //Now find the done URL
        NULogger.getLogger().log(Level.INFO, "Upload post URL: {0}", uploadPostUrl);
        NULogger.getLogger().log(Level.INFO, "AuthHash: {0}", authHash);
        NULogger.getLogger().log(Level.INFO, "Done URL: {0}", doneURL);
        sessionID = CookieUtils.getCookieValue(httpContext, "PHPSESSID");

        //Start the upload
        uploading();

        httppost = new NUHttpPost(uploadPostUrl);
        ContentBody cbFile = createMonitoredFileBody();
        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        mpEntity.addPart("fileext", new StringBody("*"));
        mpEntity.addPart("do_convert", new StringBody("1"));
        mpEntity.addPart("session", new StringBody(sessionID));
        mpEntity.addPart("folder", new StringBody("/"));
        mpEntity.addPart("auth_hash", new StringBody(authHash));
        mpEntity.addPart("Filedata", cbFile);
        mpEntity.addPart("Upload", new StringBody("Submit Query"));
        httppost.setEntity(mpEntity);
        response = httpclient.execute(httppost, httpContext);
        reqResponse = EntityUtils.toString(response.getEntity());
        
        if("cool story bro".equals(reqResponse)){
            //Now we can read the link
            gettingLink();
            reqResponse = NUHttpClientUtils.getData(doneURL, httpContext);
            downURL = "http://www.sockshare.com/file/"+StringUtils.stringBetweenTwoStrings(reqResponse, "<a href=\"http://www.sockshare.com/file/", "\"");
            NULogger.getLogger().log(Level.INFO, "Download URL: {0}", downURL);
        }
        else{
            //Handle errors
            NULogger.getLogger().info(reqResponse);
            throw new Exception("Error in sockshare.com. Take a look to last reqResponse!");
        }
    }

    @Override
    public void run() {
        uploadInitialising();
        try{
            if (sockShareAccount.loginsuccessful) {
                if (file.length() > loggedInFileSizeLimit) {
                    throw new NUMaxFileSizeException(loggedInFileSizeLimit, file.getName(), sockShareAccount.getHOSTNAME());
                }
                apiUpload();
            }
            else{
                if (file.length() > notLoggedInFileSizeLimit) {
                    throw new NUMaxFileSizeException(notLoggedInFileSizeLimit, file.getName(), sockShareAccount.getHOSTNAME());
                }
                normalUpload();
            }
            
            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch(Exception e){
            NULogger.getLogger().log(Level.INFO, "Exception: {0}", e);
            uploadFailed();
        }
    }

}
