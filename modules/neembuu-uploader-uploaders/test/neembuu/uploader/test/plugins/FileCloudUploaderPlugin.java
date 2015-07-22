/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.*;
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
 * @author vigneshwaran
 */
public class FileCloudUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static PrintWriter pw;
    private static BufferedReader br;
    private static String uploadURL;
    private static String apiKey="";
    private static File file;
    private static String downloadURL="";

    public static void main(String[] args) throws Exception {
        initialize();
//        fileUpload();
     }

    private static void initialize() throws Exception {

        u = new URL("https://secure.filecloud.io/api-fetch_apikey.api?response=text");
        uc = (HttpURLConnection) u.openConnection();
        uc.setDoOutput(true);
        System.out.println("Getting api key from filecloud.....");
        uc.setRequestMethod("POST");
        uc.setInstanceFollowRedirects(false);

        pw = new PrintWriter(new OutputStreamWriter(uc.getOutputStream()), true);
        pw.print("username=007007dinesh&password=");
        pw.flush();
        pw.close();

        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String lineResponse = "", pageResponse = "";
        while ((lineResponse = br.readLine()) != null) {
            System.out.println(lineResponse);
            pageResponse += lineResponse;
        }

        if (!pageResponse.contains("ok") && !pageResponse.contains("akey: ")) {
            System.out.println("Error in retrieving API Key from FileCloud");
            return;
        }
        apiKey = pageResponse.substring(pageResponse.indexOf("akey: "));
        apiKey = apiKey.replaceAll("akey: ", "");
        System.out.println("API Key : " + apiKey);
        //System.out.println(k);
        ///bsb7UdieuznJ/S7BUd4Pw==
        u = null;
        uc = null;
        br = null;
        System.out.println("Getting upload url from FileCloud.....");
        u = new URL("http://api.filecloud.io/api-fetch_upload_url.api?response=text");
        uc = (HttpURLConnection) u.openConnection();
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        lineResponse = "";
        pageResponse = "";
        while ((lineResponse = br.readLine()) != null) {
//            System.out.println(lineResponse);
            pageResponse += lineResponse;
        }

        if (!pageResponse.contains("ok") && !pageResponse.contains("upload_url: ")) {
            System.out.println("Error in retrieving upload URL from FileCloud");
            return;
        }
        uploadURL = pageResponse.substring(pageResponse.indexOf("upload_url: "));
        uploadURL = uploadURL.replaceAll("upload_url: ", "");
        System.out.println("FileCloud Upload URL : " + uploadURL);


    }

    //akey: 
    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void fileUpload() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(uploadURL);
        file = new File("/home/vigneshwaran/VIGNESH/Obito-Tobi.jpg");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("akey", new StringBody(apiKey));

        mpEntity.addPart("Filedata", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into filecloud.io .....");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        String uploadResponse = "";
        if (resEntity != null) {
            uploadResponse = EntityUtils.toString(resEntity);
        }
//        if (resEntity != null) {
//            resEntity.consumeContent();
//        }
//        System.out.println("res " + uploadresponse);


        if (!uploadResponse.contains("ok") && !uploadResponse.contains("\"ukey\":\"")) {
            System.out.println("Error in retrieving file information after uploading.....");
            return;
        }

        downloadURL=parseResponse(uploadResponse, "\"ukey\":\"", "\"");
        downloadURL="http://filecloud.io/"+downloadURL;
        System.out.println("Download link : " + downloadURL);
 
        httpclient.getConnectionManager().shutdown();
    }

    
}
