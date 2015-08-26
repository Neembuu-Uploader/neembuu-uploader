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
import neembuu.uploader.accounts.FileOmAccount;
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
    exports={FileOm.class,FileOmAccount.class},
    interfaces={Uploader.class,Account.class},
    name="FileOM.com",
    ignore = true
)
public class FileOm extends AbstractUploader{
    
    FileOmAccount fileOmAccount = (FileOmAccount) getAccountsProvider().getAccount("FileOM.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    
    private String uploadURL;
    private String uploadType;
    private String srvTmpUrl;
    private String uid;
    private String userType;
    private String sessionID = "";
    
    private final String UPLOAD_URL_FORMAT = "%s%s&js_on=1&utype=%s&upload_type=%s";
    
    private String downloadlink = "";
    private String deletelink = "";

    public FileOm() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "FileOM.com";
        if (fileOmAccount.loginsuccessful) {
            host = fileOmAccount.username + " | FileOM.com";
        }
        maxFileSizeLimit = 2202009600l; //2100 MB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://fileom.com/", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("div#upload_div div#div_file.upload_block form").first().attr("action");
        uploadType = doc.select("div#upload_div div#div_file.upload_block form input:eq(0)").val();
        srvTmpUrl = doc.select("div#upload_div div#div_file.upload_block form input:eq(2)").val();
        uid = StringUtils.uuid(12, 10);
        uploadURL = String.format(UPLOAD_URL_FORMAT, uploadURL, uid, userType, uploadType);
    }

    @Override
    public void run() {
        try {
            if (fileOmAccount.loginsuccessful) {
                userType = "reg";
                httpContext = fileOmAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "xfss");
                maxFileSizeLimit = 10737418240l; //10240 MB
            } else {
                userType = "anon";
                sessionID = "";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 2202009600l; //2100 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), fileOmAccount.getHOSTNAME());
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("upload_type", new StringBody(uploadType));
            mpEntity.addPart("sess_id", new StringBody(sessionID));
            mpEntity.addPart("srv_tmp_url", new StringBody(srvTmpUrl));
            mpEntity.addPart("file_1", new StringBody(""));
            mpEntity.addPart("file_0_descr", new StringBody(""));
            mpEntity.addPart("tos", new StringBody("1"));
            mpEntity.addPart("file_0", createMonitoredFileBody());
            mpEntity.addPart("submit_btn", new StringBody(""));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into FileOM.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            String fn = doc.select("form textarea").first().text();
            
            //Read the links
            gettingLink();
            httpPost = new NUHttpPost("http://fileom.com/");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("fn", fn));
            formparams.add(new BasicNameValuePair("op", "upload_result"));
            formparams.add(new BasicNameValuePair("st", "OK"));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //FileUtils.saveInFile("FileOm.html", responseString);
            
            doc = Jsoup.parse(responseString);
            downloadlink = doc.select("div.tabbertab textarea").first().val();
            deletelink = doc.select("div.tabbertab textarea").eq(4).val();
            
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
 
