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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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
 * @author Dinesh
 */
public class ShareSendUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String tmp, sharesendlink = "";
    private static String postURL;
    private static File file;
    private static String uploadresponse = "";

    public static void main(String[] args) throws Exception {
        initialize();
        postURL = sharesendlink + "?flash=1";
        fileUpload();
    }

    private static void initialize() throws IOException, Exception {
        System.out.println("Getting startup cookie from sharesend.com");
        u = new URL("http://sharesend.com/");
        uc = (HttpURLConnection) u.openConnection();
        br = new BufferedReader(new InputStreamReader(u.openStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
        System.out.println("Getting sharesend dynamic upload link");
        sharesendlink = parseResponse(k, "action=\"", "\"");

        System.out.println(sharesendlink);
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {

        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void fileUpload() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postURL);

        file = new File("h:/install.txt");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        mpEntity.addPart("Filedata", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into sharesend.com");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }
        System.out.println("Upload Response : " + uploadresponse);
        System.out.println("Download Link : http://sharesend.com/" + uploadresponse);
        httpclient.getConnectionManager().shutdown();
    }
}
