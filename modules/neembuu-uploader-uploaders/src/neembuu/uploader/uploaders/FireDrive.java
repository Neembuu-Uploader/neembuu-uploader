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
import neembuu.uploader.accounts.FireDriveAccount;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={FireDrive.class,FireDriveAccount.class},
    interfaces={Uploader.class,Account.class},
    name="FireDrive.com"
)
public class FireDrive extends AbstractUploader {
    
    FireDriveAccount fireDriveAccount = (FireDriveAccount) getAccountsProvider().getAccount("FireDrive.com");
//    private String apiURL = "http://upload.putlocker.com/uploadapi.php";
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    
    public FireDrive() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "FireDrive.com";

        if (fireDriveAccount.loginsuccessful) {
            host = fireDriveAccount.username + " | FireDrive.com";
        }
        
        maxFileSizeLimit = 1073741824l; //1 GB
    }
    
    /**
     * Upload with <a href="http://www.putlocker.com/apidocs.php">API</a>.
     */
    private void apiUpload() throws UnsupportedEncodingException, IOException, Exception{
//        uploading();
//        ContentBody cbFile = createMonitoredFileBody();
//        NUHttpPost httppost = new NUHttpPost(apiURL);
//        MultipartEntity mpEntity = new MultipartEntity();
//        
//        mpEntity.addPart("file", cbFile);
//        mpEntity.addPart("user", new StringBody(fireDriveAccount.username));
//        mpEntity.addPart("password", new StringBody(fireDriveAccount.password));
//        mpEntity.addPart("convert", new StringBody("1"));
//        httppost.setEntity(mpEntity);
//        HttpResponse response = httpclient.execute(httppost);
//        String reqResponse = EntityUtils.toString(response.getEntity());
//        //NULogger.getLogger().info(reqResponse);
//        
//        if(reqResponse.contains("File Uploaded Successfully")){
//            gettingLink();
//            downURL = StringUtils.stringBetweenTwoStrings(reqResponse, "<link>", "</link>");
//        }
//        else{
//            //Handle the errors
//            status = UploadStatus.GETTINGERRORS;
//            throw new Exception(StringUtils.stringBetweenTwoStrings(reqResponse, "<message>", "</message>"));
//        }
    }
    
    /**
     * Upload with normal uploader.
     */
    public void normalUpload() throws IOException, Exception{
        String uploadPostUrl;
        NUHttpPost httppost;
        HttpResponse response;
        String reqResponse;
        
        uploadPostUrl = "https://upload.firedrive.com/web";
        
        //Getting vars
        final String getUrl = "http://www.firedrive.com/upload?_=" + System.currentTimeMillis();
        reqResponse = NUHttpClientUtils.getData(getUrl, httpContext);
        final String vars = StringUtils.stringBetweenTwoStrings(reqResponse, "return '", "'");
        
        NULogger.getLogger().log(Level.INFO, "getUrl: {0}", getUrl);
        NULogger.getLogger().log(Level.INFO, "vars: {0}", vars);

        //Start the upload
        uploading();

        httppost = new NUHttpPost(uploadPostUrl);
        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("name", new StringBody(file.getName()));
        mpEntity.addPart("vars", new StringBody(vars));
        mpEntity.addPart("target_folder", new StringBody("0"));
        mpEntity.addPart("target_group", new StringBody("0"));
        mpEntity.addPart("file", createMonitoredFileBody());
        httppost.setEntity(mpEntity);
        response = httpclient.execute(httppost, httpContext);
        reqResponse = EntityUtils.toString(response.getEntity());
        
        JSONObject jSonObject = new JSONObject(reqResponse);
        
        if(jSonObject.getString("result").equals("success")){
            //Now we can read the link
            gettingLink();
            downURL = "http://www.firedrive.com/file/" + jSonObject.getString("id");
            NULogger.getLogger().log(Level.INFO, "Download URL: {0}", downURL);
        }
        else{
            //Handle errors
            NULogger.getLogger().info(reqResponse);
            throw new Exception("Error in firedrive.com. Take a look to last jSonObject!");
        }
    }

    @Override
    public void run() {
        uploadInitialising();
        try{
            if (fireDriveAccount.loginsuccessful) {
                httpContext = fireDriveAccount.getHttpContext();
                
                if(fireDriveAccount.isPremium()){
                    maxFileSizeLimit = 53687091200l; //50 GB
                }
                else{
                    maxFileSizeLimit = 1073741824l; //1 GB
                }
                
            }
            else{
                httpContext.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
            }
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), getHost());
            }
            
            normalUpload();
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
