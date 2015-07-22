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
import neembuu.uploader.accounts.HotFileAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author vigneshwaran
 */
@SmallModule(
    exports={HotFile.class,HotFileAccount.class},
    interfaces={Uploader.class,Account.class},
    name="HotFile.com",
    ignore = true
)
public class HotFile extends AbstractUploader implements UploaderAccountNecessary {

    HotFileAccount hotFileAccount = (HotFileAccount) getAccountsProvider().getAccount("HotFile.com");
    
    HttpClient httpclient = NUHttpClient.getHttpClient();
    HttpContext httpContext;
    HttpResponse httpresponse;
    NUHttpGet httpget;
    NUHttpPost httppost;
    
    
    String strResponse, link;
    String start;
    String manageURL;

    public HotFile() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "HotFile.com";

        if (hotFileAccount.loginsuccessful) {
            host = hotFileAccount.username + " | HotFile.com";
        }

    }

    @Override
    public void run() {
        //Checking once again as user may disable account while this upload thread is waiting in queue

        if (hotFileAccount.loginsuccessful) {
            host = hotFileAccount.username + " | HotFile.com";
        } else {
            host = "HotFile.com";

            uploadInvalid();
            return;
        }
        
        uploadLogin();
    }

    private void uploadLogin() {
        try {
            uploadInitialising();
            //-------------------------------------------------------------
            if (file.length() > hotFileAccount.getMaxFileSizeLimit()) {
                throw new NUMaxFileSizeException(hotFileAccount.getMaxFileSizeLimit(), file.getName(), hotFileAccount.getHOSTNAME());
            }
            
            httpContext = hotFileAccount.getHttpContext();

            status = UploadStatus.GETTINGCOOKIE;
            httpget = new NUHttpGet("http://hotfile.com/?cookiecheck=1");
            httpresponse = httpclient.execute(httpget, httpContext);


            strResponse = EntityUtils.toString(httpresponse.getEntity());
            start = "<form action=\"";
            link = strResponse.substring(strResponse.indexOf(start + "http://") + start.length());
            link = link.substring(0, link.indexOf("\""));
            NULogger.getLogger().info(link);
            //------------------------------------------------------------

            httppost = new NUHttpPost(link);
            MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            requestEntity.addPart("uploads[]", createMonitoredFileBody());
            requestEntity.addPart("iagree", new StringBody("on"));
            requestEntity.addPart("", new StringBody("Upload"));

            httppost.setEntity(requestEntity);
            //-------------------------------------------------------------
            uploading();
            //-------------------------------------------------------------
            httpresponse = httpclient.execute(httppost, httpContext);
            manageURL = httpresponse.getHeaders("Location")[0].getValue();
            strResponse = EntityUtils.toString(httpresponse.getEntity());
            NULogger.getLogger().log(Level.INFO, "HotFile Manage URL{0}", manageURL);

            NULogger.getLogger().info("Getting links from Manage URL");

            gettingLink();

            //-------------------------------------------------------------
            httpget = new NUHttpGet(manageURL);
            httpresponse = httpclient.execute(httpget, httpContext);
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
        } catch (NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception ex) {
            Logger.getLogger(HotFile.class.getName()).log(Level.SEVERE, null, ex);
            uploadFailed();
        }
    }
    /*
    private void uploadWithoutLogin() {
    try {
    //-------------------------------------------------------------
    if (file.length() > notLogFileSizeLimit) {
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
    NUHttpGet httpget = new NUHttpGet("http://www.hotfile.com");
    HttpResponse httpresponse = httpclient.execute(httpget);
    
    strResponse = EntityUtils.toString(httpresponse.getEntity());
    start = "<form action=\"";
    link = strResponse.substring(strResponse.indexOf(start + "http://") + start.length());
    link = link.substring(0, link.indexOf("\""));
    NULogger.getLogger().info(link);
    //------------------------------------------------------------
    httppost = new NUHttpPost(link);
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
}
