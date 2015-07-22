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
import neembuu.uploader.accounts.SolidfilesAccount;
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
    exports={Solidfiles.class,SolidfilesAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Solidfiles.com"
)
public class Solidfiles extends AbstractUploader {
    
    SolidfilesAccount solidfilesAccount = (SolidfilesAccount) getAccountsProvider().getAccount("Solidfiles.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private HttpContext httpContext = new BasicHttpContext();
    private CookieStore cookieStore;
    private NUHttpPost httpPost;
    
    private String downloadlink = "http://www.solidfiles.com/d/%s/";
    private long fileSizeLimit = 524288000l; //500 MB

    public Solidfiles() {
        host = "Solidfiles.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        
        if (solidfilesAccount.loginsuccessful) {
            //login = true;
            host = solidfilesAccount.username + " | Solidfiles.com";
        }
    }

    @Override
    public void run() {
        try {
            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), this.getHost());
            }
            
            if (solidfilesAccount.loginsuccessful) {
                httpContext = solidfilesAccount.getHttpContext();
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
            Logger.getLogger(Solidfiles.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
    
    private void fileupload() throws Exception {
        
        if(!solidfilesAccount.loginsuccessful){
            NUHttpClientUtils.getData("http://www.solidfiles.com/", httpContext);
        }
        
        httpPost = new NUHttpPost("http://www.solidfiles.com/upload/process/0/");


        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("name", new StringBody(file.getName()));
        reqEntity.addPart("file", createMonitoredFileBody());
        
        httpPost.setEntity(reqEntity);
        uploading();
        NULogger.getLogger().info("Now uploading your file into solidfiles.com. Please wait...");
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = httpResponse.getEntity();
        
        String downloadCode;
        if (resEntity != null) {
            gettingLink();
            downloadCode = EntityUtils.toString(resEntity);
            NULogger.getLogger().log(Level.INFO, "Download code :{0}", downloadCode);
            downloadlink = String.format(downloadlink, downloadCode);
        }

        downURL = downloadlink;
        uploadFinished();
    }
    
}
