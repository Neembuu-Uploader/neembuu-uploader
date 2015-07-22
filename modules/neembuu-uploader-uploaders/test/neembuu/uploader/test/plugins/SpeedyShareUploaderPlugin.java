/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
public class SpeedyShareUploaderPlugin {

    private static URL u;
    private static boolean login;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String uploadURL;
    static final String UPLOAD_ID_CHARS = "1234567890";
    private static String uid;
    private static File file;
    private static String scookie;

    public static void main(String[] args) throws Exception {
        initialize();

        fileUpload();

        getDownloadLinks();
//        System.out.println(Math.random());
    }

    public static void generateRandomID() {
        StringBuilder sb = new StringBuilder();
        //sb.append(new Date().getTime() / 1000);
        for (int i = 0; i < 10; i++) {
            int idx = 1 + (int) (Math.random() * 9);
            sb.append(UPLOAD_ID_CHARS.charAt(idx));
        }
        uid = sb.toString();
        System.out.println("uid : " + uid + " - " + uid.length());

    }

    private static void initialize() throws Exception {

//        generateRandomID();
        System.out.println("Getting upload url from speedyshare.com");

        u = new URL("http://www.speedyshare.com/upload_page.php");

        uc = (HttpURLConnection) u.openConnection();
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "", tmp = "";
        while ((tmp = br.readLine()) != null) {
            System.out.println(tmp);
            k += tmp;
        }

        uid = parseResponse(k, "/upload_fileapi.php?", "\"");

        uploadURL = "http://www.speedyshare.com/upload_fileapi.php?" + uid + "&&file=0&startpos=0&r=" + Math.random();

        System.out.println("Finallllllllly =>" + uploadURL);
        //http://www18.speedyshare.com/upload_fileapi.php?1923653624=0&file=0&startpos=0&r=0.7389458813972893
        //http://www15.speedyshare.com/upload_fileapi.php?4990834035=0&file=0&startpos=0&r=0.7938335145696153
//        System.exit(0);
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                if (tmp.contains("S=")) {
                    scookie = tmp;
                }
            }
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
        HttpPost httppost = new HttpPost(uploadURL);
        httppost.setHeader("Cookie", scookie);
        file = new File("/media/backup/Projects/NU/NeembuuUploader/test/neembuuuploader/test/plugins/DropBoxUploaderPlugin.java");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
//        mpEntity.addPart("Filename", new StringBody(file.getName()));

//        mpEntity.addPart("PHPSESSID", new StringBody(dataid));

        mpEntity.addPart("upload", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into speedyshare.com");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        //  uploadresponse = response.getLastHeader("Location").getValue();
        System.out.println("Upload response : " + EntityUtils.toString(response.getEntity()));
        System.out.println(response.getStatusLine());
//        if (resEntity != null) {
//            uploadresponse = EntityUtils.toString(resEntity);
//        }
//        if (resEntity != null) {
//            resEntity.consumeContent();
//        }
//        System.out.println("res " + uploadresponse);


        httpclient.getConnectionManager().shutdown();
    }

    private static void getDownloadLinks() throws Exception {
        //http://www21.speedyshare.com/upload_fileapi.php?8614749610=0
        u = new URL(uploadURL.substring(0, uploadURL.indexOf("&file")-1));
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", scookie);
        uc.setDoOutput(true);
        uc.setInstanceFollowRedirects(false);
        uc.setRequestMethod("POST");
        System.out.println(uc.getURL());


        PrintWriter pw = new PrintWriter(new OutputStreamWriter(uc.getOutputStream()), true);
        pw.print("do=getlinks&name%5B0%5D=" + file.getName());
        pw.flush();
        pw.close();
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
//        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "", tmp = "";
        while ((tmp = br.readLine()) != null) {
            System.out.println(tmp);
            k += tmp;
        }

        
        //System.out.println(uc.getHeaderField("Location")); 

//        uploadURL = parseResponse(k, "href=\"", "\"");

        String[] respArr = k.split(":");
        String dnlnk = respArr[0];
        String dllnk = respArr[1];
        String flnm = respArr[2].substring(0, respArr[2].length()-1);
        System.out.println("http://speedy.sh/"+dnlnk+"/"+flnm);
        System.out.println(dllnk);
    }
}
