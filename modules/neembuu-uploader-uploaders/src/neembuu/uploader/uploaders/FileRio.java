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
import neembuu.uploader.accounts.FileRioAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
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
    exports={FileRio.class,FileRioAccount.class},
    interfaces={Uploader.class,Account.class},
    name="FileRio.in"
)
public class FileRio extends AbstractUploader{
    
    FileRioAccount fileRioAccount = (FileRioAccount) getAccountsProvider().getAccount("FileRio.in");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public FileRio() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "FileRio.in";
        if (fileRioAccount.loginsuccessful) {
            host = fileRioAccount.username + " | FileRio.in";
        }
        maxFileSizeLimit = 10485760000l; //10000 MB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://filerio.in", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form.upload_file_form").first().attr("action");
        String uid = StringUtils.uuid(12, 10);
        
        uploadURL += "?upload_id=" + uid + "&js_on=1" + "&utype=" + userType + "&upload_type=file";
    }

    @Override
    public void run() {
        try {
            if (fileRioAccount.loginsuccessful) {
                userType = "reg";
                httpContext = fileRioAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "xfss");
                maxFileSizeLimit = 10485760000l; //10000 MB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 10485760000l; //10000 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("sess_id", new StringBody(sessionID));
            mpEntity.addPart("upload_type", new StringBody("file"));
            mpEntity.addPart("tos", new StringBody("1"));
            mpEntity.addPart("file_0", createMonitoredFileBody());
            mpEntity.addPart("submit_btn", new StringBody("Upload!"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into FileRio.in");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            final String fn = doc.select("textarea[name=fn]").val();
            
            //Read the links
            gettingLink();
            httpPost = new NUHttpPost("http://filerio.in/");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("fn", fn));
            formparams.add(new BasicNameValuePair("op", "upload_result"));
            formparams.add(new BasicNameValuePair("st", "OK"));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //FileUtils.saveInFile("FileRio.html", responseString);
            
            doc = Jsoup.parse(responseString);
            downloadlink = doc.select("textarea").first().val();
            deletelink = doc.select("textarea").eq(3).val();
            
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
 
