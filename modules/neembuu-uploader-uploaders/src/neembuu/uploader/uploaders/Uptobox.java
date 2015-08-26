/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.UptoboxAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.CookieUtils;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={Uptobox.class,UptoboxAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Uptobox.com"
)
public class Uptobox extends AbstractUploader{
    
    UptoboxAccount uptoboxAccount = (UptoboxAccount) getAccountsProvider().getAccount("Uptobox.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private Document doc;
    private String uploadURL;
    private final String uploadType = "file";
    private String userType;
    private String sessionID = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public Uptobox() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Uptobox.com";
        if (uptoboxAccount.loginsuccessful) {
            host = uptoboxAccount.username + " | Uptobox.com";
        }
        maxFileSizeLimit = 1073741824l; //1024 MB (default)
        
    }

    private void initialize() throws Exception {
        String uid;
        String jsOn = "1";
        
        stringResponse = NUHttpClientUtils.getData("http://uptobox.com/", httpContext);
        
        doc = Jsoup.parse(stringResponse);
        uploadURL = doc.select("#div_file form").first().attr("action");
        uid = StringUtils.uuid(12, 10);
        
        uploadURL += uid + "&js_on=" + jsOn + "&utype=" + userType + "&upload_type=" + uploadType;
    }

    @Override
    public void run() {
        String fn;
        try {
            if (uptoboxAccount.loginsuccessful) {
                userType = "reg";
                httpContext = uptoboxAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "xfss");
                if(uptoboxAccount.isPremium()){
                    maxFileSizeLimit = 32212254720l; //30720 MB
                }
                else{
                    maxFileSizeLimit = 5368709120l; //5120 MB
                }
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 1073741824l; //1024 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), uptoboxAccount.getHOSTNAME());
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("upload_type", new StringBody("file"));
            mpEntity.addPart("sess_id", new StringBody(sessionID));
            mpEntity.addPart("srv_tmp_url", new StringBody("http://www20.uptobox.com/tmp"));
            
            mpEntity.addPart("file_1", new StringBody(""));
            mpEntity.addPart("file_0_descr", new StringBody(""));
            mpEntity.addPart("tos", new StringBody("1"));
            mpEntity.addPart("file_0", createMonitoredFileBody());
            mpEntity.addPart("submit_btn", new StringBody(""));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into uptobox.com");
            uploading();
            HttpResponse response = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(response.getEntity());
            
            doc = Jsoup.parse(stringResponse);
            fn = doc.select("textarea").first().val();
            
            //Read the links
            httpPost = new NUHttpPost("http://uptobox.com/");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("fn", fn));
            formparams.add(new BasicNameValuePair("op", "upload_result"));
            formparams.add(new BasicNameValuePair("st", "OK"));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            gettingLink();
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            
            //FileUtils.saveInFile("Uptobox.com.html", stringResponse);
            
            doc = Jsoup.parse(stringResponse);
            downloadlink = doc.select("input").first().val();
            deletelink = doc.select("input").eq(3).val();
            
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
