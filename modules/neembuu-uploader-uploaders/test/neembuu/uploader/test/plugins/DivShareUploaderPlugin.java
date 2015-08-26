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
import java.util.Map;
import org.apache.http.Header;
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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class DivShareUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static StringBuilder cookie;
    private static DefaultHttpClient httpclient;
    private static boolean login = false;
    private static String downURL;
    private static String sid;
    private static File file;
    private static String downloadlink;
    private static String uploadcomplete_page;

    public static void main(String[] args) throws Exception {

        initialize();

        loginDivShare();

        System.out.println("Now getting upload url....");
        u = new URL("http://www.divshare.com/upload");
        uc = (HttpURLConnection) u.openConnection();
        uc.setInstanceFollowRedirects(false);
        uc.setRequestProperty("Cookie", cookie.toString());

        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Location")) {
            List<String> header = headerFields.get("Location");
            for (int i = 0; i < header.size(); i++) {
                downURL = header.get(i);
            }
        }

        System.out.println("Upload URL : " + downURL);

        u = new URL(downURL);
        uc = (HttpURLConnection) u.openConnection();
        uc.setInstanceFollowRedirects(false);
        uc.setRequestProperty("Cookie", cookie.toString());
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
//            System.out.println(temp);
            k += temp;
        }
        sid = parseResponse(k, "sid=", "\"");
        System.out.println("SID : " + sid);

        downURL = downURL.substring(0, downURL.lastIndexOf("upload"));
        System.out.println("Modified upload url : " + downURL);
        fileUpload();

        System.out.println("Now getting download links..............");

        //http://upload3.divshare.com/scripts/ajax/upload_server_progress.php
        //Getting upload progress ............
        HttpPost hp = new HttpPost(downURL + "scripts/ajax/upload_server_progress.php");
        hp.setHeader("Cookie", cookie.toString());
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("sid", sid));
        formparams.add(new BasicNameValuePair("_", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        hp.setEntity(entity);

        HttpResponse response = httpclient.execute(hp);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        System.out.println(EntityUtils.toString(resEntity)); 
        
     //   System.exit(0);
        
        hp = new HttpPost(downURL + "upload.php");
        hp.setHeader("Cookie", cookie.toString());

        formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("upload_method", "progress"));
        formparams.add(new BasicNameValuePair("gallery_id", "0"));
        formparams.add(new BasicNameValuePair("gallery_title", ""));
        formparams.add(new BasicNameValuePair("gallery_password", ""));
        formparams.add(new BasicNameValuePair("email_to", "julie@gmail.com,patrick@aol.com"));
        formparams.add(new BasicNameValuePair("terms", "on"));
        formparams.add(new BasicNameValuePair("data_form_sid", sid));
        entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        hp.setEntity(entity);

        response = httpclient.execute(hp);
        resEntity = response.getEntity();
        System.out.println(response.getStatusLine());


        downloadlink = EntityUtils.toString(resEntity);
        
        
        System.out.println(downloadlink);

        downloadlink = parseResponse(downloadlink, "http://www.divshare.com/download/", "\"");

        downloadlink = "http://www.divshare.com/download/" + downloadlink;

        System.out.println("Download link : " + downloadlink);



    }

    private static void fileUpload() throws Exception {

        file = new File("C:\\Documents and Settings\\All Users\\Documents\\My Pictures\\Sample Pictures\\Sunset.jpg");
        httpclient = new DefaultHttpClient();
//http://upload3.divshare.com/cgi-bin/upload.cgi?sid=8ef15852c69579ebb2db1175ce065ba6

        HttpPost httppost = new HttpPost(downURL + "cgi-bin/upload.cgi?sid=" + sid);


        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("file[0]", cbFile);

        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into divshare.com");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        System.out.println(EntityUtils.toString(resEntity));

    }

    private static void initialize() throws IOException {

        cookie = new StringBuilder();
        System.out.println("Getting startup cookies & link from divshare.com");

        u = new URL("http://www.divshare.com/");
        uc = (HttpURLConnection) u.openConnection();

        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                cookie.append(header.get(i)).append(";");
            }
            System.out.println(cookie.toString());



        }
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    public static void loginDivShare() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to divshare.com");
        HttpPost httppost = new HttpPost("http://www.divshare.com/login");

        httppost.setHeader("Cookie", cookie.toString());
        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();



        formparams.add(new BasicNameValuePair("login_submit", "Login >"));
        formparams.add(new BasicNameValuePair("user_email", "007007dinesh@gmail.com"));
        formparams.add(new BasicNameValuePair("user_password", "******************"));



//        formparams.add(new BasicNameValuePair("remember", "1"));
//        formparams.add(new BasicNameValuePair("username", ""));
//        formparams.add(new BasicNameValuePair("password", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        System.out.println(httpresponse.getStatusLine());
        Header[] allHeaders = httpresponse.getAllHeaders();
        for (int i = 0; i < allHeaders.length; i++) {
            if (allHeaders[i].getName().contains("Location")) {
                if (allHeaders[i].getValue().contains("members")) {
                    login = true;
                }
                String tmp = cookie.toString();
                tmp = tmp.replace("cntHeaderMessages=0; path=/; domain=.divshare.com;", "");
                cookie.setLength(0);
                cookie.append(tmp);
                System.out.println(cookie);

                break;
            }
        }


        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            cookie.append(escookie.getName()).append("=").append(escookie.getValue()).append(";");

        }

        if (login) {
            System.out.println("DivShare Login successful :)");

        } else {
            System.out.println("DivShare Login failed :(");
        }

        System.out.println("Cookie : " + cookie.toString());
    }
}
