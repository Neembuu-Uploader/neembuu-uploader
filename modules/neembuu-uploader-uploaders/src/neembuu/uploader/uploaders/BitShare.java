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
import neembuu.uploader.accounts.BitShareAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.uploaders.common.FormBodyPartUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={BitShare.class,BitShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="BitShare.com"
)
public class BitShare extends AbstractUploader{
    
    BitShareAccount bitShareAccount = (BitShareAccount) getAccountsProvider().getAccount("BitShare.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String progressKey = "";
    private String userGroupKey = "";
    private String uploadIdentifier = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public BitShare() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "BitShare.com";
        if (bitShareAccount.loginsuccessful) {
            host = bitShareAccount.username + " | BitShare.com";
        }
        maxFileSizeLimit = 1073741824l; //1024 MB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://bitshare.com", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("#uploadform").first().attr("action");
        uploadURL += "?X-Progress-ID=undefined" + CommonUploaderTasks.createRandomString(32);
        
        progressKey = doc.select("#progress_key").first().val();
        userGroupKey = doc.select("#usergroup_key").first().val();
        uploadIdentifier = doc.select("input[name=UPLOAD_IDENTIFIER]").first().val();
    }

    @Override
    public void run() {
        try {
            if (bitShareAccount.loginsuccessful) {
                httpContext = bitShareAccount.getHttpContext();
                
                if(bitShareAccount.isPremium()){
                    maxFileSizeLimit = 1073741824l; //1024 MB
                }
                else{
                    maxFileSizeLimit = 2147483648l; //2048 MB
                }
                
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 1073741824l; //1024 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("APC_UPLOAD_PROGRESS", new StringBody(progressKey));
            mpEntity.addPart("APC_UPLOAD_USERGROUP", new StringBody(userGroupKey));
            mpEntity.addPart("UPLOAD_IDENTIFIER", new StringBody(uploadIdentifier));
            FormBodyPart customBodyPart = FormBodyPartUtils.createEmptyFileFormBodyPart("file[]", new StringBody(""));
            mpEntity.addPart(customBodyPart);
            mpEntity.addPart("file[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into BitShare.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            final String location = httpResponse.getFirstHeader("Location").getValue();
            responseString = EntityUtils.toString(httpResponse.getEntity());
            responseString = NUHttpClientUtils.getData(location, httpContext);
 
            //Read the links
            gettingLink();
            //FileUtils.saveInFile("BitShare.html", responseString);
            
            doc = Jsoup.parse(responseString);
            downloadlink = doc.select("#filedetails input[type=text]").eq(1).val();
            deletelink = doc.select("#filedetails input[type=text]").eq(4).val();
            
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
