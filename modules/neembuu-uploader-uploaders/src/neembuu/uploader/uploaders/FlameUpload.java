/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
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
 * @author dinesh
 */
@SmallModule(
    exports={FlameUpload.class},
    interfaces={Uploader.class},
    name="FlameUpload.com",
    ignore = true
)
public class FlameUpload extends AbstractUploader {

    private URL u;
    private HttpURLConnection uc;
    private BufferedReader br;
    private String uploadid;
    private String downloadlink;
    private long fileSizeLimit = 419430400; //400 MB

    public FlameUpload() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "FlameUpload.com";
    }

    public void run() {

        try {


            if (file.length() > fileSizeLimit) {
                showWarningMessage( "<html><b>" + getClass().getSimpleName() + "</b> " + Translation.T().maxfilesize() + ": <b>400MB</b></html>", getClass().getSimpleName());
                
                uploadInvalid();
                return;
            }
            uploadInitialising();
            initialize();
            fileUpload();
            gettingLink();
            u = new URL("http://flameupload.com/process.php?upload_id=" + uploadid);
            uc = (HttpURLConnection) u.openConnection();
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
//            NULogger.getLogger().info(temp);
                k += temp;
            }
            downloadlink = parseResponse(k, "files/", "\"");
            downloadlink = "http://flameupload.com/files/" + downloadlink;
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            downURL = downloadlink;
            
            uploadFinished();
            uc.disconnect();
        } catch (Exception e) {
            NULogger.getLogger().severe(e.toString());
            
            uploadFailed();

        }
    }

    private void initialize() throws IOException {
        NULogger.getLogger().info("Getting startup cookies & link from flameupload.com");

        u = new URL("http://flameupload.com/ubr_link_upload.php?rnd_id=" + new Date().getTime());
        uc = (HttpURLConnection) u.openConnection();
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
//            NULogger.getLogger().info(temp);
            k += temp;
        }


        uploadid = parseResponse(k, "startUpload(\"", "\"");

        NULogger.getLogger().log(Level.INFO, "Upload ID : {0}", uploadid);

        uc.disconnect();
    }

    public String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private void fileUpload() throws Exception {

        HttpClient httpclient = new DefaultHttpClient();
//http://flameupload.com/cgi/ubr_upload.pl?upload_id=cf3feacdebff8f722a5a4051ccefda3f        
        HttpPost httppost = new HttpPost("http://flameupload.com/cgi/ubr_upload.pl?upload_id=" + uploadid);


        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        mpEntity.addPart("upfile_0", createMonitoredFileBody());
        mpEntity.addPart("uploaded", new StringBody("on"));
        mpEntity.addPart("wupload", new StringBody("on"));
        mpEntity.addPart("turbobit", new StringBody("on"));
        mpEntity.addPart("depositfiles", new StringBody("on"));
        mpEntity.addPart("fileserve", new StringBody("on"));
        mpEntity.addPart("easyshare", new StringBody("on"));
        mpEntity.addPart("zshare", new StringBody("on"));
        mpEntity.addPart("badongo", new StringBody("on"));
        mpEntity.addPart("filefactory", new StringBody("on"));
        mpEntity.addPart("netload", new StringBody("on"));
        mpEntity.addPart("loadto", new StringBody("on"));
        mpEntity.addPart("_2shared", new StringBody("on"));

        httppost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httppost.getRequestLine());
        NULogger.getLogger().info("Now uploading your file into flameupload.com");
        uploading();
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        NULogger.getLogger().info(response.getStatusLine().toString());
        NULogger.getLogger().info(EntityUtils.toString(resEntity));

        if (response.getStatusLine().getStatusCode() == 302) {
            NULogger.getLogger().info("Files uploaded successfully");
        } else {
            throw new Exception("There might be a problem with your internet connection or server error. Please try again later :(");
        }

    }
}
