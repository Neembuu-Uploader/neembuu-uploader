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
import neembuu.uploader.accounts.EdiskCzAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
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
 * @author Paralytic
 */
@SmallModule(
    exports={EdiskCz.class,EdiskCzAccount.class},
    interfaces={Uploader.class,Account.class},
    name="EDisk.cz"
)
public class EdiskCz extends AbstractUploader{
    
    EdiskCzAccount ediskCzAccount = (EdiskCzAccount) getAccountsProvider().getAccount("EDisk.cz");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String upload_host = "";
    private String upload_hash = "";
    private String upload_id = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public EdiskCz() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "EDisk.cz";
        if (ediskCzAccount.loginsuccessful) {
            host = ediskCzAccount.username + " | EDisk.cz";
        }
        maxFileSizeLimit = 3145728000L; // 3,000 MB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://www.edisk.cz/en/upload", httpContext);
        uploadURL = "http://www.edisk.cz/upload-get-link-multi";
        upload_host = StringUtils.stringBetweenTwoStrings(responseString, "var uploadHost = '", "'");
        upload_hash = StringUtils.stringBetweenTwoStrings(responseString, "var uploadHash = '", "'");
    }

    @Override
    public void run() {
        try {
            if (ediskCzAccount.loginsuccessful) {
                httpContext = ediskCzAccount.getHttpContext();
                maxFileSizeLimit = 3145728000L; // 3,000 MB
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 3145728000L; // 3,000 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            httpPost = new NUHttpPost(uploadURL);
            httpPost.setHeader("Referer", "http://www.edisk.cz/en/upload");
            httpPost.setHeader("Host", "www.edisk.cz");
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("locale", new StringBody("en"));
            mpEntity.addPart("uploadHash", new StringBody(upload_hash));
            mpEntity.addPart("uploadHost", new StringBody(upload_host));
            mpEntity.addPart("upload_file[]", new StringBody(file.getName()));
            httpPost.setEntity(mpEntity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            
            // ------------- NORMAL RESPONSE: -------------
            // if(typeof UberUpload.startUpload == 'function'){ UberUpload.startUpload("6e0d8b9dccc163027e967ea1c0aeda14",0,0); }
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            // http://data8.edisk.cz/cgi-bin/ubr_upload.cgi?upload_id=3edea552cbb86e692e7ed55ec8d65cff
            upload_id = StringUtils.stringBetweenTwoStrings(responseString, "\"", "\"");
            uploadURL = "http://" + upload_host + ".edisk.cz/cgi-bin/ubr_upload.cgi?upload_id=" + upload_id;
            
            long unixTime = System.currentTimeMillis() / 1000L;
            String upfile_time = "upfile_" + unixTime;
            
            httpPost = new NUHttpPost(uploadURL);
            httpPost.setHeader("Referer", "http://www.edisk.cz/en/upload");
            mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("locale", new StringBody("en"));
            mpEntity.addPart(upfile_time, createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into EDisk.cz");
            uploading();
            
            httpResponse = httpclient.execute(httpPost, httpContext);
            // ------------- NORMAL RESPONSE: -------------
            // <script language="javascript" type="text/javascript">
            // </script>
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            // http://data8.edisk.cz/en/verify-files-upload/3edea552cbb86e692e7ed55ec8d65cff
            uploadURL = "http://" + upload_host + ".edisk.cz/en/verify-files-upload/" + upload_id;
            responseString = NUHttpClientUtils.getData(uploadURL, httpContext);
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();
            downloadlink = doc.select("#plainLink_1").first().attr("value");
            deletelink = doc.select("#deleteLink_1").first().attr("value");
            
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
