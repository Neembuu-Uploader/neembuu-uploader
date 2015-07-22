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
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
public class DepositFilesUploaderPlugin {

    static final String UPLOAD_ID_CHARS = "1234567890qwertyuiopasdfghjklzxcvbnm";
    private static URL u;
    private static HttpURLConnection uc;
    private static PrintWriter pw;
    private static BufferedReader br;
    private static String uprandcookie = "";
    private static String autologincookie = "";
    private static String postURL = "";
    private static File file;
    private static String uploadresponse = "";
    private static String uid = "";
    private static String downloadlink = "";
    private static String deletelink = "";
    private static String uname = "";
    private static String pwd = "";

    public static void main(String[] args) throws IOException {

        if (uname.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please give valid username,pwd");
            return;
        }
//
        System.out.println("Now getting cookie from Depositfiles........");
        initialize();
        loginDepositFiles();
        System.out.println("Now getting deposifiles page post action value........");
        postURL = getData("http://www.depositfiles.com/en/");
        postURL = parseResponse(postURL, "file_upload_action = '", "'");
        postURL = postURL.substring(0, postURL.indexOf("=") + 1);
        generateDepositFilesID();
        System.out.println("Post URL  : " + postURL);
        fileUpload();


    }

    public static void loginDepositFiles() {
        System.out.println("login to deposifiles........");
        postData("go=1&login=" + uname + "&password=" + pwd, "http://www.depositfiles.com/en/login.php?return=%2Fen%2F");
    }

    public static void fileUpload() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        file = new File("H:\\FileServeUploader.java");


//        if(!username.isEmpty() &&  !password.isEmpty())
//            postURL = megauploadlink + "upload_done.php?UPLOAD_IDENTIFIER=" + uploadID + "&user=undefined&s=" + filelength;
//        else
//           postURL = megauploadlink + "upload_done.php?UPLOAD_IDENTIFIER=" + uploadID + "&"+usercookie+"&s=" + filelength; 
        HttpPost httppost = new HttpPost(postURL);

        httppost.setHeader("Cookie", uprandcookie + ";" + autologincookie);
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("MAX_FILE_SIZE", new StringBody("2097152000"));
        mpEntity.addPart("UPLOAD_IDENTIFIER", new StringBody(uid));
        mpEntity.addPart("go", new StringBody("1"));
        mpEntity.addPart("files", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("Now uploading your file into depositfiles...........................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();

        System.out.println(response.getStatusLine());
        if (resEntity != null) {

            uploadresponse = EntityUtils.toString(resEntity);
            downloadlink = parseResponse(uploadresponse, "ud_download_url = '", "'");
            deletelink = parseResponse(uploadresponse, "ud_delete_url = '", "'");
            System.out.println("download link : " + downloadlink);
            System.out.println("delete link : " + deletelink);

        }
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    public static void initialize() throws IOException {
        u = new URL("http://www.depositfiles.com/");
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                String tmp = header.get(i);
                if (tmp.contains("uprand")) {
                    uprandcookie = tmp;
                    uprandcookie = uprandcookie.substring(0, uprandcookie.indexOf(";"));
                }
            }
            System.out.println("uprand cookie : " + uprandcookie);
        }
    }

    public static void generateDepositFilesID() {
        StringBuilder sb = new StringBuilder();
        sb.append(new Date().getTime() / 1000);
        for (int i = 0; i < 32; i++) {
            // int Min = 1, Max = 35;
            //Min + (int)(Math.random() * ((Max - Min) + 1))
            //1+(int)(Math.random() * ((35 - 1) + 1))
            //1+(int)(Math.random() * 35)
            int idx = 1 + (int) (Math.random() * 35);
            sb.append(UPLOAD_ID_CHARS.charAt(idx));
        }
        uid = sb.toString();
        postURL += sb.toString();
    }

    public static void setHttpHeader(String cookie) {

        try {
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.depositfiles.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://www.depositfiles.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            uc.setRequestProperty("Cookie", cookie);
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
            Map<String, List<String>> headerFields = uc.getHeaderFields();
            if (headerFields.containsKey("Set-Cookie")) {
                List<String> header = headerFields.get("Set-Cookie");
                for (int i = 0; i < header.size(); i++) {
                    String tmp = header.get(i);
                    if (tmp.contains("autologin")) {
                        autologincookie = tmp;
                        autologincookie = autologincookie.substring(0, autologincookie.indexOf(";"));
                        System.out.println("autologin cookie : " + autologincookie);
                    } else {
                        System.out.println("Invalid username or password");
                    }
                }

            }


        } catch (Exception e) {
            System.out.println("ex " + e.toString());
        }
    }

    public static void postData(String content, String posturl) {
        try {

            u = new URL(posturl);
            setHttpHeader(uprandcookie);
            writeHttpContent(content);
            u = null;
            uc = null;
        } catch (Exception e) {
            System.out.println("exception " + e.toString());
        }
    }

    public static String getData(String url) {

        try {
            u = new URL(url);
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.depositfiles.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://depositfiles.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            //uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            uc.setRequestProperty("Cookie", uprandcookie);
            uc.setRequestMethod("GET");
            uc.setInstanceFollowRedirects(false);
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
                //System.out.println(temp);
                k += temp;
            }
            br.close();
            u = null;
            uc = null;
            return k;
        } catch (Exception e) {
            System.out.println("exception : " + e.toString());
            return "";
        }

    }
}
