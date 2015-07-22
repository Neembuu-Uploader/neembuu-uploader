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
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class BoxUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String ticket;
    private static String zcookie, visitorcookie;
    private static String request_token;
    private static String loginresponse = "";
    private static String auth_token;
    private static String downloadlink;

    public static void main(String[] args) throws Exception {


        //https://www.box.net/api/1.0/rest?action=get_ticket&api_key=vkf3k5dh0tg1ibvcikjcp8sx0f89d14u
        u = new URL("https://www.box.net/api/1.0/rest?action=get_ticket&api_key=vkf3k5dh0tg1ibvcikjcp8sx0f89d14u");
        uc = (HttpURLConnection) u.openConnection();
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "", tmp;
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }

        ticket = parseResponse(k, "<ticket>", "</ticket>");
        System.out.println("Token : " + ticket);
        uc.disconnect();



        u = new URL("https://www.box.net/api/1.0/auth/" + ticket);
        uc = (HttpURLConnection) u.openConnection();
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        tmp = "";
        k = "";
        while ((tmp = br.readLine()) != null) {
//            System.out.println(k);
            k += tmp;
        }
        request_token = parseResponse(k, "request_token = '", "'");

        System.out.println("Request token  : " + request_token);
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                String t = header.get(i);
                if (t.contains("z=")) {
                    zcookie = t;
                    zcookie = zcookie.substring(0, zcookie.indexOf(";"));
                }
                if (t.contains("box_visitor_id=")) {
                    visitorcookie = t;
                    visitorcookie = visitorcookie.substring(0, visitorcookie.indexOf(";"));
                }
            }
            System.out.println("zcookie : " + zcookie);
            System.out.println("visitorcookie : " + visitorcookie);
        }

        uc.disconnect();



        loginBox();


        getUserInfo();
        uc.disconnect();

        fileUpload();
        //https://www.box.net/api/1.0/rest?action=get_auth_token&api_key=vkf3k5dh0tg1ibvcikjcp8sx0f89d14u&ticket=udd863k39gn9mioc6ym2c6erbqm8qfsh

    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {

        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    public static void loginBox() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);
//https://www.box.net/api/1.0/auth/jtear5a2djtfxs9598apynmea62vko5d
        System.out.println("Trying to log in to box.com");
        HttpPost httppost = new HttpPost("https://www.box.net/api/1.0/auth/" + ticket);
        httppost.setHeader("Cookie", zcookie + ";" + visitorcookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
//        formparams.add(new BasicNameValuePair("action", "login"));
        formparams.add(new BasicNameValuePair("login", "din_ram2007@yahoo.co.in"));
        formparams.add(new BasicNameValuePair("password", ""));
        formparams.add(new BasicNameValuePair("__login", "1"));
        formparams.add(new BasicNameValuePair("dologin", "1"));
        formparams.add(new BasicNameValuePair("reg_step", ""));
        formparams.add(new BasicNameValuePair("submit1", "1"));
        formparams.add(new BasicNameValuePair("folder", ""));
        formparams.add(new BasicNameValuePair("skip_framework_login", "1"));
        formparams.add(new BasicNameValuePair("login_or_register_mode", "login"));
        formparams.add(new BasicNameValuePair("new_login_or_register_mode", ""));
        formparams.add(new BasicNameValuePair("request_token", request_token));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        System.out.println("Gonna print the response");
        loginresponse = EntityUtils.toString(httpresponse.getEntity());
        if (loginresponse.contains("Invalid username or password")) {
            System.out.println("DropBox login failed");
        } else {
            System.out.println("DropbBox login successful :)");
        }

    }

    private static void getUserInfo() throws Exception {
        //https://www.box.net/api/1.0/rest?action=get_auth_token&api_key=vkf3k5dh0tg1ibvcikjcp8sx0f89d14u&ticket=

        //https://www.box.net/api/1.0/rest?action=get_auth_token&api_key=vkf3k5dh0tg1ibvcikjcp8sx0f89d14u&ticket=xybt9orxzo1xrr5vk4r0axne804y1tpk
        u = new URL("https://www.box.net/api/1.0/rest?action=get_auth_token&api_key=vkf3k5dh0tg1ibvcikjcp8sx0f89d14u&ticket=" + ticket);
        uc = (HttpURLConnection) u.openConnection();
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "", tmp;
        while ((tmp = br.readLine()) != null) {
            k += tmp;
            System.out.println(k);
        }

        auth_token = parseResponse(k, "<auth_token>", "</auth_token>");
        System.out.println("Auth_token : " + auth_token);
        uc.disconnect();

    }

    public static void fileUpload() throws Exception {
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);


        //https://upload.box.net/api/1.0/upload/<auth token>/<folder id>
//nid1qr129kzte5gx1x47sypfj4exuo8d
        HttpPost post = new HttpPost("https://upload.box.net/api/1.0/upload/nid1qr129kzte5gx1x47sypfj4exuo8d/0");
//        HttpPost post = new HttpPost("https://upload.box.net/api/1.0/upload/" + auth_token + "/0");
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

// For File parameters
        entity.addPart("file", new FileBody(new File("C:\\Documents and Settings\\dinesh\\Desktop\\GrUploaderPlugin.java")));

        entity.addPart("share", new StringBody("1"));
// For usual String parameters
        post.setEntity(entity);

        System.out.println("Executing ......" + post.getRequestLine());

// Here we go!
        System.out.println("Now uploading your files into box.com");
        String response = EntityUtils.toString(client.execute(post).getEntity(), "UTF-8");
        System.out.println("res : " + response);
        client.getConnectionManager().shutdown();
        if (response.contains("upload_ok") && !response.contains("error")) {

            downloadlink = parseResponse(response, "public_name=\"", "\"");
            downloadlink = "http://www.box.com/s/" + downloadlink;
            System.out.println("Download Link : " + downloadlink);
        } else {
            throw new Exception("There might be a problem with your internet connection or server error. Please try again later :(");
        }

    }
}
