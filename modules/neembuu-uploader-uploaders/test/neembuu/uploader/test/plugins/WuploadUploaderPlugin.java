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
import java.util.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
public class WuploadUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String rolecookie = "", langcookie = "";
    private static String postURL = "";
    private static File file;
    private static String wuplodDomain = "";
    private static String uploadID;
    private static String downloadlink;
    private static String linkID;
    private static boolean login;
    private static String sessioncookie = "", mailcookie = "", namecookie = "", affiliatecookie = "";
    private static String orderbycookie = "", directioncookie = "";
    private static String uname = "";
    private static String pwd = "";

    public static void main(String[] args) throws IOException {
        if (uname.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please give valid username,pwd");
            return;
        }

        initialize();

        loginWuploader();
        uploadID = "upload_" + new Date().getTime() + "_" + sessioncookie.replace("PHPSESSID", "") + "_" + Math.round(Math.random() * 90000);

        postURL = "http://s" + (new Random().nextInt(3) + 1) + wuplodDomain.replaceAll("http://www", "")
                + "?callbackUrl=" + wuplodDomain + "upload/done/:uploadProgressId&X-Progress-ID=" + uploadID;
        System.out.println("post URL : " + postURL);

        fileUpload();



    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void initialize() throws IOException {
        System.out.println("Getting domain from wupload.com");
        u = new URL("http://www.wupload.com/");
        uc = (HttpURLConnection) u.openConnection();

        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
//            for (int i = 0; i < header.size(); i++) {
//                String tmp = header.get(i);
//            }
            wuplodDomain = header.get(0).substring(header.get(0).indexOf("domain=."));
            wuplodDomain = wuplodDomain.replaceAll("domain=.", "");

        }
        wuplodDomain = "http://www." + wuplodDomain + "/";
        System.out.println(wuplodDomain);
    }

    public static void fileUpload() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        file = new File("/home/vigneshwaran/dinesh.txt");
        HttpPost httppost = new HttpPost(postURL);
        httppost.setHeader("Cookie", langcookie + ";" + sessioncookie + ";" + mailcookie + ";" + namecookie + ";" + rolecookie + ";" + orderbycookie + ";" + directioncookie + ";");
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("files[]", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("Now uploading your file into wupload...........................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (response.getStatusLine().getStatusCode() == 302
                && response.getFirstHeader("Location").getValue().contains("upload/done/")) {

            System.out.println("Upload successful :)");
        } else {
            System.out.println("Upload failed :(");
        }

    }

    public static void getFolderCookies() throws IOException {
        u = new URL(wuplodDomain);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", langcookie + ";" + sessioncookie + ";" + mailcookie + ";" + namecookie + ";" + rolecookie);
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                String tmp = header.get(i);
                if (tmp.contains("fs_orderFoldersBy")) {
                    orderbycookie = tmp;
                    orderbycookie = orderbycookie.substring(0, orderbycookie.indexOf(";"));
                }
                if (tmp.contains("fs_orderFoldersDirection")) {
                    directioncookie = tmp;
                    directioncookie = directioncookie.substring(0, directioncookie.indexOf(";"));
                }
            }
            System.out.println("ordercookie : " + orderbycookie);
            System.out.println("directioncookie : " + directioncookie);
        }

    }

    public static void loginWuploader() throws IOException {



        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to Wupload");
        HttpPost httppost = new HttpPost(wuplodDomain + "account/login");
        httppost.setHeader("Referer", wuplodDomain);
        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("email", uname));
        formparams.add(new BasicNameValuePair("password", pwd));
        formparams.add(new BasicNameValuePair("redirect", "/"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);

        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            if (escookie.getName().equalsIgnoreCase("PHPSESSID")) {
                sessioncookie = "PHPSESSID=" + escookie.getValue();
                System.out.println(sessioncookie);
            }
            if (escookie.getName().equalsIgnoreCase("email")) {
                mailcookie = "email=" + escookie.getValue();
                login = true;
                System.out.println(mailcookie);
            }
            if (escookie.getName().equalsIgnoreCase("nickname")) {
                namecookie = "nickname=" + escookie.getValue();
                System.out.println(namecookie);
            }
            if (escookie.getName().equalsIgnoreCase("isAffiliate")) {
                affiliatecookie = "isAffiliate=" + escookie.getValue();
                System.out.println(affiliatecookie);
            }
            if (escookie.getName().equalsIgnoreCase("role")) {
                rolecookie = "role=" + escookie.getValue();
                System.out.println(rolecookie);
            }

        }
        if (login) {
            System.out.println("Login Success");
            getFolderCookies();
        } else {
            System.out.println("Login failed");
        }

    }
}
