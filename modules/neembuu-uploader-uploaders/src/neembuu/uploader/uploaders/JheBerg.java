/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.JheBergAccount;
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
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
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
    exports={JheBerg.class,JheBergAccount.class},
    interfaces={Uploader.class,Account.class},
    name="JheBerg.net"
)
public class JheBerg extends AbstractUploader{
    
    JheBergAccount jheBergAccount = (JheBergAccount) getAccountsProvider().getAccount("JheBerg.net");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String session_id = "";
    private String upload_hash = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public JheBerg() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "JheBerg.net";
        if (jheBergAccount.loginsuccessful) {
            host = jheBergAccount.username + " | JheBerg.net";
        }
        maxFileSizeLimit = 20971520000L; // 20,000 MB (default)
        
    }

    private void initialize() throws Exception {
        NULogger.getLogger().info("** JheBerg.net ** => Getting the upload server URL now ...");
        responseString = NUHttpClientUtils.getData("http://jheberg.net/api/get/server/", httpContext);
        // {"url": "http://tdjm9.jheberg.net/", "name": "tdjm9.jheberg.net"}
        
        uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "\"url\": \"", "\"");
        if (!uploadURL.isEmpty()){
            NULogger.getLogger().info("** JheBerg.net ** => Upload server URL found! Proceeding further.");
        } else {
            NULogger.getLogger().info("** JheBerg.net ** => Error! Unable to find the upload server URL, cannot proceed!");
        }
        uploadURL += "upload/";
        
        responseString = NUHttpClientUtils.getData("http://jheberg.net/", httpContext);
        doc = Jsoup.parse(responseString);
        
        session_id = doc.select("form[id=fileupload]").select("input[name=session_id]").attr("value");
        upload_hash = doc.select("form[id=fileupload]").select("input[name=upload_hash]").attr("value");
    }

    @Override
    public void run() {
        try {
            if (jheBergAccount.loginsuccessful) {
                userType = "reg";
                httpContext = jheBergAccount.getHttpContext();
                maxFileSizeLimit = 20971520000L; // 20,000 MB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 20971520000L; // 20,000 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("session_id", new StringBody(session_id));
            mpEntity.addPart("upload_hash", new StringBody(upload_hash));
            mpEntity.addPart("Uploaded", new StringBody("1"));     // Uploaded.net
            mpEntity.addPart("Uptobox", new StringBody("1"));      // UptoBox.com
            mpEntity.addPart("1fichier", new StringBody("1"));     // 1Fichier.com
            mpEntity.addPart("2shared", new StringBody("1"));      // 2Shared.com
            mpEntity.addPart("Filerio", new StringBody("1"));      // FileRio.in
            mpEntity.addPart("Mediafire", new StringBody("1"));    // MediaFire.com
            mpEntity.addPart("Mega", new StringBody("1"));         // Mega.Co.Nz
            mpEntity.addPart("Rapidgator", new StringBody("1"));   // RapidGator.net
            mpEntity.addPart("Depositfiles", new StringBody("1")); // DepositFiles.com
            mpEntity.addPart("Turbobit", new StringBody("1"));     // TurboBit.net
            mpEntity.addPart("Free", new StringBody("1"));         // dl.free.fr
            mpEntity.addPart("Hugefiles", new StringBody("1"));    // HugeFiles.net
            mpEntity.addPart("Uplea", new StringBody("1"));        // Uplea.com
            mpEntity.addPart("Letitbit", new StringBody("1"));     // LetItBit.net
            mpEntity.addPart("Oboom", new StringBody("1"));        // Oboom.com
            mpEntity.addPart("Nitroflare", new StringBody("1"));   // NitroFlare.com
            mpEntity.addPart("Toutbox", new StringBody("1"));      // ToutBox.fr
            mpEntity.addPart("Ezfile", new StringBody("1"));       // EzFile.ch
            mpEntity.addPart("files[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);

            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("** JheBerg.net ** => Uploading your file to the server now!");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());

            gettingLink();
            uploadURL = "http://jheberg.net/upload/success/" +upload_hash+ "/";
            
            NULogger.getLogger().info("** JheBerg.net ** => Upload complete! Attempting to retrieve the download link ...");
            responseString = NUHttpClientUtils.getData(uploadURL, httpContext);
            doc = Jsoup.parse(responseString);
            responseString = doc.select("ul[class=list-files]").select("li").select("a").attr("href");
            
            if (!responseString.isEmpty()) {
                NULogger.getLogger().log(Level.INFO, "** JheBerg.net ** => DL link received as: {0}", responseString);
            } else {
                throw new Exception("** JheBerg.net ** => Download link not found!");
            }
            
            downloadlink = responseString;
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