/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Level;
import neembuu.uploader.utils.NULogger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class FlameUpoadUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static File file;
    private static DefaultHttpClient httpclient;
    private static String uploadid;
    private static String downloadlink;

    public static void main(String[] args) throws Exception {
        initialize();
        fileUpload();

        //http://flameupload.com/process.php?upload_id=cf3feacdebff8f722a5a4051ccefda3f
        u = new URL("http://flameupload.com/process.php?upload_id=" + uploadid);
        uc = (HttpURLConnection) u.openConnection();
        System.out.println(uc.getURL());

        uc.setRequestProperty("Referer", "http://flameupload.com/cgi/ubr_upload.pl?upload_id=" + uploadid);
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
//            NULogger.getLogger().info(temp);
            System.out.println(temp);
            k += temp;
        }
        System.exit(0);
        downloadlink = parseResponse(k, "files/", "\"");
        downloadlink = "http://flameupload.com/files/" + downloadlink;
        NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);


    }

    private static void initialize() throws IOException {
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
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void fileUpload() throws Exception {

        file = new File("c:/Documents and Settings/dinesh/Desktop/ZShareUploaderPlugin.java");
        httpclient = new DefaultHttpClient();
//http://flameupload.com/cgi/ubr_upload.pl?upload_id=cf3feacdebff8f722a5a4051ccefda3f        
        HttpPost httppost = new HttpPost("http://flameupload.com/cgi/ubr_upload.pl?upload_id=" + uploadid);


        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        ContentBody cbFile = new FileBody(file);

        /*
         * 
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="uploaded"
        
        on
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="hotfile"
        
        on
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="turbobit"
        
        on
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="depositfiles"
        
        on
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="fileserve"
        
        on
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="filefactory"
        
        on
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="netload"
        
        on
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="uploadstation"
        
        on
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="uploading"
        
        on
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="badongo"
        
        on
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="_2shared"
        
        on
        ------WebKitFormBoundaryyAuWHgANtPnAprcx
        Content-Disposition: form-data; name="megashare"
        
        on
        
         */


        mpEntity.addPart("upfile_0", cbFile);
        mpEntity.addPart("uploaded", new StringBody("on"));
        mpEntity.addPart("hotfile", new StringBody("on"));
        mpEntity.addPart("turbobit", new StringBody("on"));
        mpEntity.addPart("depositfiles", new StringBody("on"));
        mpEntity.addPart("fileserve", new StringBody("on"));
        mpEntity.addPart("filefactory", new StringBody("on"));
        mpEntity.addPart("netload", new StringBody("on"));
        mpEntity.addPart("uploadstation", new StringBody("on"));
        mpEntity.addPart("badongo", new StringBody("on"));
        mpEntity.addPart("uploading", new StringBody("on"));
        mpEntity.addPart("megashare", new StringBody("on"));
        mpEntity.addPart("_2shared", new StringBody("on"));

        httppost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httppost.getRequestLine());
        NULogger.getLogger().info("Now uploading your file into flameupload.com");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        NULogger.getLogger().info(response.getStatusLine().toString());
        NULogger.getLogger().info(EntityUtils.toString(resEntity));
        Header[] allHeaders = response.getAllHeaders();
        for (int i = 0; i < allHeaders.length; i++) {
            System.out.println(allHeaders[i].getName() + "=" + allHeaders[i].getValue());
        }

        if (response.getStatusLine().getStatusCode() == 302) {
            NULogger.getLogger().info("Files uploaded successfully");
        } else {
            throw new Exception("There might be a problem with your internet connection or server error. Please try again later :(");
        }

    }
}
