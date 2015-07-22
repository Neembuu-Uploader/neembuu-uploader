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
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
public class FileServeUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static File file;
    private static String postURL = "";
    private static String uploadresponse = "";
    private static String downloadlink = "", deletelink = "";
    private static String sessioncookie = "";
    private static PrintWriter pw;
    private static String dashboardcookie = "";
    private static Boolean login = false;
    private static String tmpURL = "";
    private static String sessionid;
    private static String uname = "dineshs";
    private static String pwd = "";

    public static void main(String[] args) {

        if (uname.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please give valid username,pwd");
            //return;
        }
        try {
            initialize();
            loginFileServe();
//http://upload.fileserve.com/upload/16888216/5000/?callback=jQuery16408563735049683601_1330864972087&_=1330864980565
//http://upload.fileserve.com/upload/16888216/5000/?callback=jQuery1330865730312&_=1330865748457
            tmpURL = parseResponse(getData("http://fileserve.com/upload-file.php"), "http://upload.fileserve.com/upload/", "\"");
            postURL = "http://upload.fileserve.com/upload/" + tmpURL;
            long uploadtime = new Date().getTime();
            tmpURL = "http://upload.fileserve.com/upload/" + tmpURL + "?callback=jQuery" + Math.round((Math.random() * 1000000000000000000L) + 1000000000000000000L) + "_" + uploadtime + "&_=" + (uploadtime + Math.round(Math.random() * 100000));
            System.out.println("tmp URL : " + tmpURL);

            sessionid = getData(tmpURL);
            sessionid = parseResponse(sessionid, "sessionId:'", "'");
            System.out.println("Session ID  : " + sessionid);

            postURL += sessionid;

            System.out.println("post URL : " + postURL);
//            System.exit(0);
            fileUpload();
        } catch (Exception e) {
            System.out.println("Exception in " + e);
        }

    }

    public static void loginFileServe() {
        System.out.println("Login to FileServe.com");
        postData("loginUserName=" + uname + "&loginUserPassword=" + pwd + "&ppp=102&loginFormSubmit=Login", "http://fileserve.com/login.php");
    }

    public static void setHttpHeader(String cookie) {

        try {
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.fileserve.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://www.fileserve.com/");
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

//            System.out.println(content);
            pw = new PrintWriter(new OutputStreamWriter(uc.getOutputStream()), true);
            pw.print(content);
            pw.flush();
            pw.close();
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String s = "", tmp = "";
            while ((s = br.readLine()) != null) {
                tmp += s;
            }

            if (tmp.contains("<a href=\"/dashboard.php")) {
                System.out.println("Login Success :)");
                login = true;
                u = new URL("http://fileserve.com/dashboard.php");
                uc = (HttpURLConnection) u.openConnection();
                uc.setRequestProperty("Cookie", sessioncookie);
                Map<String, List<String>> headerFields = uc.getHeaderFields();
                if (headerFields.containsKey("Set-Cookie")) {
                    List<String> header = headerFields.get("Set-Cookie");
                    for (int i = 0; i < header.size(); i++) {
                        String t = header.get(i);
                        if (t.contains("cookie")) {
                            dashboardcookie = t;
                            dashboardcookie = dashboardcookie.substring(0, dashboardcookie.indexOf(";"));
                        }
                    }
                    System.out.println("dashboard cookie : " + dashboardcookie);
                }

            } else {
                System.out.println("Invalid username or password");
            }



        } catch (Exception e) {
            System.out.println("ex " + e.toString());
        }
    }

    public static void postData(String content, String posturl) {
        try {

            u = new URL(posturl);
            setHttpHeader(sessioncookie);
            writeHttpContent(content);
            u = null;
            uc = null;
        } catch (Exception e) {
            System.out.println("exception " + e.toString());
        }
    }

    public static void initialize() throws IOException {
        System.out.println("Getting start up cookie from FileServe.com");
        u = new URL("http://www.fileserve.com/");
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                String tmp = header.get(i);
                if (tmp.contains("PHPSESSID")) {
                    sessioncookie = tmp;
                    sessioncookie = sessioncookie.substring(0, sessioncookie.indexOf(";"));
                }
            }
            System.out.println("session cookie : " + sessioncookie);
        }
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    public static String getData(String url) {

        try {
            u = new URL(url);
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.fileserve.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://fileserve.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            uc.setRequestProperty("Cookie", dashboardcookie + ";");

            uc.setRequestMethod("GET");
            uc.setInstanceFollowRedirects(false);
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
//                System.out.println(temp);
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

    public static void fileUpload() throws Exception {
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        HttpPost post = new HttpPost(postURL);
        post.setHeader("Cookie", dashboardcookie + ";");

        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

// For File parameters
        entity.addPart("file", new FileBody(new File("/home/vigneshwaran/Documents/TNEB Online Payment 3.pdf")));

// For usual String parameters
//        entity.addPart(paramName, new StringBody(paramValue.toString(), "text/plain",
//                Charset.forName("UTF-8")));
//
        post.setEntity(entity);

// Here we go!
        String response = EntityUtils.toString(client.execute(post).getEntity(), "UTF-8");
        System.out.println("res : " + response);
        client.getConnectionManager().shutdown();
        uploadresponse = response;
        String shortencode = parseResponse(uploadresponse, "\"shortenCode\":\"", "\"");
        String fileName = parseResponse(uploadresponse, "\"fileName\":\"", "\"");
        String deleteCode = parseResponse(uploadresponse, "\"deleteCode\":\"", "\"");
        downloadlink = "http://www.fileserve.com/file/" + shortencode + "/" + fileName;
        deletelink = "http://www.fileserve.com/file/" + shortencode + "/delete/" + deleteCode;
        System.out.println("Download Link : " + downloadlink);
        System.out.println("Delete Link : " + deletelink);
    }
}
