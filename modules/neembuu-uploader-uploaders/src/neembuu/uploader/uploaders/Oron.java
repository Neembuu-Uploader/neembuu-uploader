/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.accounts.OronAccount;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
@SmallModule(
    exports={Oron.class,OronAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Oron.com",
    ignore = true
)
public class Oron extends AbstractUploader {

    OronAccount oronAccount = (OronAccount) getAccountsProvider().getAccount("Oron.com");
    private HttpURLConnection uc;
    private BufferedReader br;
    private URL u;
    private String oronlink;
    private String tmpserver;
    private String serverID;
    private long UploadID;
    private String fnvalue;
    private String refererURL;
    private String uploadresponse = "";
    private String downloadlink;
    private String deletelink;
    private long fileSizeLimit = 427622400; //400 MB

    public Oron() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Oron.com";
        if (oronAccount.loginsuccessful) {
            host = oronAccount.username + " | Oron.com";
        }
    }

    public String getData(String url) {

        try {
            u = new URL(url);
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.oron.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://oron.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (oronAccount.loginsuccessful) {
                uc.setRequestProperty("Cookie", OronAccount.getLogincookie() + ";" + OronAccount.getXfsscookie());
            }
            uc.setRequestMethod("GET");
            uc.setInstanceFollowRedirects(false);
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
                //NULogger.getLogger().info(temp);
                k += temp;
            }
//            br.close();
//            u = null;
//            uc = null;
            return k;
        } catch (Exception e) {
            NULogger.getLogger().log(Level.INFO, "exception : {0}", e.toString());
            return "";
        }
    }

    public void fileUpload() throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpPost httppost = new HttpPost(oronlink);
        if (oronAccount.loginsuccessful) {

            httppost.setHeader("Cookie", OronAccount.getLogincookie() + ";" + OronAccount.getXfsscookie());
        }
        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("upload_type", new StringBody("file"));
        mpEntity.addPart("srv_id", new StringBody(serverID));
        if (oronAccount.loginsuccessful) {
            mpEntity.addPart("sess_id", new StringBody(OronAccount.getXfsscookie().substring(5)));
        }
        mpEntity.addPart("srv_tmp_url", new StringBody(tmpserver));
        mpEntity.addPart("file_0", createMonitoredFileBody());
        httppost.setEntity(mpEntity);
        NULogger.getLogger().info("Now uploading your file into oron...........................");
        uploading();
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        NULogger.getLogger().info(response.getStatusLine().toString());
        if (resEntity != null) {

            String tmp = EntityUtils.toString(resEntity);
            NULogger.getLogger().log(Level.INFO, "Upload response : {0}", tmp);

            fnvalue = StringUtils.stringBetweenTwoStrings(tmp, "name='fn' value='", "'");
            NULogger.getLogger().log(Level.INFO, "fn value : {0}", fnvalue);

        } else {
            throw new Exception("There might be a problem with your internet connection or server problem. Please try again after some time. :(");
        }


    }

    private void initialize() throws Exception {

        u = new URL("http://www.oron.com");
        uc = (HttpURLConnection) u.openConnection();
        if (oronAccount.loginsuccessful) {
            uc.setRequestProperty("Cookie", OronAccount.getLogincookie() + ";" + OronAccount.getXfsscookie());
        }
        NULogger.getLogger().info("Getting dynamic oron upload link value ........");
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
            //NULogger.getLogger().info(temp);
            k += temp;
        }

        //http://polaris.oron.com/upload/337/?X-Progress-ID=502517462254

        oronlink = StringUtils.stringBetweenTwoStrings(k, "action=\"", "\"");

        tmpserver = oronlink.substring(0, oronlink.indexOf("/upload"));
        NULogger.getLogger().log(Level.INFO, "Temp Server : {0}", tmpserver);
        serverID = oronlink.substring(oronlink.lastIndexOf("/") + 1);
        NULogger.getLogger().log(Level.INFO, "Server ID : {0}", serverID);
        UploadID = (long) (Math.floor(Math.random() * 900000000000L) + 100000000000L);
        oronlink += "/?X-Progress-ID=" + UploadID;
        NULogger.getLogger().info(oronlink);
    }

    public void run() {
        try {

            if (oronAccount.loginsuccessful) {
                host = oronAccount.username + " | Oron.com";
            } else {
                host = "Oron.com";
            }

            if (file.length() > fileSizeLimit) {
                showWarningMessage( "<html><b>" + getClass().getSimpleName() + "</b> " + Translation.T().maxfilesize() + ": <b>400MB</b></html>", getClass().getSimpleName());

                uploadInvalid();
                return;
            }

            uploadInitialising();
            initialize();
            fileUpload();
            gettingLink();
            refererURL = tmpserver + "/status.html?file=" + UploadID + "=" + file.getName();
            NULogger.getLogger().log(Level.INFO, "Referer URL : {0}", refererURL);

            uploadresponse = getData(refererURL);

            HttpParams params = new BasicHttpParams();
            params.setParameter(
                    "http.useragent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
            DefaultHttpClient httpclient = new DefaultHttpClient(params);


            HttpPost httppost = new HttpPost("http://oron.com/");
            if (oronAccount.loginsuccessful) {
                httppost.setHeader("Cookie", OronAccount.getLogincookie() + ";" + OronAccount.getXfsscookie());
            }
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("op", "upload_result"));
            formparams.add(new BasicNameValuePair("fn", fnvalue));
            formparams.add(new BasicNameValuePair("st", "OK"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httppost.setEntity(entity);
            HttpResponse httpresponse = httpclient.execute(httppost);

            uploadresponse = EntityUtils.toString(httpresponse.getEntity());
//        NULogger.getLogger().info(uploadresponse);
            if (uploadresponse.isEmpty()) {
                throw new Exception("There might be a problem with your internet connection or Oron server problem. Please try after some time. :(");
            }
            downloadlink = StringUtils.stringBetweenTwoStrings(uploadresponse, "Download Link", "\" class=");

            downloadlink = downloadlink.substring(downloadlink.indexOf("<a href=\""));
            downloadlink = downloadlink.replace("<a href=\"", "");
            NULogger.getLogger().log(Level.INFO, "Download Link : {0}", downloadlink);
            downURL = downloadlink;
            deletelink = StringUtils.stringBetweenTwoStrings(uploadresponse, "Delete Link", "\"></td>");
            deletelink = deletelink.substring(deletelink.indexOf("value=\""));
            deletelink = deletelink.replace("value=\"", "");
            NULogger.getLogger().log(Level.INFO, "Delete Link : {0}", deletelink);
            delURL = deletelink;

            uploadFinished();


        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
}
