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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.accounts.FileSonicAccount;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
@SmallModule(
    exports={FileSonic.class,FileSonicAccount.class},
    interfaces={Uploader.class,Account.class},
    name="FileSonic.com",
    ignore = true
)
public class FileSonic extends AbstractUploader implements UploaderAccountNecessary {

    FileSonicAccount fileSonicAccount = (FileSonicAccount) getAccountsProvider().getAccount("FileSonic.com");
    private String uploadID = "";
    private String filesoniclink = "";
    private String postURL = "";
    private URL u;
    private HttpURLConnection uc;
    private BufferedReader br;
    private String uploadresponse;
    private long fileSizeLimit = 1073741824; //1 GB

    public FileSonic() {
        downURL = UploadStatus.NA.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "FileSonic.com";
        if (fileSonicAccount.loginsuccessful) {
            host = fileSonicAccount.username + " | FileSonic.com";
        }

    }

    public void run() {
        try {

            if (file.length() > fileSizeLimit) {
                showWarningMessage( "<html><b>" + getClass().getSimpleName() + "</b> " + Translation.T().maxfilesize() + ": <b>1GB</b></html>", getClass().getSimpleName());

                uploadInvalid();
                return;
            }

            if (fileSonicAccount.loginsuccessful) {
                host = fileSonicAccount.username + " | FileSonic.com";
            }
            //No need for 'Else' condition here. Because, filesonic will accept file upload only if the login is successful.

            NULogger.getLogger().info("Getting dynamic filesonic domain upload link ...."); 
            status=UploadStatus.INITIALISING;
            
            String fileSonicDomain = getData(FileSonicAccount.getFsdomain() + "filesystem/browse#fileManager");
            fileSonicDomain = StringUtils.stringBetweenTwoStrings(fileSonicDomain, "uploadServerHostname = '", "'");
            NULogger.getLogger().info(fileSonicDomain);




            uploadID = "upload_" + new Date().getTime() + "_" + FileSonicAccount.getSessioncookie().replace("PHPSESSID", "") + "_" + Math.round(Math.random() * 90000);


            //http://s342.filesonic.in/?callbackUrl=http://www.filesonic.in/upload-completed/:uploadProgressId&X-Progress-ID=upload_1344152373069_801k78evotima0fqmnjgg36vk5_53991


            postURL = "http://" + fileSonicDomain + "/?callbackUrl=" + FileSonicAccount.getFsdomain() + "upload-completed/:uploadProgressId&X-Progress-ID=" + uploadID;
            System.out.println("post URL : " + postURL);
//            System.exit(0);            
            fileUpload();

        } catch (Exception ex) {
            Logger.getLogger(FileSonic.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();

            uploadFailed();
        }
    }

    public void fileUpload() throws Exception {


        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        HttpPost httppost = new HttpPost(postURL);

        httppost.setHeader("Cookie", FileSonicAccount.getLangcookie() + ";" + FileSonicAccount.getSessioncookie() + ";" + FileSonicAccount.getMailcookie() + ";" + FileSonicAccount.getNamecookie() + ";" + FileSonicAccount.getRolecookie() + ";");

        MultipartEntity mpEntity = new MultipartEntity();
        //ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("files[]", createMonitoredFileBody());
        httppost.setEntity(mpEntity);
        uploading();
        NULogger.getLogger().info("Now uploading your file into filesonic...........................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        NULogger.getLogger().info(response.getStatusLine().toString());
        if (resEntity != null) {

            String tmp = EntityUtils.toString(resEntity);
            //NULogger.getLogger().info("response : " + tmp);

        }
        uploadresponse = response.getLastHeader("Location").getValue();
        NULogger.getLogger().log(Level.INFO, "Upload response URL : {0}", uploadresponse);

        uploadresponse = getData(uploadresponse);
        if (uploadresponse.contains("File was successfully uploaded")) {
            NULogger.getLogger().info("File was successfully uploaded :)");

            uploadFinished();
        } else {
            throw new Exception("There might be a problem with your internet connecivity or server error. Please try after some time. :(");
        }

    }

    public String getData(String url) throws Exception {
        System.out.println(url);

        u = new URL(url);
        uc = (HttpURLConnection) u.openConnection();
        uc.setDoOutput(true);
        uc.setRequestProperty("Host", filesoniclink);
        uc.setRequestProperty("Connection", "keep-alive");
        uc.setRequestProperty("Referer", "http://filesonic.com/");
        uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
        uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        uc.setRequestProperty("Accept-Encoding", "html");
        uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        uc.setRequestProperty("Cookie", FileSonicAccount.getLangcookie() + ";" + FileSonicAccount.getSessioncookie() + ";" + FileSonicAccount.getMailcookie() + ";" + FileSonicAccount.getNamecookie() + ";" + FileSonicAccount.getRolecookie());
        uc.setRequestMethod("GET");
        uc.setInstanceFollowRedirects(false);
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
            //NULogger.getLogger().info(temp);
            k += temp;
        }
        br.close();
        u = null;
        uc = null;
        return k;
    }
}
