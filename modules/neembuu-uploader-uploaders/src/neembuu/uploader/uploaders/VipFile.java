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
import neembuu.uploader.accounts.VipFileAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
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
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Random;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={VipFile.class,VipFileAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Vip-File.com"
)
public class VipFile extends AbstractUploader implements UploaderAccountNecessary{
    
    VipFileAccount vipFileAccount = (VipFileAccount) getAccountsProvider().getAccount("Vip-File.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private String uploadURL;
    private String vip_pin = "";
    private String vip_base = "";
    private String vip_host = "";
    private String vip_acupl_UID = "";
    private String dl_id = "";
    
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();
    
    private String downloadlink = "";
    private String deletelink = "";

    public VipFile() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Vip-File.com";
        if (vipFileAccount.loginsuccessful) {
            host = vipFileAccount.username + " | Vip-File.com";
        }
        maxFileSizeLimit = 2147483647L; // 2047.999 MB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://vip-file.com", httpContext);
        vip_pin = StringUtils.stringBetweenTwoStrings(responseString, "name=\"pin\" value=\"", "\"");
        vip_base = StringUtils.stringBetweenTwoStrings(responseString, "name=\"base\" value=\"", "\"");
        vip_host = StringUtils.stringBetweenTwoStrings(responseString, "name=\"host\" value=\"", "\"");
        
        uploadURL = "http://" + StringUtils.stringBetweenTwoStrings(responseString, "var ACUPL_UPLOAD_SERVER = '", "'");
        vip_acupl_UID = (new Date()).getTime() + "_" + randomString(40);
        uploadURL += "/marker=" + vip_acupl_UID + "?r=" + Math.random();
    }

    @Override
    public void run() {
        try {
            if (vipFileAccount.loginsuccessful) {
                httpContext = vipFileAccount.getHttpContext();
                maxFileSizeLimit = 2147483647L; // 2047.999 MB
            } else {
                host = "Vip-File.com";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            URL url = new URL(uploadURL);
            URI uri = url.toURI();
            httpPost = new NUHttpPost(uri);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("MAX_FILE_SIZE", new StringBody("2147483647"));
            mpEntity.addPart("owner", new StringBody(vipFileAccount.username));
            mpEntity.addPart("pin", new StringBody(vip_pin));
            mpEntity.addPart("base", new StringBody(vip_base));
            mpEntity.addPart("host", new StringBody(vip_host));
            mpEntity.addPart("file0", createMonitoredFileBody());
            mpEntity.addPart("rules_accepted", new StringBody("1"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into Vip-File.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            // {"code": 200, "uids": ["98212.96fccd74e5a7a150f5f4d26d5bca"]}
            dl_id = StringUtils.stringBetweenTwoStrings(responseString, "\"uids\": ", "}");
            dl_id = StringUtils.stringBetweenTwoStrings(dl_id, "\"", "\"");
            
            downloadlink = "http://vip-file.com/download/" + dl_id + "/" + file.getName() + ".html";
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

    String randomString( int len ) 
    {
       StringBuilder sb = new StringBuilder( len );
       for( int i = 0; i < len; i++ ) 
          sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
       return sb.toString();
    }    
}
