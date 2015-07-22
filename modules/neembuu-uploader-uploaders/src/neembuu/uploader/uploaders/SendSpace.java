/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.SendSpaceAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
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

/**
 *
 * @author Dinesh
 */
@SmallModule(
    exports={SendSpace.class,SendSpaceAccount.class},
    interfaces={Uploader.class,Account.class},
    name="SendSpace.com"
)
public class SendSpace extends AbstractUploader {

    SendSpaceAccount sendSpaceAccount = (SendSpaceAccount) getAccountsProvider().getAccount("SendSpace.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private String stringResponse;
    
    private String sidcookie = "", ssuicookie = "";
    private String progressURL = "";
    private String postURL;
    private String uploadID;
    private String destinationDir;
    private String signature;
    private String userID;
    private String uploadresponse;
    private String downloadlink;
    private String deletelink;
    private long fileSizeLimit = 314572800l; //300 MB

    public SendSpace() {
        host = "SendSpace.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        if (sendSpaceAccount.loginsuccessful) {
            //  login = true;
            host = sendSpaceAccount.username + " | SendSpace.com";
        }
    }

    private void initialize() throws Exception {
        NULogger.getLogger().info("Getting startup cookie from sendspace.com");
        httpGet = new NUHttpGet("http://www.sendspace.com/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        sidcookie = CookieUtils.getCookieNameValue(httpContext, "SID");
        ssuicookie = CookieUtils.getCookieNameValue(httpContext, "ssui");
        
        NULogger.getLogger().log(Level.INFO, "sidcookie: {0}", sidcookie);
        NULogger.getLogger().log(Level.INFO, "ssuicookie: {0}", ssuicookie);

    }

    public void getDynamicSendSpaceValues() throws Exception {
        
        httpGet = new NUHttpGet("http://www.sendspace.com/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        //FileUtils.saveInFile("SendSpace.html", stringResponse);
        
        progressURL = StringUtils.stringBetweenTwoStrings(stringResponse, "name=\"PROGRESS_URL\" value=\"", "\"", false);
        NULogger.getLogger().log(Level.INFO, "Progress URL : {0}", progressURL);
        String tmpDomain = progressURL.substring(0, progressURL.indexOf("progress"));
        uploadID = progressURL.substring(progressURL.indexOf("UPLOAD_IDENTIFIER"));
        NULogger.getLogger().log(Level.INFO, "Upload ID : {0}", uploadID);
        destinationDir = StringUtils.stringBetweenTwoStrings(stringResponse, "var upload_form_destination_dir = '", "'", false);
        NULogger.getLogger().log(Level.INFO, "destinationdir : {0}", destinationDir);
        signature = StringUtils.stringBetweenTwoStrings(stringResponse, "\"signature\" value=\"", "\"", false);
        NULogger.getLogger().log(Level.INFO, "signature : {0}", signature);
        //\"userid\" value=\"
        if (sendSpaceAccount.loginsuccessful) {
            userID = StringUtils.stringBetweenTwoStrings(stringResponse, "\"userid\" value=\"", "\"", false);
            NULogger.getLogger().log(Level.INFO, "User ID : {0}", userID);
        }

        postURL = tmpDomain + "upload?SPEED_LIMIT=0&MAX_FILE_SIZE=314572800&" + uploadID + "&DESTINATION_DIR=" + destinationDir;
        NULogger.getLogger().log(Level.INFO, "Post URL {0}", postURL);

    }

    private void fileUpload() throws Exception {
        httpPost = new NUHttpPost(postURL);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("PROGRESS_URL", new StringBody(progressURL));
        mpEntity.addPart("DESTINATION_DIR", new StringBody(destinationDir));
        mpEntity.addPart("js_enabled", new StringBody("1"));
        mpEntity.addPart("signature", new StringBody(signature));
        mpEntity.addPart("upload_files", new StringBody(""));
        if (sendSpaceAccount.loginsuccessful) {
            mpEntity.addPart("userid", new StringBody(userID));
        }

        mpEntity.addPart("terms", new StringBody("1"));
        mpEntity.addPart("file[]", new StringBody(""));
        mpEntity.addPart("description[]", new StringBody(""));
        mpEntity.addPart("upload_file[]", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
        uploading();
        NULogger.getLogger().info("Now uploading your file into sendspace.com");
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = httpResponse.getEntity();
        NULogger.getLogger().info(httpResponse.getStatusLine().toString());
        if (resEntity != null) {
            gettingLink();
            uploadresponse = EntityUtils.toString(resEntity);
        }
//        if (resEntity != null) {
//            resEntity.consumeContent();
//        }


        NULogger.getLogger().log(Level.INFO, "resp : {0}", uploadresponse);

        downloadlink = StringUtils.stringBetweenTwoStrings(uploadresponse, "Download Link", "target", false);
        deletelink = StringUtils.stringBetweenTwoStrings(uploadresponse, "Delete File Link", "target", false);
        downloadlink = downloadlink.replaceAll("\\s+", " ");
        deletelink = deletelink.replaceAll("\\s+", " ");
        downloadlink = StringUtils.stringBetweenTwoStrings(downloadlink, "<a href=\"", "\"", false);
        deletelink = StringUtils.stringBetweenTwoStrings(deletelink, "href=\"", "\"", false);
        NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
        NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
        downURL = downloadlink;
        delURL = deletelink;

        uploadFinished();

    }

    @Override
    public void run() {
        try {
            if (sendSpaceAccount.loginsuccessful) {
                host = sendSpaceAccount.username + " | SendSpace.com";
            } else {
                host = "SendSpace.com";
            }

            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), sendSpaceAccount.getHOSTNAME());
            }


            uploadInitialising();
            if (sendSpaceAccount.loginsuccessful) {
                httpContext = sendSpaceAccount.getHttpContext();
            }
            else{
                CookieStore cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                initialize();
            }
            
            getDynamicSendSpaceValues();
            fileUpload();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(SendSpace.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
}
