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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
public class TwoSharedUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String postURL;
    private static String uploadID;
    private static String downloadURL;
    private static String adminURL;
    private static StringBuilder cookies = null;

    public static void main(String args[]) throws IOException, Exception {


        loginTwoShared();
        System.exit(0);
        getPostURL();
        fileUpload();
        System.out.println("Getting download URL....");
        String tmp = getData("http://www.2shared.com/uploadComplete.jsp?" + uploadID);
        downloadURL = tmp;
        adminURL = tmp;
        System.out.println("Upload complete. Please wait......................");
        getDownloadPageURL();
        getAdminPageURL();

    }

    public static void getPostURL() {
        System.out.println("Gettign File upload URL");
        postURL = getData("http://www.2shared.com");
        postURL = postURL.substring(postURL.indexOf("action=\""));
        postURL = postURL.replace("action=\"", "");
        postURL = postURL.substring(0, postURL.indexOf("\""));
        System.out.println(postURL);
        uploadID = postURL.substring(postURL.indexOf("sId="));
        System.out.println(uploadID);
    }

    public static String getData(String url) {
        try {
            u = new URL(url);
            uc = (HttpURLConnection) u.openConnection();
            uc.setRequestProperty("Cookie", cookies.toString());
            uc.setRequestMethod("GET");
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
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
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postURL);
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        //reqEntity.addPart("string_field",new StringBody("field value"));
        FileBody bin = new FileBody(new File("/home/vigneshwaran/VIGNESH/naruto.txt"));
        reqEntity.addPart("fff", bin);
        httppost.setEntity(reqEntity);
        System.out.println("Now uploading your file into 2shared.com. Please wait......................");
        HttpResponse response = httpclient.execute(httppost);
//        HttpEntity resEntity = response.getEntity();
//
//        if (resEntity != null) {
//            String page = EntityUtils.toString(resEntity);
//            System.out.println("PAGE :" + page);
//        }
    }

    public static void getDownloadPageURL() {
        downloadURL = downloadURL.substring(downloadURL.indexOf("action=\""));
        downloadURL = downloadURL.replace("action=\"", "");
        downloadURL = downloadURL.substring(0, downloadURL.indexOf("\""));
        System.out.println("File download  link : " + downloadURL);
    }

    public static void getAdminPageURL() {
        adminURL = adminURL.replace("<form action=\"" + downloadURL, "");
        adminURL = adminURL.substring(adminURL.indexOf("action=\""));
        adminURL = adminURL.replace("action=\"", "");
        adminURL = adminURL.substring(0, adminURL.indexOf("\""));
        System.out.println("File adminstration link : " + adminURL);
    }

    public static void loginTwoShared() throws Exception {

        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to 2shared.com");
        HttpPost httppost = new HttpPost("http://www.2shared.com/login.jsp");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("login", "007007dinesh@gmail.com"));
        formparams.add(new BasicNameValuePair("password", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        
        cookies= new StringBuilder();
        
        while (it.hasNext()) {
            escookie = it.next();
            cookies.append(escookie.getName()).append("=").append(escookie.getValue()).append(";"); 
        }

        if (cookies.toString().contains("Login=")) {
            System.out.println("2Shared login success :)");
            
            System.out.println("Cookies : "+cookies);
        } else {
            System.out.println("2Shared login failed :(");
        }

    }
}
