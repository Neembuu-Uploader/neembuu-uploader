/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.net.URI;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.SlingFileAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import neembuu.uploader.utils.URIUtils;
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
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author dinesh
 * @author davidepastore
 */
@SmallModule(
    exports={SlingFile.class,SlingFileAccount.class},
    interfaces={Uploader.class,Account.class},
    name="SlingFile.com",
    ignore = true
)
public class SlingFile extends AbstractUploader {

    SlingFileAccount slingFileAccount = (SlingFileAccount) getAccountsProvider().getAccount("SlingFile.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;
    private String uploadURL;
    private String progressID;
    private String rauLink;
    private URI URILink;
    private JSONObject jSonObject;
    
    private String ssd = "";
    private String encUserID = "";
    private String postURL = "";
    private String downloadlink = "";
    private String deletelink = "";
    private long fileSizeLimit = 2147483648l; //2 GB

    public SlingFile() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "SlingFile.com";
        if (slingFileAccount.loginsuccessful) {
            host = slingFileAccount.username + " | SlingFile.com";
        }
        
    }

    private void initialize() throws Exception {


        NULogger.getLogger().info("After login, geting the link again :)");
        httpGet = new NUHttpGet("http://www.slingfile.com/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        //FileUtils.saveInFile("SlingFile.com.html", stringResponse);
        
        //See here: http://www.slingfile.com/media/plupload.beauty.js
        // http://www.plupload.com/punbb/viewtopic.php?pid=5686
        jSonObject = new JSONObject(StringUtils.stringBetweenTwoStrings(stringResponse, "var uploaderSettings = ", ";"));
        
        uploadURL = jSonObject.getString("uploadURL");
        ssd = jSonObject.getString("ssd");
        
        if(jSonObject.has("encUserID")){
            encUserID = jSonObject.getString("encUserID");
        }
        
        progressID = guid();
        postURL = uploadURL + progressID;
        rauLink = StringUtils.stringBetweenTwoStrings(stringResponse, "document.location.href = '", "'");
        NULogger.getLogger().log(Level.INFO, "progressID: "+progressID);
        
        URILink = URIUtils.createURI(rauLink);
    }

    @Override
    public void run() {
        try {
            if (slingFileAccount.loginsuccessful) {
                host = slingFileAccount.username + " | SlingFile.com";
                httpContext = slingFileAccount.getHttpContext();
            } else {
                host = "SlingFile.com";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            }

            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), slingFileAccount.getHOSTNAME());
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(postURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("X-Progress-ID", new StringBody(progressID));
            mpEntity.addPart("uFileID", new StringBody(progressID));
            mpEntity.addPart("uid", new StringBody(encUserID));
            if(slingFileAccount.loginsuccessful){
                mpEntity.addPart("folderid", new StringBody("0"));
            }
            mpEntity.addPart("ssd", new StringBody(ssd));
            mpEntity.addPart("Filename", new StringBody(file.getName()));
            mpEntity.addPart("name", new StringBody(file.getName()));
            mpEntity.addPart("Upload", new StringBody("Submit Query"));
            mpEntity.addPart("file", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into slingfile.com");
            uploading();
            HttpResponse response = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(response.getEntity());
            
            if("done".equals(stringResponse)){
                NULogger.getLogger().log(Level.INFO, "upload done!");
                
                gettingLink();
                httpGet = new NUHttpGet(URILink);
                httpResponse = httpclient.execute(httpGet, httpContext);

                stringResponse = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("SlingFile.com.html", stringResponse);
                Document doc = Jsoup.parse(stringResponse);
                downloadlink = doc.select("div#container div#mainContent fieldset table tbody tr td input").first().val();
                deletelink = doc.select("div#container div#mainContent fieldset table tbody tr td input").eq(3).val();

                NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
                NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
                downURL = downloadlink;
                delURL = deletelink;
            }
            else{
                throw new Exception("Upload isn't good.");
            }
            
            uploadFinished();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
    
    /**
     * Implementation of guid of <a href="http://www.slingfile.com/media/plupload.beauty.js">plupload</a>.
     * @return the guid string
     */
    private String guid() {
        int f = 0;
        Long number = new Long(new Date().getTime());
        String o = Long.toString(number, 32);
        for (int p = 0; p < 5; p++) {
            o += Long.toString((Math.round(Math.random() * 65535)), 32);
        }
        //return (g.guidPrefix || "p") + o + (f++).toString(32);
        return ("p") + o + Integer.toString(f++, 32);
    }
    
}
