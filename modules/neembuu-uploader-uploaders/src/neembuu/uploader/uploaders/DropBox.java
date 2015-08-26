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
import neembuu.uploader.accounts.DropBoxAccount;
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
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={DropBox.class,DropBoxAccount.class},
    interfaces={Uploader.class,Account.class},
    name="DropBox.com"
)
public class DropBox extends AbstractUploader implements UploaderAccountNecessary {
    
    DropBoxAccount dropBoxAccount = (DropBoxAccount) getAccountsProvider().getAccount("DropBox.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String db_t_val = "";
    private String db_userid = "";
    private String upload_time = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public DropBox() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "DropBox.com";
        if (dropBoxAccount.loginsuccessful) {
            host = dropBoxAccount.username + " | DropBox.com";
        }
        maxFileSizeLimit = 314572800L; // 300 MB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("https://www.dropbox.com/home", httpContext);
        
        doc = Jsoup.parse(responseString);
        db_t_val = doc.select("form[id=basic-upload-form]").select("input[name=t]").attr("value");
        db_userid = doc.select("form[id=basic-upload-form]").select("input[name=_subject_uid]").attr("value");
    }

    @Override
    public void run() {
        try {
            if (dropBoxAccount.loginsuccessful) {
                httpContext = dropBoxAccount.getHttpContext();
                maxFileSizeLimit = 314572800L; // 300 MB
            } else {
                host = "DropBox.com";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            long unixTime = System.currentTimeMillis() / 1000L;
            upload_time = String.valueOf(unixTime);

            uploadURL = "https://dl-web.dropbox.com/upload";
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("t", new StringBody(db_t_val));
            mpEntity.addPart("_subject_uid", new StringBody(db_userid));
            mpEntity.addPart("plain", new StringBody("yes"));
            mpEntity.addPart("dest", new StringBody(""));
            mpEntity.addPart("file", createMonitoredFileBody());
            mpEntity.addPart("mtime_utc", new StringBody(upload_time));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into DropBox.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();
            uploadURL = "https://www.dropbox.com/sm/share_link/" + file.getName();
            httpPost = new NUHttpPost(uploadURL);
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("_subject_uid", db_userid));
            formparams.add(new BasicNameValuePair("is_xhr", "true"));
            formparams.add(new BasicNameValuePair("origin", "browse_actions_basic_token_share"));
            formparams.add(new BasicNameValuePair("t", db_t_val));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //FileUtils.saveInFile("DropBox.html", responseString);
            
            doc = Jsoup.parse(responseString);
            responseString = responseString.replaceAll("\\\\", "");
            
            downloadlink = "https://www.dropbox.com/s/" + StringUtils.stringBetweenTwoStrings(responseString, "value=\"https://www.dropbox.com/s/", "\"");
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
