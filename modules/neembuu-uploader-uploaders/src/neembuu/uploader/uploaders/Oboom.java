/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.nio.charset.Charset;
import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.OboomAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author MNidhal
 */
@SmallModule(
    exports={Oboom.class,OboomAccount.class},
    interfaces={Uploader.class,Account.class},
    name="obOom.com"
)
public class Oboom extends AbstractUploader implements UploaderAccountNecessary{
    
    OboomAccount oboomAccount = (OboomAccount) getAccountsProvider().getAccount("Oboom.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    private String token;
    
    private String downloadlink = "";
    private String deletelink = "";

    public Oboom() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "obOom.com";
        if (oboomAccount.loginsuccessful) {
            host = oboomAccount.username + " | obOom.com";
        }
        maxFileSizeLimit = 5368709120l; //5 GB
        
    }

    private void initialize() throws Exception {
        httpPost = new NUHttpPost("http://www.oboom.com/1.1/token");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("api_key", oboomAccount.api_key));
            
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        responseString = EntityUtils.toString(httpResponse.getEntity());
        token = StringUtils.stringBetweenTwoStrings(responseString, "200,\"", "\"]");
        //doc = Jsoup.parse(responseString);
        uploadURL = "https://upload.oboom.com/1.0/ul";
        uploadURL += "?token=" + token + "&parent=1&name_policy=rename";
    }

    @Override
    public void run() {
        try {
            if (oboomAccount.loginsuccessful) {
                userType = "reg";
                httpContext = oboomAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "user");
                maxFileSizeLimit = 5368709120l; //5 GB
            } else {
                host = "oboom.com";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("token", new StringBody(token));
            mpEntity.addPart("file", createMonitoredFileBody());
            mpEntity.addPart("parent", new StringBody("1"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into obOom.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            
            //file.getName()
            String fid = StringUtils.stringBetweenTwoStrings(responseString, "\"id\":\"", "\",");
            String dlink = "http://www.oboom.com/"+fid+"/"+file.getName();
            downloadlink = dlink;
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
