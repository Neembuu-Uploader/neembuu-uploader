/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
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
import org.jsoup.select.Elements;

/**
 * 
 * @author davidepastore
 */
@SmallModule(
    exports={MegaUp.class},
    interfaces={Uploader.class},
    name="Megaup.me",
    ignore = true
)
public class MegaUp extends AbstractUploader{
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private String stringResponse;
    private Document document;
    
    private String postUrl;
    private String sessionID;
    private String accessKey;
    private String maxFileSize;
    private String phpUploadScript;
    private String returnUrl;
    private String uploadMode;
    
    public MegaUp() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "Megaup.me";
        /*
        if (megaUpAccount.loginsuccessful) {
            //  login = true;
            host = megaUpAccount.username + " | Megaup.me";
        }
        */
    }

    @Override
    public void run() {
        try {
            uploadInitialising();
            init();
            httpGet = new NUHttpGet("http://megaup.me/");
            httpResponse = httpclient.execute(httpGet, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            document = Jsoup.parse(stringResponse);
            Elements elements = document.select("#uploadwindow");
            postUrl = elements.select("#uploadform").first().attr("action");
            sessionID = elements.select("[name=sessionid]").first().val();
            accessKey = elements.select("[name=AccessKey]").first().val();
            maxFileSize = elements.select("[name=maxfilesize]").first().val();
            phpUploadScript = elements.select("[name=phpuploadscript]").first().val();
            returnUrl = elements.select("[name=returnurl]").first().val();
            uploadMode = elements.select("[name=uploadmode]").first().val();
            
            NULogger.getLogger().log(Level.INFO, "postUrl: {0}", postUrl);

            httpPost = new NUHttpPost(postUrl);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("sessionid", new StringBody(sessionID));
            mpEntity.addPart("AccessKey", new StringBody(accessKey));
            mpEntity.addPart("maxfilesize", new StringBody(maxFileSize));
            mpEntity.addPart("phpuploadscript", new StringBody(phpUploadScript));
            mpEntity.addPart("returnurl", new StringBody(returnUrl));
            mpEntity.addPart("uploadmode", new StringBody(uploadMode));
            mpEntity.addPart("phpuploadscript", new StringBody(phpUploadScript));
            mpEntity.addPart("uploadfile_0", createMonitoredFileBody());
            mpEntity.addPart("_descr", new StringBody(""));
            mpEntity.addPart("_password", new StringBody(""));
            
            httpPost.setEntity(mpEntity);
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into megaup.me");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            HttpEntity entity = httpResponse.getEntity();
            String location = httpResponse.getFirstHeader("Location").getValue();
            NULogger.getLogger().log(Level.INFO, "Location: {0}", location);
            EntityUtils.consume(entity);

            stringResponse = getRequest(location);
            stringResponse = getRequest(returnUrl + "?&redir=1");
            stringResponse = getRequest("http://megaup.me/getlinks.php?sessionid=" + sessionID + "&submitnums=1");
            
            NULogger.getLogger().log(Level.INFO, "Result:\n{0}", stringResponse);
        } catch (Exception ex) {
            Logger.getLogger(MegaUp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void init() {
        CookieStore cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }
    
    
    /**
     * Get method.
     * @param url the url
     * @return the response from the request
     * @throws IOException 
     */
    private String getRequest(String url) throws IOException{
        httpGet = new NUHttpGet(url);
        NULogger.getLogger().log(Level.INFO, "Request for: {0}", url);
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        NULogger.getLogger().log(Level.INFO, "Response for {0}: {1}", new Object[]{url, stringResponse });
        return stringResponse;
    }
    
}
