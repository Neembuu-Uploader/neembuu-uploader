/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.BufferedReader;
import java.io.File;
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
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
public class UploadedDotToUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static String tmp;
    private static String phpsessioncookie, admincode = "";
    private static BufferedReader br;
    private static String postURL = "";
    private static File file;
    private static String uploadresponse;
    private static String downloadlink;
    private static String logincookie;
    private static String authcookie;
    private static boolean login = false;
    private static String userid = "", userpwd = "";

    public static void main(String[] args) throws Exception {
        generateUploadedValue();
        initialize();
        fileUpload();
    }

    public static void generateUploadedValue() {
        char[] nonvowel = new char[]{'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'};
        char[] vowel = new char[]{'a', 'e', 'i', 'o', 'u'};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(nonvowel[(int) Math.round(Math.random() * 1000) % 20]).append("").append(vowel[(int) Math.round(Math.random() * 1000) % 5]);
        }
        admincode = sb.toString();
        System.out.println("Admin Code : " + admincode);
    }

    private static void initialize() throws Exception {
        System.out.println("Getting startup cookie from uploaded.to");
        u = new URL("http://uploaded.to/");
        uc = (HttpURLConnection) u.openConnection();
//        uc.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
        Map<String, List<String>> headerFields = uc.getHeaderFields();

        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                if (tmp.contains("PHPSESSID")) {
                    phpsessioncookie = tmp;
                }
            }
        }
        phpsessioncookie = phpsessioncookie.substring(0, phpsessioncookie.indexOf(";"));
        System.out.println("phpsessioncookie: " + phpsessioncookie);

//http://uploaded.to/js/script.js

        loginUploadedDotTo();


        u = new URL("http://uploaded.to/js/script.js");
        uc = (HttpURLConnection) u.openConnection();
        if (login) {
            uc.setRequestProperty("Cookie", phpsessioncookie + ";" + logincookie + ";" + authcookie);
        } else {
            uc.setRequestProperty("Cookie", phpsessioncookie);
        }
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }

//http://stor1053.uploaded.to/upload?admincode=cajuda&id=2612531&pw=1d3826e6fbac3d22f969f34ba45fee363a4cd49c
        if (login) {
            postURL = parseResponse(k, "uploadServer = '", "'") + "upload?admincode=" + admincode + "&id=" + userid + "&pw=" + userpwd;
        } else {
            postURL = parseResponse(k, "uploadServer = '", "'") + "upload?admincode=" + admincode;
        }
//http://stor1079.uploaded.to/upload?admincode=bavika
        System.out.println("postURL : " + postURL);

    }

    private static String getData(String url) throws Exception {
        u = new URL(url);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", phpsessioncookie + ";" + logincookie + ";" + authcookie);
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
        return k;
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
        // httppost.setHeader("Referer", "https://www.dropbox.com/home/Public");
        //httppost.setHeader("Cookie", phpsessioncookie);
        file = new File("h:\\install.txt");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        mpEntity.addPart("Filedata", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into uploaded.to");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();


        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }
//  
        System.out.println("Upload response : " + uploadresponse);
        uploadresponse = uploadresponse.substring(0, uploadresponse.indexOf(","));
        downloadlink = "http://ul.to/" + uploadresponse;
        System.out.println("Download link : " + downloadlink);
    }

    public static void loginUploadedDotTo() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to uploaded.to");
        HttpPost httppost = new HttpPost("http://uploaded.to/io/login");
        httppost.setHeader("Cookie", phpsessioncookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("id", ""));
        formparams.add(new BasicNameValuePair("pw", ""));
        formparams.add(new BasicNameValuePair("_", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            if (escookie.getName().equalsIgnoreCase("login")) {
                logincookie = "login=" + escookie.getValue();
                System.out.println(logincookie);
                login = true;
            }
            if (escookie.getName().equalsIgnoreCase("auth")) {
                authcookie = "auth=" + escookie.getValue();
                System.out.println(authcookie);
            }

        }

        tmp = getData("http://uploaded.to/");
        userid = parseResponse(tmp, "id=\"user_id\" value=\"", "\"");
        userpwd = parseResponse(tmp, "id=\"user_pw\" value=\"", "\"");
    }
}
