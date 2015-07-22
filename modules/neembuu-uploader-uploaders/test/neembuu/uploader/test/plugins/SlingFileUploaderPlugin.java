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
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
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
public class SlingFileUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String sling_guest_url;
    private static String postURL;
    private static String postuploadpage;
    private static DefaultHttpClient httpclient;
    private static File file;
    private static String ssd;
    private static String uploadresponsepage;
    private static StringBuilder cookie;
    private static String downloadlink;
    private static String deletelink;
    private static boolean login = false;

    public static void main(String[] args) throws Exception {

//        System.out.println(URLDecoder.decode("Login+%C2%BB", "UTF-8"));

        initialize();
        loginSlingFile();
        System.exit(0);
        if (login) {
            initialize();
        }
//        System.exit(0);
        fileUpload();
        System.out.println("Getting download & delete links......");
        u = new URL(postuploadpage);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", cookie.toString());
        uc.setRequestProperty("Referer", sling_guest_url);
        System.out.println(uc.getResponseCode());
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
//            System.out.println(temp);
            k += temp;
        }
        downloadlink = parseResponse(k, "this.select();", "\" type=\"text\"");
        downloadlink = downloadlink.replace("\" value=\"", "");
        System.out.println("Download link : " + downloadlink);
        deletelink = parseResponse(k, "Delete Link:", "\" type=\"text");
        deletelink = deletelink.substring(deletelink.indexOf("http://"));
        System.out.println("Delete link : " + deletelink);
    }

    private static void initialize() throws IOException {

        if (login) {
            System.out.println("After login,geting the link again :)");
        } else {
            System.out.println("Getting startup cookies & link from slingfile.com");
        }
        u = new URL("http://www.slingfile.com/");
        uc = (HttpURLConnection) u.openConnection();
        if (login) {
            uc.setRequestProperty("Cookie", cookie.toString());
        }
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
//            System.out.println(temp);
            k += temp;
        }

        
        //http://sf002-01.slingfile.com/upload?X-Progress-ID=p173qvokvee3uk2h1kp776215pa4
//        System.exit(0);
//
//        sling_guest_url = parseResponse(k, "single-premium?uu=", "'");
//        System.out.println("sling guest url : " + sling_guest_url);
//        ssd = parseResponse(sling_guest_url, "&ssd=", "&rd");
//        System.out.println("SSD : " + ssd);
//        postURL = parseResponse(sling_guest_url, "http://", "&ssd");
//        postURL = "http://" + postURL;
//        System.out.println("Post URL : " + postURL);

//        postuploadpage = sling_guest_url.substring(sling_guest_url.indexOf("&rd=") + 4);
//        System.out.println("post upload page : " + postuploadpage);


        cookie = new StringBuilder();

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

    private static void fileUpload() throws Exception {

        file = new File("/home/vigneshwaran/VIGNESH/dinesh.txt");
        httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postURL);


        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        mpEntity.addPart("ssd", new StringBody(ssd));
        mpEntity.addPart("Filedata", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into slingfile.com");
        HttpResponse response = httpclient.execute(httppost);
//        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        System.out.println(EntityUtils.toString(response.getEntity())); 

    }

    public static void loginSlingFile() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to slingfile.com");
        HttpPost httppost = new HttpPost("http://www.slingfile.com/login");

        httppost.setHeader("Cookie", cookie.toString() + ";signupreferrerurl=http%3A%2F%2Fwww.slingfile.com%2F;");
        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();



        formparams.add(new BasicNameValuePair("f_user", "dineshs"));
        formparams.add(new BasicNameValuePair("f_password", ""));
//        formparams.add(new BasicNameValuePair("submit", "Login »"));


//        formparams.add(new BasicNameValuePair("remember", "1"));
//        formparams.add(new BasicNameValuePair("username", ""));
//        formparams.add(new BasicNameValuePair("password", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        System.out.println(httpresponse.getStatusLine());

        if (httpresponse.getFirstHeader("Location").getValue().contains("dashboard")) {
            login = true;
            cookie.append(";signupreferrerurl=http%3A%2F%2Fwww.slingfile.com%2F;");
        } else {
            login = false;
        }
//        System.out.println("Getting cookies........");
//        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
//        Cookie escookie = null;
//        while (it.hasNext()) {
//            escookie = it.next();
////            if (escookie.getName().equalsIgnoreCase("login")) {
////                logincookie = "login=" + escookie.getValue();
////                System.out.println(logincookie);
////            }
////            if (escookie.getName().equalsIgnoreCase("xfss")) {
////                xfsscookie = "xfss=" + escookie.getValue();
////                System.out.println(xfsscookie);
////                login = true;
////            }
//        }

        if (login) {
            System.out.println("SlingFile Login successful :)");
        } else {
            System.out.println("SlingFile Login failed :(");
        }


    }
}
