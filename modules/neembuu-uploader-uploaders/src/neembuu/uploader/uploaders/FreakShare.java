/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.accounts.FreakShareAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.uploaders.common.FormBodyPartUtils;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author vigneshwaran
 * @author davidepastore
 */
@SmallModule(
    exports={FreakShare.class,FreakShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="FreakShare.com"
)
public class FreakShare extends AbstractUploader {
    
    FreakShareAccount freakShareAccount = (FreakShareAccount) getAccountsProvider().getAccount("FreakShare.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    private String progressKey = "";
    private String userGroupKey = "";
    private String uploadIdentifier = "";

    HttpGet httpget;
    HttpPost httppost;
    String strResponse, link;
    String start;
    String manageURL;
    private long notLogfileSizeLimit = 1073741824L; //1024 MB
    private long logfileSizeLimit = 1073741824L; //1024 MB
    
    private String downloadlink = "";
    private String deletelink = "";

    public FreakShare() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "FreakShare.com";
        if (freakShareAccount.loginsuccessful) {
            host = freakShareAccount.username + " | FreakShare.com";
        }
        maxFileSizeLimit = 1073741824l; //1024 MB
    }
    
    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://www.freakshare.com", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.getElementById("uploadform").attr("action");
        uploadURL += "?X-Progress-ID=undefined" + CommonUploaderTasks.createRandomString(32);
        
        progressKey = doc.select("#progress_key").first().val();
        userGroupKey = doc.select("#usergroup_key").first().val();
        uploadIdentifier = doc.select("input[name=UPLOAD_IDENTIFIER]").first().val();
    }

    @Override
    public void run() {
        try {
            if (freakShareAccount.loginsuccessful) {
                userType = "reg";
                httpContext = freakShareAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "xfss");
                
                if(freakShareAccount.isPremium()){
                    maxFileSizeLimit = 8438939648l; //8048 MB
                }
                else{
                    maxFileSizeLimit = 4244635648l; //4048 MB
                }
                
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 1073741824l; //1024 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("APC_UPLOAD_PROGRESS", new StringBody(progressKey));
            mpEntity.addPart("APC_UPLOAD_USERGROUP", new StringBody(userGroupKey));
            mpEntity.addPart("UPLOAD_IDENTIFIER", new StringBody(uploadIdentifier));
            FormBodyPart customBodyPart = FormBodyPartUtils.createEmptyFileFormBodyPart("file[]", new StringBody(""));
            mpEntity.addPart(customBodyPart);
            mpEntity.addPart("file[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into FreakShare.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            final String location = httpResponse.getFirstHeader("Location").getValue();
            EntityUtils.consume(httpResponse.getEntity());
            responseString = NUHttpClientUtils.getData(location, httpContext);

            //Read the links
            gettingLink();
            //FileUtils.saveInFile("FreakShare.html", responseString);
            
            doc = Jsoup.parse(responseString);
            downloadlink = doc.select("input[type=text]").eq(0).val();
            deletelink = doc.select("input[type=text]").eq(3).val();
            
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
        
        //Checking once again as user may disable account while this upload thread is waiting in queue

//        uploadWithoutLogin();

    }

    private void uploadWithoutLogin() {
        try {
            uploadInitialising();
            //-------------------------------------------------------------
            if (file.length() > notLogfileSizeLimit) { //1024 mbytes
                showWarningMessage( "<html><b>" + getClass().getSimpleName() + "</b> " + Translation.T().maxfilesize() + ": <b>1024MB</b></html>", getClass().getSimpleName());
                uploadInvalid();
                return;
            }

            
            HttpParams params = new BasicHttpParams();
            params.setParameter(
                    "http.useragent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
            DefaultHttpClient httpclient = new DefaultHttpClient(params);
            httpget = new HttpGet("http://www.freakshare.com/");
            httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            HttpResponse httpresponse = httpclient.execute(httpget);
            strResponse = EntityUtils.toString(httpresponse.getEntity());
            
            start = "<form action=\"";
            link = strResponse.substring(strResponse.indexOf(start + "http://") + start.length());
            link = link.substring(0, link.indexOf("\""));
            NULogger.getLogger().log(Level.INFO, "FreakShare post link :{0}", link);
            //------------------------------------------------------------

            httppost = new HttpPost(link+"?X-Progress-ID=undefined"+CommonUploaderTasks.createRandomString(24));
            System.out.println(httppost.getURI()); 
            httppost.setHeader("Referer", "http://freakshare.com/");
            httppost.setHeader("Cache-Control", "max-age=0");
            httppost.setHeader("Origin", "http://freakshare.com");
            httppost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

            MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            Map<String,String> mp = getHiddenParams(strResponse);
            for(String key : mp.keySet()) {
                requestEntity.addPart(key, new StringBody(mp.get(key)));
                System.out.println("setting cookies...");
            }
            requestEntity.addPart("file[]", new StringBody(""));
            requestEntity.addPart("file[]", createMonitoredFileBody());
            

            httppost.setEntity(requestEntity);
            //-------------------------------------------------------------
            uploading();
            //-------------------------------------------------------------
            httpresponse = httpclient.execute(httppost);
            System.out.println(EntityUtils.toString(httpresponse.getEntity()));
            manageURL = httpresponse.getHeaders("Location")[0].getValue();
            NULogger.getLogger().log(Level.INFO, "FreakShare Manage URL{0}", manageURL);

            NULogger.getLogger().info("Getting links from Manage URL");

            gettingLink();

            //-------------------------------------------------------------
            httpget = new HttpGet(manageURL);
            //httpclient = new DefaultHttpClient(params);
            httpresponse = httpclient.execute(httpget);
            strResponse = EntityUtils.toString(httpresponse.getEntity());



            downURL = getDownURLFromResp(strResponse);
            delURL = getDeleteURLFromResp(strResponse);
            //--------------------------------------------------------------





            NULogger.getLogger().log(Level.INFO, "Download Link: {0}", downURL);
            NULogger.getLogger().log(Level.INFO, "Delete link: {0}", delURL);

            uploadFinished();
        } catch (Exception ex) {
            ex.printStackTrace();
            NULogger.getLogger().severe(ex.toString());

            uploadFailed();
        }
    }
    /*
    private void uploadWithoutLogin() {
    try {
    //-------------------------------------------------------------
    if (file.length() > 419430400) {
    showWarningMessage( "<html><b>" + getClass().getSimpleName() + "</b> " + Translation.T().maxfilesize() + ": <b>400MB</b></html>", getClass().getSimpleName());
    
    uploadFailed();
    return;
    }
    uploadInitialising();
    HttpParams params = new BasicHttpParams();
    params.setParameter(
    "http.useragent",
    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
    DefaultHttpClient httpclient = new DefaultHttpClient(params);
    HttpGet httpget = new HttpGet("http://www.hotfile.com");
    HttpResponse httpresponse = httpclient.execute(httpget);
    
    strResponse = EntityUtils.toString(httpresponse.getEntity());
    start = "<form action=\"";
    link = strResponse.substring(strResponse.indexOf(start + "http://") + start.length());
    link = link.substring(0, link.indexOf("\""));
    NULogger.getLogger().info(link);
    //------------------------------------------------------------
    httppost = new HttpPost(link);
    httppost.setHeader("Referer", "http://www.hotfile.com/");
    httppost.setHeader("Cache-Control", "max-age=0");
    httppost.setHeader("Origin", "http://www.hotfile.com/");
    httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png;q=0.5");
    
    MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
    
    requestEntity.addPart("uploads[]", createMonitoredFileBody());
    requestEntity.addPart("iagree", new StringBody("on"));
    requestEntity.addPart("", new StringBody("Upload"));
    
    httppost.setEntity(requestEntity);
    //-------------------------------------------------------------
    uploading();
    
    //-------------------------------------------------------------
    httpresponse = httpclient.execute(httppost);
    manageURL = httpresponse.getHeaders("Location")[0].getValue();
    NULogger.getLogger().log(Level.INFO, "HotFile Manage URL{0}", manageURL);
    
    status = UploadStatus.GETTINGLINK;
    
    NULogger.getLogger().info("Getting links from Manage URL");
    
    
    //-------------------------------------------------------------
    httpget = new HttpGet(manageURL);
    httpclient = new DefaultHttpClient(params);
    httpresponse = httpclient.execute(httpget);
    strResponse = EntityUtils.toString(httpresponse.getEntity());
    
    start = "<input type=\"text\" name=\"url\" id=\"url\" class=\"textfield\" value=\"";
    downURL = strResponse.substring(strResponse.indexOf(start) + start.length());
    downURL = downURL.substring(0, downURL.indexOf("\""));
    
    start = "<input type=\"text\" name=\"delete\" id=\"delete\" class=\"textfield\" value=\"";
    delURL = strResponse.substring(strResponse.indexOf(start) + start.length());
    delURL = delURL.substring(0, delURL.indexOf("\""));
    //--------------------------------------------------------------
    
    
    
    
    
    NULogger.getLogger().log(Level.INFO, "Download Link: {0}", downURL);
    NULogger.getLogger().log(Level.INFO, "Delete link: {0}", delURL);
    
    uploadFinished();
    } catch (Exception ex) {
    ex.printStackTrace();
    NULogger.getLogger().severe(ex.toString());
    
    uploadFailed();
    }
    }
     */
    
   
    private Map<String,String> getHiddenParams(String resp){
        String nameStart = "name=\"";
        String valueStart = "value=\"";
        Map<String,String> mp = new HashMap<String, String>();
        
        Pattern pattern = Pattern.compile("<input type=\"hidden[^>]+?>");
        Matcher matcher = pattern.matcher(resp);
        while(matcher.find()) {
            String line = matcher.group();
            String key = line.substring(line.indexOf(nameStart)+nameStart.length());
            key = key.substring(0,key.indexOf("\""));
            String value = line.substring(line.indexOf(valueStart)+valueStart.length());
            value = value.substring(0,value.indexOf("\""));
            mp.put(key, value);
            System.out.println(key+ " --> "+ value);
        }
        return mp;
    }

    private String getDownURLFromResp(String resp) throws Exception {
        Pattern pattern = Pattern.compile("value=\"(http://freakshare.com/files/[^\"]+?)\"");
        Matcher matcher = pattern.matcher(resp);
        if(!matcher.find()) throw new Exception("Invalid Download URL");
        String line = matcher.group(1);
        return line;
    }
    
    
    private String getDeleteURLFromResp(String resp) throws Exception {
        Pattern pattern = Pattern.compile("value=\"(http://freakshare.com/delete/[^\"]+?)\"");
        Matcher matcher = pattern.matcher(resp);
        if(!matcher.find()) throw new Exception("Invalid Delete URL");
        String line = matcher.group(1);
        return line;
    }
}
