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
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.accounts.BadongoAccount;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
@SmallModule(
    exports={Badongo.class,BadongoAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Badongo.com",
    ignore = true
)
public class Badongo extends AbstractUploader {

    BadongoAccount badongoAccount = (BadongoAccount) getAccountsProvider().getAccount("Badongo.com");
    private String UPLOAD_ID_CHARS = "1234567890qwertyuiopasdfghjklzxcvbnm";
    private HttpURLConnection uc;
    private BufferedReader br;
    private String uid;
    private String postURL;
    private String dataid;
    private String uploadresponse;
    private String downloadlink;
    private long fileSizeLimit = 1073741824l; //1 GB

    public Badongo() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "Badongo.com";

        if (badongoAccount.loginsuccessful) {
            //  login = true;
            host = badongoAccount.username + " | Badongo.com";
        }
    }

    private String getData(String myurl) throws Exception {
        URL url = new URL(myurl);
        uc = (HttpURLConnection) url.openConnection();
        if (badongoAccount.loginsuccessful) {
            uc.setRequestProperty("Cookie", BadongoAccount.getUsercookie() + ";" + BadongoAccount.getPwdcookie());
        }
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
//                NULogger.getLogger().info(temp);
            k += temp;
        }
        br.close();
        return k;
    }

    public void run() {

        try {

            if (badongoAccount.loginsuccessful) {
                host = badongoAccount.username + " | Badongo.com";
            } else {
                host = "Badongo.com";
            }

            if (file.length() > fileSizeLimit) {
                showWarningMessage( "<html><b>" + getClass().getSimpleName() + "</b> " + Translation.T().maxfilesize() + ": <b>1GB</b></html>", getClass().getSimpleName());
                uploadInvalid();
                return;
            }

            uploadInitialising();
            generateBadongoID();
            postURL = "http://upload.badongo.com/mpu_upload_single.php?UL_ID=undefined&UPLOAD_IDENTIFIER=undefined&page=upload_s&s=&cou=en&PHPSESSID=" + uid + "&desc=";
            NULogger.getLogger().log(Level.INFO, "post : {0}", postURL);
            if (badongoAccount.loginsuccessful) {
                dataid = getData("http://upload.badongo.com/mpu.php?cou=en&k=member");
                dataid = StringUtils.stringBetweenTwoStrings(dataid, "\"PHPSESSID\" : \"", "\"");
                NULogger.getLogger().log(Level.INFO, "Data : {0}", dataid);
            }
            fileUpload();
            gettingLink();
            if (badongoAccount.loginsuccessful) {
                downloadlink = getData("http://upload.badongo.com/upload_complete.php?session=" + dataid);
                downloadlink = StringUtils.stringBetweenTwoStrings(downloadlink, "msg_u=", "&");
            } else {
                downloadlink = getData("http://upload.badongo.com/upload_complete.php?page=upload_s_f&PHPSESSID=" + uid + "&url=undefined&url_kill=undefined&affliate=");
                downloadlink = StringUtils.stringBetweenTwoStrings(downloadlink, "url=", "&");
            }
            downloadlink = URLDecoder.decode(downloadlink, "UTF-8");
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            downURL = downloadlink;
            uploadFinished();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            uploadFailed();
        } finally {
            UPLOAD_ID_CHARS = null;
            uc = null;
            br = null;
            uid = null;
            postURL = null;
            dataid = null;
            uploadresponse = null;
            downloadlink = null;
        }
    }

    private void fileUpload() throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        if (badongoAccount.loginsuccessful) {
            postURL = "http://upload.badongo.com/mpu_upload.php";
        }
        HttpPost httppost = new HttpPost(postURL);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        if (badongoAccount.loginsuccessful) {
            mpEntity.addPart("PHPSESSID", new StringBody(dataid));
        }
        mpEntity.addPart("Filedata", createMonitoredFileBody());
        httppost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httppost.getRequestLine());
        NULogger.getLogger().info("Now uploading your file into badongo.com");
        uploading();
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        //  uploadresponse = response.getLastHeader("Location").getValue();
        //NULogger.getLogger().log(Level.INFO, "Upload response : {0}", uploadresponse);
        NULogger.getLogger().info(response.getStatusLine().toString());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }
//        if (resEntity != null) {
//            resEntity.consumeContent();
//        }
        NULogger.getLogger().log(Level.INFO, "res {0}", uploadresponse);


        httpclient.getConnectionManager().shutdown();
    }

    public void generateBadongoID() throws Exception {
        StringBuilder sb = new StringBuilder();
        //sb.append(new Date().getTime() / 1000);
        for (int i = 0; i < 32; i++) {
            int idx = 1 + (int) (Math.random() * 35);
            sb.append(UPLOAD_ID_CHARS.charAt(idx));
        }
        uid = sb.toString();
//        NULogger.getLogger().info("uid : "+uid+" - "+uid.length());
    }
}
