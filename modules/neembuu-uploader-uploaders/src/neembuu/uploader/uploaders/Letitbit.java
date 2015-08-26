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
import neembuu.uploader.accounts.LetitbitAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.CookieUtils;
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

/**
 *
 * @author Dinesh
 * @author davidepastore
 */
@SmallModule(
    exports={Letitbit.class,LetitbitAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Letitbit.net"
)
public class Letitbit extends AbstractUploader implements UploaderAccountNecessary {

    LetitbitAccount letitbitAccount = (LetitbitAccount) getAccountsProvider().getAccount("Letitbit.net");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private String stringResponse;
    
    private String tmp;
    private String phpsessioncookie, debugcookie = "", downloadlink = "", deletelink = "";
    private String server, postURL = "";
    private String base;
    private String uploadresponse;
    private String uploadpage;
    private String pin = "";
    private String uid;

    public Letitbit() {

        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Letitbit.net";
        //It has to be successful.. as it won't work without login
        if (letitbitAccount.loginsuccessful) {
            host = letitbitAccount.username + " | Letitbit.net";
        }
        
        maxFileSizeLimit = 2147483647; //2 GB
    }

    private void initialize() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        NULogger.getLogger().info("Getting startup cookie from letitbit.net");
        stringResponse =  getData("http://www.letitbit.net/");
        
        if(CookieUtils.existCookie(httpContext, "PHPSESSID")){
            phpsessioncookie = CookieUtils.getCookieNameValue(httpContext, "PHPSESSID");
        }
        
        if(CookieUtils.existCookie(httpContext, "debug_panel")){
            debugcookie = CookieUtils.getCookieNameValue(httpContext, "PHPSESSID");
        }

        NULogger.getLogger().log(Level.INFO, "phpsessioncookie: {0}", phpsessioncookie);
        NULogger.getLogger().log(Level.INFO, "debugcookie : {0}", debugcookie);
        
        server = StringUtils.stringBetweenTwoStrings(stringResponse, "ACUPL_UPLOAD_SERVER = '", "'");
        base = StringUtils.stringBetweenTwoStrings(stringResponse, "\"base\" type=\"hidden\" value=\"", "\"");
        NULogger.getLogger().log(Level.INFO, "base : {0}", base);
        generateLetitbitID();
        NULogger.getLogger().log(Level.INFO, "server : {0}", server);
        postURL = "http://" + server + "/marker=" + uid;
        NULogger.getLogger().log(Level.INFO, "Post URL :{0}", postURL);
    }

    private void generateLetitbitID() throws Exception {

        String rand = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        sb.append(Long.toHexString(System.currentTimeMillis()).toUpperCase());
        sb.append("_");
        for (int i = 0; i < 40; i++) {
            // int Min = 1, Max = 60;
            //Min + (int)(Math.random() * ((Max - Min) + 1))
            //1+(int)(Math.random() * ((60 - 1) + 1))
            //1+(int)(Math.random() * 60)
//            int k=(int) Math.round(1+(int)(Math.random() * 60));
            sb.append(rand.charAt((int) Math.round(1 + (int) (Math.random() * 60))));
        }
        uid = sb.toString();
    }

    private void getData() throws Exception {
        stringResponse =  getData("http://www.letitbit.net/");
        
        //CookieUtils.printCookie(httpContext);
        //FileUtils.saveInFile("Letitbit.html", stringResponse);
        server = StringUtils.stringBetweenTwoStrings(stringResponse, "ACUPL_UPLOAD_SERVER = '", "'");
        base = StringUtils.stringBetweenTwoStrings(stringResponse, "\"base\" type=\"hidden\" value=\"", "\"");
        pin = StringUtils.stringBetweenTwoStrings(stringResponse, "\"pin\" type=\"hidden\" value=\"", "\"");
        NULogger.getLogger().log(Level.INFO, "pin : {0}", pin);
        NULogger.getLogger().log(Level.INFO, "base : {0}", base);
        generateLetitbitID();
        NULogger.getLogger().log(Level.INFO, "server : {0}", server);
        postURL = "http://" + server + "/marker=" + uid;
        NULogger.getLogger().log(Level.INFO, "Post URL :{0}", postURL);
    }

    private String getData(String geturl) throws Exception {
        httpGet = new NUHttpGet(geturl);
            
        httpResponse = httpclient.execute(httpGet, httpContext);
        return EntityUtils.toString(httpResponse.getEntity());
    }

    private void fileUpload() throws Exception {
        httpPost = new NUHttpPost(postURL);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        mpEntity.addPart("MAX_FILE_SIZE", new StringBody(Long.toString(maxFileSizeLimit)));
        mpEntity.addPart("owner", new StringBody(""));
        mpEntity.addPart("pin", new StringBody(pin));
        mpEntity.addPart("base", new StringBody(base));
        mpEntity.addPart("host", new StringBody("letitbit.net"));
        mpEntity.addPart("file0", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
        uploading();
        NULogger.getLogger().info("Now uploading your file into letitbit.net");
        HttpResponse response = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = response.getEntity();
        NULogger.getLogger().info(response.getStatusLine().toString());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }
//  
        NULogger.getLogger().log(Level.INFO, "Upload response : {0}", uploadresponse);
    }

    @Override
    public void run() {

        if (letitbitAccount.loginsuccessful) {
            host = letitbitAccount.username + " | Letitbit.net";
        } else {
            host = "Letitbit.net";
            uploadInvalid();
            return;
        }


        try {
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), getHost());
            }
            
            uploadInitialising();
            if (letitbitAccount.loginsuccessful) {
                httpContext = letitbitAccount.getHttpContext();
                getData();
            } else {
                initialize();
            }
            fileUpload();
            gettingLink();
            uploadresponse = "http://letitbit.net/acupl_proxy.php?srv=" + server + "&uid=" + uid;
            tmp = getData(uploadresponse);
            // NULogger.getLogger().info("upload response : "+uploadresponse);
            tmp = StringUtils.stringBetweenTwoStrings(tmp, "\"post_result\": \"", "\"");
            NULogger.getLogger().log(Level.INFO, "upload page : {0}", tmp);
            uploadpage = getData(tmp);
//            NULogger.getLogger().info(uploadpage);
            downloadlink = StringUtils.stringBetweenTwoStrings(uploadpage, "Links to download files:", "</textarea>");
            downloadlink = downloadlink.substring(downloadlink.lastIndexOf(">") + 1);
            deletelink = StringUtils.stringBetweenTwoStrings(uploadpage, "Links to delete files:", "</div>");
            deletelink = deletelink.replace("<br/>", "");
            deletelink = deletelink.substring(deletelink.lastIndexOf(">") + 1);
            NULogger.getLogger().log(Level.INFO, "Download Link : {0}", downloadlink);
            NULogger.getLogger().log(Level.INFO, "Delete Link : {0}", deletelink);
            downURL = downloadlink;
            delURL = deletelink;

            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(Letitbit.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
}
