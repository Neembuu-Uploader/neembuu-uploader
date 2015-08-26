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
import neembuu.uploader.accounts.HitFileAccount;
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
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={HitFile.class,HitFileAccount.class},
    interfaces={Uploader.class,Account.class},
    name="HitFile.net"
)
public class HitFile extends AbstractUploader implements UploaderAccountNecessary{
    
    HitFileAccount hitFileAccount = (HitFileAccount) getAccountsProvider().getAccount("HitFile.net");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String flash_vars = "";
    private String user_id = "";
    private String apptype = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public HitFile() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "HitFile.net";
        if (hitFileAccount.loginsuccessful) {
            host = hitFileAccount.username + " | HitFile.net";
        }
        maxFileSizeLimit = 4294967296L; // 4 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://hitfile.net/", httpContext);
        
        doc = Jsoup.parse(responseString);
        flash_vars = doc.select("param[name=FlashVars]").attr("value");
        uploadURL = StringUtils.stringBetweenTwoStrings(flash_vars, "&urlSite=", "&");
        user_id = StringUtils.stringBetweenTwoStrings(flash_vars, "&userId=", "&");
        apptype = StringUtils.stringStartingFromString(flash_vars, "&apptype=");
    }

    @Override
    public void run() {
        try {
            if (hitFileAccount.loginsuccessful) {
                httpContext = hitFileAccount.getHttpContext();
                maxFileSizeLimit = 4294967296L; // 4 GB
            } else {
                host = "HitFile.net";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            // http://s139.turbobit.net/uploadfile
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("Filename", new StringBody(file.getName()));
            mpEntity.addPart("stype", new StringBody("null"));
            mpEntity.addPart("apptype", new StringBody(apptype));
            mpEntity.addPart("user_id", new StringBody(user_id));
            mpEntity.addPart("id", new StringBody("null"));
            mpEntity.addPart("Filedata", createMonitoredFileBody());
            mpEntity.addPart("Upload", new StringBody("Submit Query"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into HitFile.net");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();

            downloadlink = "http://hitfile.net/" + StringUtils.stringBetweenTwoStrings(responseString, "\"id\":\"", "\"");
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
    
}
