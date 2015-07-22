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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class CrockoUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String postURL;
    private static File file;
    private static String uploadresponse;
    private static String downloadlink;
    private static String deletelink;
    private static String sessionid;
    private static boolean login;
    private static StringBuilder cookies;

    public static void main(String[] args) throws Exception {
        loginCrocko();
//        initialize();
        fileUpload();
    }

    private static void initialize() throws IOException {



        u = new URL("http://crocko.com/");
        uc = (HttpURLConnection) u.openConnection();

        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
//            System.out.println(temp);
            k += temp;
        }


        postURL = parseResponse(k, "upload_url : \"", "\"");
        System.out.println("Post URL : " + postURL);
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void fileUpload() throws Exception {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postURL);
//        if (login) {
//            httppost.setHeader("Cookie", cookies.toString());
//        }
//        httppost.setHeader("Content-Type", "multipart/form-data");
//        httppost.setHeader("Host", "upload.crocko.com");
//        httppost.setHeader("User-Agent", "Shockwave Flash");
        file = new File("C:\\Documents and Settings\\dinesh\\Desktop\\MegaUploadUploaderPlugin.java");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        if (login) {
            System.out.println("adding php sess .............");
            mpEntity.addPart("PHPSESSID", new StringBody(sessionid));
        }
        mpEntity.addPart("Filedata", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into crocko");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();


        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }
//  
        System.out.println("Upload response : " + uploadresponse);
        downloadlink = parseResponse(uploadresponse, "Download link:", "</dd>");
        downloadlink = parseResponse(downloadlink, "value=\"", "\"");
        deletelink = parseResponse(uploadresponse, "Delete link:", "</a></dd>");
        deletelink = deletelink.substring(deletelink.indexOf("http://"));
        System.out.println("Download link : " + downloadlink);
        System.out.println("Delete link : " + deletelink);
    }

    public static void loginCrocko() throws Exception {


        cookies = new StringBuilder();
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to crocko.com");
        HttpPost httppost = new HttpPost("https://www.crocko.com/accounts/login");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("login", "007007dinesh"));
        formparams.add(new BasicNameValuePair("password", "*********************"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        System.out.println("Getting cookies........");
        System.out.println(EntityUtils.toString(httpresponse.getEntity()));
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            cookies.append(escookie.getName()).append("=").append(escookie.getValue()).append(";");
//       
            if (escookie.getName().equals("PHPSESSID")) {
                sessionid = escookie.getValue();
                System.out.println(sessionid);
            }
        }

        if (cookies.toString().contains("logacc")) {
            System.out.println(cookies);
            login = true;
            System.out.println("Crocko login successful :)");
            getData();
        }
        if (!login) {
            System.out.println("Crocko.com Login failed :(");
        }


    }

    private static void getData() throws Exception {
//        https://www.crocko.com/accounts/upload

        u = new URL("http://www.crocko.com/accounts/upload");
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", cookies.toString());
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
//            System.out.println(temp);
            k += temp;
        }
        postURL = parseResponse(k, "upload_url : \"", "\"");
        System.out.println("Post URL : " + postURL);
        uc.disconnect();
    }
}
