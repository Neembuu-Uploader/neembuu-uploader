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
import javax.script.ScriptException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
public class MegaUploadUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static long filelength;
    private static String postURL = "";
    private static String uploadID;
    private static File file;
    private static String downloadlink;
    private static PrintWriter pw;
    private static String usercookie = "";
    private static String username = "";
    private static String password = "";
    private static String megauploadlink;

    public static void main(String[] args) throws ScriptException, IOException {
//http://www25.megaupload.com/upload_done.php?UPLOAD_IDENTIFIER=01314345437487192749907779302818&user=undefined&s=1935
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Please give valid username,pwd");
            return;
        }

        initialize();
        loginMegaUpload();
        fileUpload();
        getDownloadLink();
    }

    public static void initialize() {
        System.out.println("Parsing http://www.megaupload.com to get megaupload host value......");
        String data = getData("http://www.megaupload.com");
        megauploadlink = parseResponse(data, "flashvars.server = \"", "\"");
        System.out.println(megauploadlink);
        System.out.println("Generating random ID value for uploading.......");
        generateMegaUploadID();

    }

    public static void loginMegaUpload() {
        if (!username.isEmpty() && !password.isEmpty()) {
            postData("login=1&redir=1&username=" + username + "&password=" + password, "http://megaupload.com/?c=login");
        }
    }

    public static void fileUpload() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        file = new File("H:\\c programs.doc");
        filelength = file.length();
        if (!username.isEmpty() && !password.isEmpty()) {
            postURL = megauploadlink + "upload_done.php?UPLOAD_IDENTIFIER=" + uploadID + "&user=undefined&s=" + filelength;
        } else {
            postURL = megauploadlink + "upload_done.php?UPLOAD_IDENTIFIER=" + uploadID + "&" + usercookie + "&s=" + filelength;
        }
        HttpPost httppost = new HttpPost(postURL);
        httppost.setHeader("Cookie", usercookie);
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("Now uploading your file into megaupload...........................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();

        System.out.println(response.getStatusLine());
        if (resEntity != null) {

            downloadlink = EntityUtils.toString(resEntity);


        }
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    public static void generateMegaUploadID() {
        int[] randno = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        int earlyID = (Math.random() < 0.5) ? 0 : 1;
        uploadID = String.valueOf(earlyID + new Date().getTime());
        int loop = 32 - String.valueOf(uploadID).length();
        int tmp = 1;
        while (tmp <= loop) {
            long sum = randno[(int) Math.round(Math.floor(Math.random() * 10))];
            uploadID = uploadID + sum;
            tmp++;
        }
        System.out.println("Megaupload key " + uploadID);
    }

    public static void setHttpHeader(String cookie) {

        try {
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.megaupload.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://www.megaupload.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
                    if (tmp.contains("user")) {
                        usercookie = tmp;
                        usercookie = usercookie.substring(0, usercookie.indexOf(";"));
                    }
                }
                System.out.println("user cookie : " + usercookie);
            } else {
                System.out.println("Invalid username or password");
            }


        } catch (Exception e) {
            //System.out.println("ex " + e.toString());
        }
    }

    public static void postData(String content, String posturl) {
        try {

            u = new URL(posturl);
            setHttpHeader(usercookie);
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
            uc.setRequestProperty("Host", "www.megaupload.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://mediafire.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            //uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //   uc.setRequestProperty("Cookie", ukeycookie + ";" + skeycookie + ";" + usercookie);
            uc.setRequestMethod("GET");
            uc.setInstanceFollowRedirects(false);
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
                System.out.println(temp);
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

    public static void getDownloadLink() {
        downloadlink = parseResponse(downloadlink, "downloadurl = '", "'");
        System.out.println("Download Link : " + downloadlink);
    }
}
