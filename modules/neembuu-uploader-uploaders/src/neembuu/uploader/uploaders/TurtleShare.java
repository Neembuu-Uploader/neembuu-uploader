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
import neembuu.uploader.accounts.TurtleShareAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={TurtleShare.class,TurtleShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="TurtleShare.com",
    ignore = true
)
public class TurtleShare extends AbstractUploader{
    
    TurtleShareAccount turtleShareAccount = (TurtleShareAccount) getAccountsProvider().getAccount("TurtleShare.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private HttpContext httpContext = new BasicHttpContext();
    private CookieStore cookieStore;
    private NUHttpPost httpPost;
    
    private String uploadUrl = "http://www.turtleshare.com/iupload/";

    public TurtleShare() {
        host = "TurtleShare.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        maxFileSizeLimit = 536870912l; //512 MB
        
        if (turtleShareAccount.loginsuccessful) {
            host = turtleShareAccount.username + " | TurtleShare.com";
            maxFileSizeLimit = 1073741824l; //1 GB
        }
    }

    @Override
    public void run() {
        try {
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), this.getHost());
            }
            
            if (turtleShareAccount.loginsuccessful) {
                httpContext = turtleShareAccount.getHttpContext();
            }
            else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            }
            
            uploadInitialising();
            fileupload();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(TurtleShare.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
    
    private void fileupload() throws Exception {
        
        if(!turtleShareAccount.loginsuccessful){
            NUHttpClientUtils.getData("http://www.turtleshare.com/", httpContext);
        }
        
        httpPost = new NUHttpPost(uploadUrl);


        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("file", createMonitoredFileBody());
        
        httpPost.setEntity(reqEntity);
        uploading();
        NULogger.getLogger().info("Now uploading your file into turtleshare.com. Please wait...");
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = httpResponse.getEntity();
        
        if (resEntity != null) {
            String responseString;
            JSONObject jSonObject;
            gettingLink();
            responseString = EntityUtils.toString(resEntity);
            jSonObject = new JSONObject(responseString);
            JSONArray jSonArray = jSonObject.getJSONArray("files");
            jSonObject = jSonArray.getJSONObject(0);
            downURL = "http:" + jSonObject.getString("url");
        }

        uploadFinished();
    }
    
}
