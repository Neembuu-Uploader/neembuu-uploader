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

/**
 *
 * @author Dinesh
 */
public class MegaShareUploaderPlugin {

    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String sID;
    private static String postURL;
    private static File file;
    private static String uploadresponse;
    private static String downloadlink;
    private static String deletelink;

    public static void main(String[] args) {
        try {
            sID=getData("http://upload.megashare.com/");
            sID = parseResponse(sID, "tmp_sid=", "&");
            System.out.println("sID : " + sID);

//http://upload.megashare.com/cgi-bin/uploader.cgi?tmp_sid=a4b9ebc524761e46e8b295b41fb9685d&rnd=1   
            postURL = "http://upload.megashare.com/cgi-bin/uploader.cgi?tmp_sid=" + sID + "&rnd=1";
            fileUpload();
            String tmp=getData(uploadresponse);
            downloadlink=parseResponse(tmp, "VALUE=\"http://www.MegaShare.com/", "\"");
            downloadlink="http://www.MegaShare.com/"+downloadlink;
            deletelink=parseResponse(tmp, "VALUE=\"http://delete.MegaShare.com/", "\"");
            deletelink="http://delete.MegaShare.com/"+deletelink;
            System.out.println("Download link : "+downloadlink);
            System.out.println("Delete link : "+deletelink);
        } catch (Exception e) {
            System.out.println("e : " + e);
        }
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
        file = new File("h:/UploadingdotcomUploaderPlugin.java");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("emai", new StringBody("Free"));
        mpEntity.addPart("upload_range", new StringBody("1"));
        mpEntity.addPart("upfile_0", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into MegaShare.com");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        uploadresponse = response.getLastHeader("Location").getValue();
        System.out.println("Upload response : "+uploadresponse);
        System.out.println(response.getStatusLine());

        httpclient.getConnectionManager().shutdown();
    }

    private static String getData(String myurl) throws Exception {
        System.out.println("getdata");
        URL url = new URL(myurl);
        uc = (HttpURLConnection) url.openConnection();
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
                System.out.println(temp);
            k += temp;
        }
        br.close();
        return k;
    }
}
