/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import neembuu.uploader.accounts.TwoSharedAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
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
 * @author Dinesh
 */
@SmallModule(
    exports={TwoShared.class,TwoSharedAccount.class},
    interfaces={Uploader.class,Account.class},
    name="2Shared.com"
)
public class TwoShared extends AbstractUploader implements UploaderAccountNecessary {

    TwoSharedAccount twoSharedAccount = (TwoSharedAccount) getAccountsProvider().getAccount("2Shared.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;
    private Document doc;
    
    private String postURL;
    private String uploadID;
    private String downloadlink;
    private String adminURL;
    private long fileSizeLimit = 209715200l; //200 MB

    public TwoShared() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        //It has to be successful.. as it won't work without login
        if (twoSharedAccount.loginsuccessful) {
            host = twoSharedAccount.username + " | 2Shared.com";
        }
    }

    public void getPostURL() throws Exception {
        //Parsing 2Shared.com page to get dynamic upload url
        NULogger.getLogger().info("Getting File upload URL");
        httpGet = new NUHttpGet("http://www.2shared.com");
        httpGet.setHeader("Cookie", TwoSharedAccount.getCookies());
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        doc = Jsoup.parse(stringResponse);
        postURL = doc.select("div table#overall tbody tr td form.body").first().attr("action");
        
        NULogger.getLogger().log(Level.INFO, "postURL: {0}", postURL);
        uploadID = postURL.substring(postURL.indexOf("sId="));
        NULogger.getLogger().log(Level.INFO, "uploadID: {0}", uploadID);
    }

    public void fileUpload() throws Exception {
        httpPost = new NUHttpPost(postURL);
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("fff", createMonitoredFileBody());
        httpPost.setEntity(reqEntity);
        NULogger.getLogger().info("Now uploading your file into 2shared.com. Please wait......................");
        uploading();
        httpResponse = httpclient.execute(httpPost);
        gettingLink();
        HttpEntity resEntity = httpResponse.getEntity();

        if (resEntity != null) {
            String page = EntityUtils.toString(resEntity);
            NULogger.getLogger().log(Level.INFO, "PAGE :{0}", page);
        }
    }

    @Override
    public void run() {
        try {


            if (twoSharedAccount.loginsuccessful) {
                host = twoSharedAccount.username + " | 2shared.com";
            } else {
                host = "2Shared.com";

                uploadInvalid();
                return;
            }


            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), twoSharedAccount.getHOSTNAME());
            }
            
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            uploadInitialising();
            getPostURL();
            fileUpload();
            NULogger.getLogger().info("Getting download URL....");
            
            httpGet = new NUHttpGet("http://www.2shared.com/uploadComplete.jsp?" + uploadID);
            httpGet.setHeader("Cookie", TwoSharedAccount.getCookies());
            httpResponse = httpclient.execute(httpGet, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());

            NULogger.getLogger().info("Upload complete. Please wait......................");
            //Parsing 2Shared.com page to get download & delete link
            
            //FileUtils.saveInFile("TwoShared.html", stringResponse);
            
            doc = Jsoup.parse(stringResponse);
            downloadlink = doc.select("table#overall tbody tr td form table tbody tr td.bodytitle textarea#downloadLink").text();
            adminURL = doc.select("table#overall tbody tr td form table tbody tr td.bodytitle textarea#adminLink").text();
            
            NULogger.getLogger().log(Level.INFO, "File download  link : {0}", downloadlink);
            NULogger.getLogger().log(Level.INFO, "File adminstration link : {0}", adminURL);
            downURL = downloadlink;
            delURL = adminURL;

            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception ex) {

            NULogger.getLogger().severe(ex.toString());
            uploadFailed();
        }

    }
}
