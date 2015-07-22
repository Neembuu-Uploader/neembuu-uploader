/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import neembuu.uploader.accounts.EasyShareAccount;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author vigneshwaran
 */
@SmallModule(
    exports={EasyShare.class,EasyShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="EasyShare.com"
)
public class EasyShare extends AbstractUploader {
    EasyShareAccount easyShareAccount = (EasyShareAccount) getAccountsProvider().getAccount("EasyShare.com");

    HttpPost httppost;
    boolean login = false;
    private long logFileSizeLimit = 1073741824; //1 GB
    private long notLogFileSizeLimit = 1073741824; //1 GB

    public EasyShare() {
        host = "EasyShare.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        if (easyShareAccount.loginsuccessful) {
            login = true;
            host = easyShareAccount.username + " | Easy-Share.com";
        }
        
    }

    @Override
    public void run() {
        //Checking once again as user may disable account while this upload thread is waiting in queue

        if (easyShareAccount.loginsuccessful) {
            login = true;
            host = easyShareAccount.username + " | Easy-Share.com";
        } else {
            login = false;
            host = "EasyShare.com";
        }

        if (login) {
            uploadLogin();
        } else {
            uploadWithoutLogin();
        }


    }

    private void uploadLogin() {
        try {
            uploadInitialising();
            if (file.length() > logFileSizeLimit) {
                showWarningMessage( "<html><b>" + getClass().getSimpleName() + "</b> " + Translation.T().maxfilesize() + ": <b>1GB</b></html>", getClass().getSimpleName());
                
                uploadInvalid();
                return;
            }
            
            
            //Must login to get session id
            //Moved essential logging code from EasyShareAccount
            //otherwise will give problem if user stops any easyshare upload(using login)
            status = UploadStatus.GETTINGCOOKIE;
            HttpParams params = new BasicHttpParams();
            params.setParameter(
                    "http.useragent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
            DefaultHttpClient httpclient = new DefaultHttpClient(params);

            NULogger.getLogger().info("Trying to log in to EasyShare");
            HttpPost httppost = new HttpPost("http://www.easy-share.com/accounts/login");
            httppost.setHeader("Referer", "http://www.easy-share.com/");
            httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("login", easyShareAccount.username));
            formparams.add(new BasicNameValuePair("password", easyShareAccount.password));
            formparams.add(new BasicNameValuePair("remember", "1"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            ////////////////////////////////////////////////////////////////////
            httppost.setEntity(entity);
            HttpResponse httpresponse = httpclient.execute(httppost);
            
            NULogger.getLogger().info("Finding cookie to add..");
            Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
            Cookie escookie = null;
            while (it.hasNext()) {
                escookie = it.next();
                if (escookie.getName().equalsIgnoreCase("PHPSESSID")) {
                    NULogger.getLogger().log(Level.INFO, "PHPSESSID:{0}", escookie.getValue());
                    break;
                }
            }
            //Watch out 1
            EntityUtils.consume(httpresponse.getEntity());
            NULogger.getLogger().info("Finding user param");
            HttpGet httpget = new HttpGet("http://www.easy-share.com/accounts/upload");
            httpresponse = httpclient.execute(httpget);
            String str = EntityUtils.toString(httpresponse.getEntity());
            str = str.substring(str.indexOf("user\": \"") + 8);
            str = str.substring(0, str.indexOf("\""));
            NULogger.getLogger().log(Level.INFO, "User param: {0}", str);
            //Watch out 2
            EntityUtils.consume(httpresponse.getEntity());
            NULogger.getLogger().info("Uploading...");
            //------------------------------------------------------------
            httppost = new HttpPost("http://upload.easy-share.com/accounts/upload_backend/perform/ajax");
            httppost.setHeader("User-Agent", "Shockwave Flash");

            MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            requestEntity.addPart("Filename", new StringBody(file.getName()));

            requestEntity.addPart("PHPSESSID", new StringBody(escookie.getValue()));

            requestEntity.addPart("user", new StringBody(str));

            requestEntity.addPart("Filedata", createMonitoredFileBody());

            requestEntity.addPart("Upload", new StringBody("Submit Query"));
            httppost.setEntity(requestEntity);
            //-------------------------------------------------------------

            uploading();

            //-------------------------------------------------------------
            httpresponse = httpclient.execute(httppost);
            String strResponse = EntityUtils.toString(httpresponse.getEntity());
            //-------------------------------------------------------------
            gettingLink();
            downURL = strResponse.substring(strResponse.indexOf("value=\"http://www.easy-share.com/") + 7);
            downURL = downURL.substring(0, downURL.indexOf("\""));

            delURL = strResponse.substring(strResponse.lastIndexOf("javascript:;\">") + 14);
            delURL = delURL.substring(0, delURL.indexOf("</a>"));


            

            NULogger.getLogger().log(Level.INFO, "Download Link: {0}", downURL);
            NULogger.getLogger().log(Level.INFO, "Delete link: {0}", delURL);

            uploadFinished();
        } catch (Exception ex) {
            ex.printStackTrace();
            NULogger.getLogger().severe(ex.toString());
            
            uploadFailed();
        }
    }

    private void uploadWithoutLogin() {
        try {
            if (file.length() > notLogFileSizeLimit) {
                showWarningMessage( "<html><b>" + getClass().getSimpleName() + "</b> " + Translation.T().maxfilesize() + ": <b>1GB</b></html>", getClass().getSimpleName());
                
                uploadInvalid();
                return;
            }
            uploadInitialising();
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("http://www.easy-share.com");
            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2 GTBDFff GTB7.0");
            HttpResponse httpresponse = httpclient.execute(httpget);
            EntityUtils.consume(httpresponse.getEntity());
            //------------------------------------------------------------
            httppost = new HttpPost("http://upload.easy-share.com/accounts/upload_backend/perform/ajax");
            httppost.setHeader("User-Agent", "Shockwave Flash");

            MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            requestEntity.addPart("Filename", new StringBody(file.getName()));

            requestEntity.addPart("Filedata", createMonitoredFileBody());

            requestEntity.addPart("Upload", new StringBody("Submit Query"));
            httppost.setEntity(requestEntity);
            //-------------------------------------------------------------
            uploading();

            //-------------------------------------------------------------
            httpresponse = httpclient.execute(httppost);
            String strResponse = EntityUtils.toString(httpresponse.getEntity());
            //-------------------------------------------------------------
            gettingLink();
            downURL = strResponse.substring(strResponse.indexOf("value=\"") + 7);
            downURL = downURL.substring(0, downURL.indexOf("\""));

            delURL = strResponse.substring(strResponse.lastIndexOf("javascript:;\">") + 14);
            delURL = delURL.substring(0, delURL.indexOf("</a>"));


            

            NULogger.getLogger().log(Level.INFO, "Download Link: {0}", downURL);
            NULogger.getLogger().log(Level.INFO, "Delete link: {0}", delURL);
            uploadFinished();
        } catch (Exception ex) {
            ex.printStackTrace();
            NULogger.getLogger().severe(ex.toString());
            
            uploadFailed();
        }
    }
    
}
