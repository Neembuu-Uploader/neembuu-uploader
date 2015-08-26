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
public class EnterUploadUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static PrintWriter pw;
    private static BufferedReader br;
    private static String logincookie = "", xfsscookie = "";
    private static boolean login = false;
    private static String uploadid;
    private static String postURL = "", servertmpurl = "", sessid = "";
    private static File file;
    private static String uploadresponse, downloadid = "";
    private static String afteruploadpage;
    private static String downloadlink;
    private static String deletelink;
    private static String uname = "";
    private static String pwd = "";

    public static void main(String[] args) throws Exception {

        if (uname.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please give valid username,pwd");
            return;
        }
        generateEnterUploadRandomID();
        System.out.println("upload id : " + uploadid);
        postData("op=login&redirect=&login=" + uname + "&password=" + pwd + "&x=0&y=0", "http://www.enterupload.com/login.html");
        String tmp = getData("http://www.enterupload.com/");
        servertmpurl = parseResponse(tmp, "srv_tmp_url='", "'");
        sessid = parseResponse(tmp, "sess_id\" value=\"", "\"");
        System.out.println(servertmpurl);
        System.out.println(sessid);
        postURL = parseResponse(tmp, "action=\"", "\"");
        postURL = postURL + uploadid + "&js_on=1&utype=reg&upload_type=file";
        System.out.println("res : " + postURL);
        fileUpload();
        //fn=yd74clcv2pnh&st=OK&op=upload_result
        postData("fn=" + downloadid + "&st=OK&op=upload_result", "http://www.enterupload.com/");

        downloadlink = afteruploadpage.substring(afteruploadpage.indexOf("Download Link"));
        downloadlink = downloadlink.substring(0, downloadlink.indexOf("</textarea>"));
        downloadlink = downloadlink.substring(downloadlink.indexOf("http://"));
        deletelink = afteruploadpage.substring(afteruploadpage.indexOf("Delete Link"));
        deletelink = deletelink.substring(0, deletelink.indexOf("</textarea>"));
        deletelink = deletelink.substring(deletelink.indexOf("http://"));
        System.out.println("Download Link : " + downloadlink);
        System.out.println("Delete Link : " + deletelink);
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void generateEnterUploadRandomID() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            uploadid = String.valueOf(Math.round(Math.random() * 10));
            sb.append(uploadid);
        }
        uploadid = sb.toString();
    }

    public static void setHttpHeader(String cookie) {

        try {
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.enterupload.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://www.enterupload.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            uc.setRequestProperty("Cookie", "");
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

            if (!content.contains("op=upload_result")) {
                Map<String, List<String>> headerFields = uc.getHeaderFields();
                if (headerFields.containsKey("Set-Cookie")) {
                    List<String> header = headerFields.get("Set-Cookie");
                    for (int i = 0; i < header.size(); i++) {
                        String tmp = header.get(i);
                        if (tmp.contains("login")) {
                            logincookie = tmp;
                            logincookie = logincookie.substring(0, logincookie.indexOf(";"));
                        }
                        if (tmp.contains("xfss")) {
                            xfsscookie = tmp;
                            xfsscookie = xfsscookie.substring(0, xfsscookie.indexOf(";"));
                        }
                    }
                }
                if (!logincookie.isEmpty() && !xfsscookie.isEmpty()) {
                    login = true;
                    System.out.println("Enter upload Login success");
                } else {
                    login = false;
                    System.out.println("Enter upload Login failed");
                }
                System.out.println("login cookie : " + logincookie);
                System.out.println("xfss cookie : " + xfsscookie);
            } else {
                String tmp = "";
                System.out.println("Getting enter upload page.........");
                while ((tmp = br.readLine()) != null) {
                    afteruploadpage += tmp;
                }
            }



        } catch (Exception e) {
            System.out.println("ex " + e.toString());
        }



    }

    public static void postData(String content, String posturl) {
        try {

            u = new URL(posturl);
            if (content.contains("op=upload_result")) {
                setHttpHeader(logincookie + ";" + xfsscookie + ";");
            } else {
                setHttpHeader("");
            }
            writeHttpContent(content);
            u = null;
            uc = null;
        } catch (Exception e) {
            System.out.println("exception " + e.toString());
        }
    }

    private static String getData(String myurl) throws Exception {
        URL url = new URL(myurl);
        uc = (HttpURLConnection) url.openConnection();
        if (login) {
            uc.setRequestProperty("Cookie", logincookie + ";" + xfsscookie);
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
        HttpPost httppost = new HttpPost(postURL);
        file = new File("h:/UploadingdotcomUploaderPlugin.java");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("upload_type", new StringBody("file"));
        mpEntity.addPart("sess_id", new StringBody(sessid));
        mpEntity.addPart("srv_tmp_url", new StringBody(servertmpurl));
        mpEntity.addPart("file_0", cbFile);
        mpEntity.addPart("submit_btn", new StringBody(" Upload!"));
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into enterupload.com");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        //  uploadresponse = response.getLastHeader("Location").getValue();
        // System.out.println("Upload response : " + response.toString());
        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }
//        if (resEntity != null) {
//            resEntity.consumeContent();
//        }
        downloadid = parseResponse(uploadresponse, "<textarea name='fn'>", "<");


        httpclient.getConnectionManager().shutdown();
    }
}
