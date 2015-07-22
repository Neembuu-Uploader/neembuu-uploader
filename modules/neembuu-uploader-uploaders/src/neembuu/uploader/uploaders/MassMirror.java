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
import neembuu.uploader.accounts.MassMirrorAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
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
import org.json.JSONObject;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={MassMirror.class,MassMirrorAccount.class},
    interfaces={Uploader.class,Account.class},
    name="MassMirror.com"
)
public class MassMirror extends AbstractUploader implements UploaderAccountNecessary{
    
    MassMirrorAccount massMirrorAccount = (MassMirrorAccount) getAccountsProvider().getAccount("MassMirror.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private JSONObject jSonObject;
    private final String uploadURL = "http://www.massmirror.com/manage/";
    
    private String downloadlink = "";

    public MassMirror() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "MassMirror.com";
        if (massMirrorAccount.loginsuccessful) {
            host = massMirrorAccount.username + " | MassMirror.com";
        }
        maxFileSizeLimit = 2147483648l; //2 GB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://www.massmirror.com", httpContext);
    }

    @Override
    public void run() {
        try {
            if (massMirrorAccount.loginsuccessful) {
                httpContext = massMirrorAccount.getHttpContext();
                maxFileSizeLimit = 2147483648l; //2 GB
            } else {
                host = "MassMirror.com";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
//            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("action", new StringBody("upload"));
            mpEntity.addPart("folder", new StringBody(""));
            mpEntity.addPart("ajax", new StringBody("ajax"));
            mpEntity.addPart("file", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into MassMirror.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            jSonObject = new JSONObject(responseString);
            
            //Read the links
            gettingLink();
            downloadlink = jSonObject.getString("url");
            //FileUtils.saveInFile("MassMirror.html", responseString);
            
            downloadlink = "http://www.massmirror.com/" + downloadlink;
            
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
    
}
