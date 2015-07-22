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
public class RapidShareUploaderPlugin {

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
    private static boolean login;
    private static File file;
    private static String cookie;
    private static String s;
    private static String uploadresponse, downloadid = "", filename = "";
    private static String downloadlink;
    private static String uname = "";
    private static String pwd = "";

    public static void main(String[] args) throws IOException {
        if (uname.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please give valid username,pwd");
            return;
        }
        cookie = getData("http://api.rapidshare.com/cgi-bin/rsapi.cgi?sub=getaccountdetails&login=" + uname + "&password=" + pwd + "&withpublicid=3&withcookie=1&cbid=1&cbf=rs.jsonp.callback");
        if (cookie.contains("ERROR")) {
            System.out.println("RapidShare Login failed :(");
        } else {
            System.out.println("RapidShare Login success :)");
            cookie = cookie.substring(cookie.indexOf("cookie="));
            cookie = cookie.replace("cookie=", "");
            cookie = cookie.substring(0, cookie.indexOf("\\"));
            System.out.println("cookie : " + cookie);
            System.out.println("Now gettting dynamic rs link");
            String link = getData("http://api.rapidshare.com/cgi-bin/rsapi.cgi?sub=nextuploadserver&cbid=3&cbf=rs.jsonp.callback");
            link = link.substring(link.indexOf("\"") + 1);
            link = link.substring(0, link.indexOf("\""));
            System.out.println("rs link : " + link);
            long uploadID = (long) Math.floor(Math.random() * 90000000000L) + 10000000000L;
            postURL = "http://rs" + link + ".rapidshare.com/cgi-bin/rsapi.cgi?uploadid=" + uploadID;
            System.out.println("rapidshare : " + postURL);
            fileUpload();
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
            uc.setRequestProperty("Host", "www.rapidshare.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://rapidshare.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
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
        file = new File("/home/vigneshwaran/VIGNESH/dinesh.txt");
        HttpPost httppost = new HttpPost(postURL);
        //httppost.setHeader("Cookie", usercookie);
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("sub", new StringBody("upload"));
        mpEntity.addPart("cookie", new StringBody(cookie));
        mpEntity.addPart("folder", new StringBody("0"));
        mpEntity.addPart("filecontent", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("Now uploading your file into rs...........................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();

        System.out.println(response.getStatusLine());
        if (resEntity != null) {

            uploadresponse = EntityUtils.toString(resEntity);
            System.out.println("Actual response : " + uploadresponse);
            uploadresponse = uploadresponse.replace("COMPLETE\n", "");

//            downloadid=uploadresponse.substring(uploadresponse.indexOf("E")+1);
            downloadid = uploadresponse.substring(0, uploadresponse.indexOf(","));
            uploadresponse = uploadresponse.replace(downloadid + ",", "");
            filename = uploadresponse.substring(0, uploadresponse.indexOf(","));
            System.out.println("download id : " + downloadid);
//            filename=uploadresponse.substring(uploadresponse.indexOf(","));
//            filename=uploadresponse.substring(0, uploadresponse.indexOf(","));
            System.out.println("File name : " + filename);
            downloadlink = "http://rapidshare.com/files/" + downloadid + "/" + filename;
            System.out.println("Download Link :" + downloadlink);
        }
    }
}
