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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.ShareOnlineAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
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

/**
 * <a href="http://www.share-online.biz/uploadapi/">API</a>
 * @author davidepastore
 */
@SmallModule(
    exports={ShareOnline.class,ShareOnlineAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Share-Online.biz"
)
public class ShareOnline extends AbstractUploader{
    
    private ShareOnlineAccount shareOnlineAccount = (ShareOnlineAccount) getAccountsProvider().getAccount("Share-Online.biz");
    private String username = "guest";
    private String password = "guest";
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private String sessionName;
    private String uploadServer;
    
    
    private String downloadlink = "";
    private long fileSizeLimit;

    public ShareOnline() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Share-Online.biz";
        if (shareOnlineAccount.loginsuccessful) {
            host = shareOnlineAccount.username + " | Share-Online.biz";
        }
        
    }

    private void initialize() throws Exception {
        createUploadSession();
        //stringResponse = NUHttpClientUtils.getData("http://www.share-online.biz", httpContext);
    }
    
    /**
     * Creates upload session.
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    private void createUploadSession() throws UnsupportedEncodingException, IOException{
        httpPost = new NUHttpPost("http://www.share-online.biz/upv3_session.php");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("username", username));
        formparams.add(new BasicNameValuePair("password", password));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        String[] splitted = stringResponse.split(";");
        sessionName = splitted[0];
        uploadServer = "http://" + splitted[1];
        
        //NULogger.getLogger().log(Level.INFO, "uploadServer: {0}", uploadServer);
    }

    @Override
    public void run() {
        try {
            if (shareOnlineAccount.loginsuccessful) {
                httpContext = shareOnlineAccount.getHttpContext();
                username = shareOnlineAccount.getUsername();
                password = shareOnlineAccount.getPassword();
                fileSizeLimit = 2147483648l; //2048 MB
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                fileSizeLimit = 1073741824l; //1024 MB
            }

            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), shareOnlineAccount.getHOSTNAME());
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadServer);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("username", new StringBody(username));
            mpEntity.addPart("password", new StringBody(password));
            mpEntity.addPart("upload_session", new StringBody(sessionName));
            
            mpEntity.addPart("chunk_no", new StringBody("1"));
            mpEntity.addPart("chunk_number", new StringBody("1"));
            mpEntity.addPart("filesize", new StringBody(Long.toString(file.length())));
            mpEntity.addPart("fn", createMonitoredFileBody());
            mpEntity.addPart("finalize", new StringBody("1"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into share-online.biz");
            uploading();
            HttpResponse response = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(response.getEntity());
            
            String[] splitted = stringResponse.split(";");
            if(splitted[0].contains("*** EXCEPTION")){
                throw new Exception(splitted[0]);
            }
            downloadlink = splitted[0];
            
            //FileUtils.saveInFile("Share-Online.biz.html", stringResponse);
            //NULogger.getLogger().log(Level.INFO, "Response: {0}", stringResponse);
            
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            downURL = downloadlink;
            delURL = UploadStatus.NA.getLocaleSpecificString();
            
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
