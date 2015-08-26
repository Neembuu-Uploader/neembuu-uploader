/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**DEAD*/
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.accounts.UploadBoxAccount;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author Dinesh
 */
@SmallModule(
    exports={UploadBox.class,UploadBoxAccount.class},
    interfaces={Uploader.class,Account.class},
    name="UploadBox.com"
)
public class UploadBox extends AbstractUploader {

    UploadBoxAccount uploadBoxAccount = (UploadBoxAccount) getAccountsProvider().getAccount("UploadBox.com");
    private String uid;
    private URL u;
    private HttpURLConnection uc;
    private String tmp;
    private String sidcookie;
    private BufferedReader br;
    private String postURL;
    private String server;
    private String uploadresponse;
    private String downloadlink;
    private String deletelink;
    private long fileSizeLimit = 2097152000l; //2000MB - last char is a 'l' for long

    public UploadBox() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        if (uploadBoxAccount.loginsuccessful) {
            host = uploadBoxAccount.username + " | UploadBox.com";
        }
    }

    private void generateUploadBoxID() throws Exception {
        String rand = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(rand.charAt((int) Math.round(1 + (int) (Math.random() * 60))));
        }
        uid = sb.toString();
    }

    private void initialize() throws Exception {
        NULogger.getLogger().info("Getting startup cookie from uploadbox.com");
        u = new URL("http://uploadbox.com/");
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();

        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                if (tmp.contains("sid")) {
                    sidcookie = tmp;
                }

            }
        }
        sidcookie = sidcookie.substring(0, sidcookie.indexOf(";"));
        NULogger.getLogger().log(Level.INFO, "sidcookie : {0}", sidcookie);
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
        postURL = StringUtils.stringBetweenTwoStrings(k, "action = \"", "\"");
        generateUploadBoxID();
        postURL = postURL + uid;
        NULogger.getLogger().log(Level.INFO, "Post URL : {0}", postURL);
        server = StringUtils.stringBetweenTwoStrings(k, "name=\"server\" value=\"", "\"");
        NULogger.getLogger().info(server);
    }

    private String getData(String myurl) throws Exception {
        u = new URL(myurl);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", UploadBoxAccount.getSidcookie());
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
        return k;
    }

    private void fileUpload() throws Exception {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postURL);
        if (uploadBoxAccount.loginsuccessful) {
            httppost.setHeader("Cookie", UploadBoxAccount.getSidcookie());
        } else {
            httppost.setHeader("Cookie", sidcookie);
        }
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        mpEntity.addPart("filepc", createMonitoredFileBody());
        mpEntity.addPart("server", new StringBody(server));
        httppost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httppost.getRequestLine());
        NULogger.getLogger().info("Now uploading your file into uploadbox.com");
        uploading();
        HttpResponse response = httpclient.execute(httppost);
        gettingLink();
        NULogger.getLogger().info(response.getStatusLine().toString());
        uploadresponse = response.getLastHeader("Location").getValue();
        NULogger.getLogger().log(Level.INFO, "Upload Response : {0}", uploadresponse);
        uploadresponse = getData(uploadresponse);
        downloadlink = StringUtils.stringBetweenTwoStrings(uploadresponse, "name=\"loadlink\" id=\"loadlink\" class=\"text\" onclick=\"this.select();\" value=\"", "\"");
        deletelink = StringUtils.stringBetweenTwoStrings(uploadresponse, "name=\"deletelink\" id=\"deletelink\" class=\"text\" onclick=\"this.select();\" value=\"", "\"");
        downURL = downloadlink;
        delURL = deletelink;
        NULogger.getLogger().log(Level.INFO, "Download link {0}", downloadlink);
        NULogger.getLogger().log(Level.INFO, "deletelink : {0}", deletelink);

        uploadFinished();
    }

    public void run() {
        
        if (file.length() > fileSizeLimit) {
            showWarningMessage( "<html><b>" + getClass().getSimpleName() + "</b> " + Translation.T().maxfilesize() + ": <b>2000MB</b></html>", getClass().getSimpleName());

            uploadInvalid();
            return;
        }

        if (uploadBoxAccount.loginsuccessful) {

            host = uploadBoxAccount.username + " | UploadBox.com";
        } else {

            host = "UploadBox.com";
        }
        try {
            uploadInitialising();
            if (uploadBoxAccount.loginsuccessful) {
                tmp = getData("http://uploadbox.com/");
                postURL = StringUtils.stringBetweenTwoStrings(tmp, "action = \"", "\"");
                generateUploadBoxID();
                postURL = postURL + uid;
                NULogger.getLogger().log(Level.INFO, "Post URL : {0}", postURL);
                server = StringUtils.stringBetweenTwoStrings(tmp, "name=\"server\" value=\"", "\"");
                NULogger.getLogger().info(server);
            } else {
                initialize();
            }
            fileUpload();
        } catch (Exception e) {
            NULogger.getLogger().severe(e.toString());

            uploadFailed();
        }

    }
}
