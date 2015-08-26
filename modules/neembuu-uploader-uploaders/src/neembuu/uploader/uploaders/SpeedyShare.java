/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import neembuu.uploader.accounts.SpeedyShareAccount;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
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
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author vigneshwaran
 */
@SmallModule(
    exports={SpeedyShare.class,SpeedyShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="SpeedyShare.com"
)
public class SpeedyShare extends AbstractUploader {
    SpeedyShareAccount speedyShareAccount = (SpeedyShareAccount) getAccountsProvider().getAccount("SpeedyShare.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;

    private String server_url_partial = "";
    private String userType;
    private String uploadURL;
    private String uid;
    private final long fileSizeLimit = Long.MAX_VALUE; // Unlimited

    public SpeedyShare() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "SpeedyShare.com";
        if (speedyShareAccount.loginsuccessful) {
            host = speedyShareAccount.username + " | SpeedyShare.com";
        }
        maxFileSizeLimit = fileSizeLimit; // Unlimited
    }

    @Override
    public void run() {
        //Checking once again as user may disable account while this upload thread is waiting in queue
        try {
            if (speedyShareAccount.loginsuccessful) {
                userType = "reg";
                httpContext = speedyShareAccount.getHttpContext();
                maxFileSizeLimit = fileSizeLimit; // Unlimited
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = fileSizeLimit; // Unlimited
            }
            
            //-------------------------------------------------------------
            uploadInitialising();
            initialize();
            //-------------------------------------------------------------

            //-------------------------------------------------------------
            uploading();
            fileUpload();
            //-------------------------------------------------------------

            //-------------------------------------------------------------
            gettingLink();
            getDownloadLinks();
            //-------------------------------------------------------------

            NULogger.getLogger().log(Level.INFO, "Download Link: {0}", downURL);
            NULogger.getLogger().log(Level.INFO, "Delete link: {0}", delURL);

            uploadFinished();
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.SEVERE, "SpeedyShare upload failed: {0} ", ex);

            uploadFailed();
        }
    }

    private void initialize() throws Exception {
        NULogger.getLogger().info("Getting upload url from speedyshare.com");

        httpGet = new NUHttpGet("http://www.speedyshare.com/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        if (userType.equals("reg")) {
            server_url_partial = StringUtils.stringBetweenTwoStrings(stringResponse, "src=\"", "speedyshare.com/upload_page.php");
        }
        else if (userType.equals("anon")){
            server_url_partial="http://www.";
        }
        
        httpGet = new NUHttpGet("http://www.speedyshare.com/upload_page.php");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        uid = parseResponse(stringResponse, "/upload_fileapi.php?", "\"");

        //http://www18.speedyshare.com/upload_fileapi.php?1923653624=0&file=0&startpos=0&r=0.7389458813972893
        uploadURL = server_url_partial + "speedyshare.com/upload_fileapi.php?" + uid + "&file=0&startpos=0&r=" + Math.random();
    }

    private void fileUpload() throws IOException {
        httpPost = new NUHttpPost(uploadURL);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("upload", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
        NULogger.getLogger().info("Now uploading your file into speedyshare.com");
        httpResponse = httpclient.execute(httpPost, httpContext);
        NULogger.getLogger().log(Level.INFO, "Upload response : {0}", EntityUtils.toString(httpResponse.getEntity()));
        NULogger.getLogger().info(httpResponse.getStatusLine().toString());
    }

    private String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private void getDownloadLinks() throws Exception {
        //http://www21.speedyshare.com/upload_fileapi.php?8614749610=0
        httpPost = new NUHttpPost(uploadURL.substring(0, uploadURL.indexOf("&file")));
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("do", "getlinks"));
        formparams.add(new BasicNameValuePair("name[0]", file.getName()));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        String[] respArr = stringResponse.split(":");
        String dnlnk = respArr[0];
        delURL = respArr[1];
        String flnm = respArr[2].substring(0, respArr[2].length() - 1);
        downURL = "http://speedy.sh/" + dnlnk + "/" + flnm;
    }
}
