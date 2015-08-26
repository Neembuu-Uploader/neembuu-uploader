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
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.MegasharesAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
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
import org.jsoup.select.Elements;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={Megashares.class,MegasharesAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Megashares.com"
)
public class Megashares extends AbstractUploader{
    
    MegasharesAccount megasharesAccount = (MegasharesAccount) getAccountsProvider().getAccount("Megashares.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL1;
    private String uploadURL2;
    private String uploadURL3;
    private String uid;
    private String userType;
    private String sessionID = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public Megashares() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Megashares.com";
        if (megasharesAccount.loginsuccessful) {
            host = megasharesAccount.username + " | Megashares.com";
        }
        maxFileSizeLimit = 10485760000l; //10000 MB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://megashares.com/", httpContext);
        uploadURL1 = StringUtils.stringBetweenTwoStrings(responseString, "pre_check_url : '", "'");
        uploadURL2 = StringUtils.stringBetweenTwoStrings(responseString, "url : '", "'");
        uploadURL3 = StringUtils.stringBetweenTwoStrings(responseString, "redirection_url: '", "'");
        
        uid = guid();
        NULogger.getLogger().log(Level.INFO, "uploadURL3: {0}", uploadURL3);
    }

    @Override
    public void run() {
        try {
            if (megasharesAccount.loginsuccessful) {
                userType = "reg";
                httpContext = megasharesAccount.getHttpContext();
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
            
            //First step
            httpPost = new NUHttpPost(uploadURL1);
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("uploading_files[0][id]", ""));
            formparams.add(new BasicNameValuePair("uploading_files[0][name]", file.getName()));
            formparams.add(new BasicNameValuePair("uploading_files[0][size]", String.valueOf(file.length())));
            formparams.add(new BasicNameValuePair("uploading_files[0][loaded]", "0"));
            formparams.add(new BasicNameValuePair("uploading_files[0][percent]", "0"));
            formparams.add(new BasicNameValuePair("uploading_files[0][status]", "1"));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            if(!responseString.equals("success")){
                throw new Exception("Pre upload error.");
            }

            //Second step
            httpPost = new NUHttpPost(uploadURL2);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            //mpEntity.addPart("sess_id", new StringBody(sessionID));
            mpEntity.addPart("name", new StringBody(uid + "." + FileUtils.getFileExtension(file)));
            mpEntity.addPart("uploadFileDescription", new StringBody(""));
            mpEntity.addPart("passProtectUpload", new StringBody(""));
            mpEntity.addPart("uploadFileCategory", new StringBody("doc"));
            mpEntity.addPart("searchable", new StringBody("on"));
            mpEntity.addPart("emailAddress", new StringBody(""));
            mpEntity.addPart("searchable", new StringBody("on"));
            mpEntity.addPart("file", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into Megashares.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            final String fid = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            responseString = NUHttpClientUtils.getData(uploadURL3 + "?fid=" + fid, httpContext);
            
            //FileUtils.saveInFile("Megashares.html", responseString);
            
            doc = Jsoup.parse(responseString);
            Elements elements = doc.select("dl.user_links a");
            downloadlink = elements.eq(0).text();
            deletelink = elements.eq(1).text();
            
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
    
    /**
     * Get a guid.
     * @return A guid String.
     */
    private String guid(){
        String n = Long.toString(new Date().getTime(), 32);
        for(int i = 0; i < 5; i++){
            n += Long.toString((int)(Math.random()*65535), 32);
        }
        return n;
    }
    
}
