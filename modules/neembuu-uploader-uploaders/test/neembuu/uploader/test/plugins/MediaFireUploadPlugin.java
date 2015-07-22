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
import java.net.URLConnection;
import java.net.URLEncoder;
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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

/**
 * 
 * @author Dinesh
 */
public class MediaFireUploadPlugin {

    private static URL url;
    private static URLConnection conn;
    private static String ukeycookie;
    private static String content;
    private static PrintWriter pw;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static URL u;
    private static String skeycookie = "";
    private static String usercookie;
    private static String myfiles;
    private static String uploadkey;
    private static String mfulconfig;
    private static String postURL;
    private static String uploadresponsekey;
    private static String downloadlink;
    private static boolean login;
    private static String uname = "";
    private static String pwd = "";

    public static void initilize() throws Exception {
        if (uname.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please give valid username,pwd");
            return;
        }

        try {
            url = new URL("http://mediafire.com/");
            conn = url.openConnection();
            ukeycookie = conn.getHeaderField("Set-Cookie");
            ukeycookie = ukeycookie.substring(0, ukeycookie.indexOf(";"));
            System.out.println(ukeycookie);

        } catch (Exception e) {
        }
        content = "login_email=" + URLEncoder.encode(uname, "UTF-8") + "&login_pass=" + pwd
                + "&submit_login.x=0&submit_login.y=0";
      
        System.out.println("Login to mediafire............");
        postData(content, "http://www.mediafire.com/dynamic/login.php");
        if (login) {
            System.out.println("Getting myfiles links........");
            getMyFilesLinks();
            System.out.println("Getting uploadkey value..........");
            getUploadKey();
            System.out.println("uploadkey " + uploadkey);
            System.out.println("Getting MFULConfig value........");
            getMFULConfig();
            postURL = "http://www.mediafire.com/douploadtoapi/?type=basic&" + ukeycookie + "&" + usercookie + "&uploadkey=" + uploadkey + "&filenum=0&uploader=0&MFULConfig=" + mfulconfig;
        } else {
            System.out.println("Invalid username or password. :( ");
        }
    }

    public static void main(String[] args) throws Exception {
        //uploadWithOutLogin();


        initilize();
        if (login) {
            fileUpload();
            getDownloadLink();
        }
    }

    public static void setHttpHeader(String cookie) {

        try {
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.mediafire.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://www.mediafire.com/");
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
            String httpResp = br.readLine();
            Map<String, List<String>> headerFields = uc.getHeaderFields();
            if (headerFields.containsKey("Set-Cookie")) {
                List<String> header = headerFields.get("Set-Cookie");
                for (int i = 0; i < header.size(); i++) {
                    String tmp = header.get(i);
                    if (tmp.contains("skey")) {
                        skeycookie = tmp;
                    }
                    if (tmp.contains("user")) {
                        usercookie = tmp;
                    }
                }
            } else {
                System.out.println("shit");
            }
            System.out.println();
            skeycookie = skeycookie.substring(0, skeycookie.indexOf(";"));
            System.out.println(skeycookie);
            if (!usercookie.contains("user")) {
                login = false;
            } else {
                login = true;
                usercookie = usercookie.substring(0, usercookie.indexOf(";"));
                System.out.println(usercookie);
            }
        } catch (Exception e) {
            //System.out.println("ex " + e.toString());
        }
    }

    public static void postData(String content, String posturl) {
        try {

            u = new URL(posturl);
            setHttpHeader(ukeycookie);
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
            uc.setRequestProperty("Host", "www.mediafire.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://mediafire.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            //uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            uc.setRequestProperty("Cookie", ukeycookie + ";" + skeycookie + ";" + usercookie);
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

    public static void fileUpload() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpPost httppost = new HttpPost(postURL);
        File file = new File("d:/hai.html");
        System.out.println(ukeycookie);
        httppost.setHeader("Cookie", ukeycookie + ";" + skeycookie + ";" + usercookie);
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("Now uploading your file into mediafire...........................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();

        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            System.out.println("Getting upload response key value..........");
            uploadresponsekey = EntityUtils.toString(resEntity);
            getUploadResponseKey();
            System.out.println("upload resoponse key " + uploadresponsekey);
        }
    }

    public static void getDownloadLink() throws InterruptedException {
        do {
            downloadlink = getData("http://www.mediafire.com/basicapi/pollupload.php?key=" + uploadresponsekey + "&MFULConfig=" + mfulconfig);
            System.out.println(downloadlink);
        } while (downloadlink.contains("Verifying File"));
        downloadlink = parseResponse(downloadlink, "<quickkey>", "<");
        downloadlink = "http://www.mediafire.com/?" + downloadlink;
        System.out.println("download link is " + downloadlink);
    }

    public static void getMyFilesLinks() {
        myfiles = getData("http://www.mediafire.com/myfiles.php");
        myfiles = parseResponse(myfiles, "LoadJS(\"", "\"");
    }

    public static void getUploadKey() {
        uploadkey = getData("http://www.mediafire.com" + myfiles);
        uploadkey = parseResponse(uploadkey, "var zb='", "'");
    }

    public static void getMFULConfig() {
        mfulconfig = getData("http://www.mediafire.com/basicapi/uploaderconfiguration.php?45144");
        mfulconfig = parseResponse(mfulconfig, "<MFULConfig>", "<");
    }

    public static void getUploadResponseKey() {
        uploadresponsekey = parseResponse(uploadresponsekey, "<key>", "<");
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    public static void uploadWithOutLogin() throws IOException {
        System.out.println("Getting uploader configuration details.............");
        String s = getData("http://www.mediafire.com/basicapi/uploaderconfiguration.php?45144");
        ukeycookie = parseResponse(s, "<ukey>", "<");
        usercookie = parseResponse(s, "<user>", "<");
        uploadkey = parseResponse(s, "<folderkey>", "<");
        mfulconfig = parseResponse(s, "<MFULConfig>", "<");
        postURL = "http://www.mediafire.com/douploadtoapi/?type=basic&ukey=" + ukeycookie + "&user" + usercookie + "&uploadkey=" + uploadkey + "&filenum=0&uploader=0&MFULConfig=" + mfulconfig;
        login = true;

    }
}
