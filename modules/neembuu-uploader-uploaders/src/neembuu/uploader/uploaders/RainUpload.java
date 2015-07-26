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
import neembuu.uploader.accounts.RainUploadAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
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
import java.util.Random;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={RainUpload.class,RainUploadAccount.class},
    interfaces={Uploader.class,Account.class},
    name="RainUpload.net",
    ignore = true
)
public class RainUpload extends AbstractUploader{
    
    RainUploadAccount rainUploadAccount = (RainUploadAccount) getAccountsProvider().getAccount("RainUpload.net");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    private String srv_tmp_url = "";
    private String uploadid_s = "";
    private String upload_fn = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public RainUpload() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "RainUpload.net";
        if (rainUploadAccount.loginsuccessful) {
            host = rainUploadAccount.username + " | RainUpload.net";
        }
        maxFileSizeLimit = 5767168000L; // 5,500 MB (default)
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://rainupload.net", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form").first().attr("action");
        srv_tmp_url = doc.select("form").first().select("input[name=srv_tmp_url]").attr("value");
        sessionID = doc.select("form").first().select("input[name=sess_id]").attr("value");
    }

    @Override
    public void run() {
        try {
            if (rainUploadAccount.loginsuccessful) {
                userType = "reg";
                httpContext = rainUploadAccount.getHttpContext();
                maxFileSizeLimit = 5767168000L; // 5,500 MB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 10485760; // 10 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            long uploadID;
            Random random = new Random();
            uploadID = Math.round(random.nextFloat() * Math.pow(10,12));
            uploadid_s = String.valueOf(uploadID);

            uploadURL += uploadid_s + "&js_on=1&utype=" + userType + "&upload_type=file";
            // http://77.245.68.66/cgi-bin/upload.cgi?upload_id=086622575442&js_on=1&utype=reg&upload_type=file
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("upload_type", new StringBody("file"));
            mpEntity.addPart("sess_id", new StringBody(sessionID));
            mpEntity.addPart("srv_tmp_url", new StringBody(srv_tmp_url));
            mpEntity.addPart("file_0", createMonitoredFileBody());
            mpEntity.addPart("file_0_descr", new StringBody("Uploaded via NeembuuUploader"));
            mpEntity.addPart("submit_btn", new StringBody("Start Upload"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into RainUpload.net");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            upload_fn = StringUtils.stringBetweenTwoStrings(responseString, "'fn'>", "<");
            if (upload_fn != null) {
                httpPost = new NUHttpPost("http://rainupload.net/");
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("fn", upload_fn));
                formparams.add(new BasicNameValuePair("op", "upload_result"));
                formparams.add(new BasicNameValuePair("st", "OK"));
                
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                httpPost.setEntity(entity);
                httpResponse = httpclient.execute(httpPost, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());
                
                doc = Jsoup.parse(responseString);
                
                downloadlink = doc.select("textarea").eq(0).val();
                deletelink = doc.select("textarea").eq(3).val();
                
                NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
                NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
                downURL = downloadlink;
                delURL = deletelink;
                
                uploadFinished();
            }
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
    
}
