/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
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
public class BadongoUploaderPlugin {

    static final String UPLOAD_ID_CHARS = "1234567890qwertyuiopasdfghjklzxcvbnm";
    private static String uid;
    private static String uploadresponse, postURL;
    private static File file;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String downloadlink;
    private static PrintWriter pw;
    private static URL u;
    private static String usercookie;
    private static String pwdcookie;
    private static boolean login = false;
    private static String dataid;
    private static String uname = "";
    private static String pwd = "";

    public static void main(String[] args) throws IOException, Exception {

        //c17f254bcdb8091e041362993894ba20

        if (uname.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please give valid username,pwd");
            return;
        }
        generateBadongoID();
        postURL = "http://upload.badongo.com/mpu_upload_single.php?UL_ID=undefined&UPLOAD_IDENTIFIER=undefined&page=upload_s&s=&cou=en&PHPSESSID=" + uid + "&desc=";
        System.out.println("post : " + postURL);
        
        postData("username=" + uname + "&password=" + pwd + "&cap_id=&cap_secret=" + uid + "&bk=&do=login&cou=en&no_ssl=1", "http://www.badongo.com/login");
        if (login) {
            dataid = getData("http://upload.badongo.com/mpu.php?cou=en&k=member");
            dataid = parseResponse(dataid, "\"PHPSESSID\" : \"", "\"");
            System.out.println("Data : " + dataid);
        }
        fileUpload();
        if (login) {
            downloadlink = getData("http://upload.badongo.com/upload_complete.php?session=" + dataid);
        } else {
            downloadlink = getData("http://upload.badongo.com/upload_complete.php?page=upload_s_f&PHPSESSID=" + uid + "&url=undefined&url_kill=undefined&affliate=");
        }
        System.out.println("Before parsing : " + downloadlink);
        if (login) {
            downloadlink = parseResponse(downloadlink, "msg_u=", "&");
        } else {
            downloadlink = parseResponse(downloadlink, "url=", "&");
        }
        downloadlink = URLDecoder.decode(downloadlink, "UTF-8");
        System.out.println("Download link : " + downloadlink);


    }

    public static void generateBadongoID() {
        StringBuilder sb = new StringBuilder();
        //sb.append(new Date().getTime() / 1000);
        for (int i = 0; i < 32; i++) {
            int idx = 1 + (int) (Math.random() * 35);
            sb.append(UPLOAD_ID_CHARS.charAt(idx));
        }
        uid = sb.toString();
//        System.out.println("uid : "+uid+" - "+uid.length());
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static String getData(String myurl) throws Exception {
        URL url = new URL(myurl);
        uc = (HttpURLConnection) url.openConnection();
        if (login) {
            uc.setRequestProperty("Cookie", usercookie + ";" + pwdcookie);
        }
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
//                System.out.println(temp);
            k += temp;
        }
        br.close();
        return k;
    }

    private static void fileUpload() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        if (login) {
            postURL = "http://upload.badongo.com/mpu_upload.php";
        }
        HttpPost httppost = new HttpPost(postURL);
        file = new File("g:/S2SClient.7z");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        if (login) {
            mpEntity.addPart("PHPSESSID", new StringBody(dataid));
        }
        mpEntity.addPart("Filedata", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into badongo.com");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        //  uploadresponse = response.getLastHeader("Location").getValue();
        System.out.println("Upload response : " + uploadresponse);
        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }
//        if (resEntity != null) {
//            resEntity.consumeContent();
//        }
        System.out.println("res " + uploadresponse);


        httpclient.getConnectionManager().shutdown();
    }

    public static void setHttpHeader(String cookie) {

        try {
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.badongo.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://www.badongo.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            uc.setRequestProperty("Cookie", "badongoL=en; bT=%2F;");
            uc.setRequestMethod("POST");
            uc.setInstanceFollowRedirects(false);


        } catch (Exception e) {
            System.out.println("ex" + e.toString());
        }
    }
    /*
     * This method is used to write the POST data like username and password.
     * This takes the "content" value as a parameter which needs to be posted.
     * 
     */

    public static void writeHttpContent(String content) {

        try {

            System.out.println(content);
            pw = new PrintWriter(new OutputStreamWriter(uc.getOutputStream()), true);
            pw.print(content);
            pw.flush();
            pw.close();
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
//            String httpResp = br.readLine();


            Map<String, List<String>> headerFields = uc.getHeaderFields();
            if (headerFields.containsKey("Set-Cookie")) {
                List<String> header = headerFields.get("Set-Cookie");
                for (int i = 0; i < header.size(); i++) {
                    String tmp = header.get(i);
                    if (tmp.contains("badongoU")) {
                        usercookie = tmp;
                        usercookie = usercookie.substring(0, usercookie.indexOf(";"));
                    }
                    if (tmp.contains("badongoP")) {
                        pwdcookie = tmp;
                        pwdcookie = pwdcookie.substring(0, pwdcookie.indexOf(";"));
                    }
                }
            }
            if (!usercookie.isEmpty() && !pwdcookie.isEmpty()) {
                login = true;
            } else {
                login = false;
            }
            System.out.println("User cookie : " + usercookie);
            System.out.println("Pwd cookie : " + pwdcookie);
        } catch (Exception e) {
            System.out.println("ex " + e.toString());
        }



    }

    public static void postData(String content, String posturl) {
        try {

            u = new URL(posturl);
            setHttpHeader("");
            writeHttpContent(content);
            u = null;
            uc = null;
        } catch (Exception e) {
            System.out.println("exception " + e.toString());
        }
    }
}
