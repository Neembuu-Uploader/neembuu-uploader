/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
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
    exports={FreeFr.class},
    interfaces={Uploader.class},
    name="dl.free.fr"
)
public class FreeFr extends AbstractUploader{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    
    private String downloadlink = "";
    private String deletelink = "";

    public FreeFr() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "dl.free.fr";
        maxFileSizeLimit = 1073741824L; // 1 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://dl.free.fr/", httpContext);
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form").eq(1).attr("action");
    }

    @Override
    public void run() {
        try {
            userType = "anon";
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            maxFileSizeLimit = 1073741824L; // 1 GB

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            
            uploadInitialising();
            initialize();

            // /upload.pl?a08421676692470426803629975451717
            // http://dl.free.fr/upload.pl?a08421676692470426803629975451717
            uploadURL = "http://dl.free.fr" + uploadURL;
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("ufile", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().info("** DL.FREE.FR ** => Now uploading your file to the server ...");
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            uploadURL = httpResponse.getLastHeader("Location").getValue();
            
            //Read the links
            gettingLink();
            
            boolean linkfound = false;
            int i=1;
            
            do {
                NULogger.getLogger().log(Level.INFO, "** DL.FREE.FR ** => {0}/20 attempt to get the download link ...", i);
                responseString = NUHttpClientUtils.getData(uploadURL, httpContext);
                doc = Jsoup.parse(responseString);
                try {
                    downloadlink = doc.select("a[class=underline]").first().attr("href");
                } catch (NullPointerException ex) {
                    NULogger.getLogger().info("** DL.FREE.FR ** => Download link not found! The host is still processing the file ... Waiting for 5 seconds before retrying.");
                    linkfound = false;
                    Thread.sleep(5000);
                }
                i++;
            } while (i<=20 && downloadlink.isEmpty());
            
            if (!downloadlink.isEmpty()){
                linkfound = true;
            }
            
            if (linkfound){
                deletelink = doc.select("a[class=underline]").eq(1).attr("href");
            } else {
                NULogger.getLogger().info("** DL.FREE.FR ** => Upload has failed ... download link not found!");
                uploadFailed();
            }
            
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
    public static void main(String[] args) {
        neembuu.uploader.paralytics_tests.GenericPluginTester.test(
                FreeFr.class,
                neembuu.uploader.accounts.GoFourUpAccount.class
        );
    }
}
