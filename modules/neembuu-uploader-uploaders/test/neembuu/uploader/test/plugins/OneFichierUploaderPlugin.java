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
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

/**
 *
 * @author dinesh
 */
public class OneFichierUploaderPlugin {

    static final String UPLOAD_ID_CHARS = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
    private static String uid;
    private static File file;
    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String uploadresponse;
    private static String downloadlink;
    private static String deletelink;
    private static String sidcookie;
    private static boolean login = false;

    public static void main(String[] args) throws Exception {
        loginOneFichier();

        generateOneFichierID();

        fileUpload();

        System.out.println("Getting file links.............");
        u = new URL("http://upload.1fichier.com" + uploadresponse);
        uc = (HttpURLConnection) u.openConnection();
        if (login) {
            uc.setRequestProperty("Cookie", sidcookie);
        }
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
            //System.out.println(temp);
            k += temp;
        }
        downloadlink = parseResponse(k, "<td><a href=\"", "\"");
        deletelink = parseResponse(k, "http://www.1fichier.com/en/remove/", "<");
        deletelink = "http://www.1fichier.com/en/remove/" + deletelink;
        System.out.println("Download link : " + downloadlink);
        System.out.println("Delete link : " + deletelink);

    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    public static void generateOneFichierID() {
        StringBuilder sb = new StringBuilder();
        //sb.append(new Date().getTime() / 1000);
        for (int i = 0; i < 5; i++) {
            int idx = 1 + (int) (Math.random() * 51);
            sb.append(UPLOAD_ID_CHARS.charAt(idx));
        }
        uid = sb.toString();
        System.out.println("uid : " + uid);
    }

    public static void fileUpload() throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        file = new File("C:\\Documents and Settings\\dinesh\\Desktop\\FourSharedUploaderPlugin.java");
//        file = new File("e:\\dinesh.txt");
        HttpPost httppost = new HttpPost("http://upload.1fichier.com/en/upload.cgi?id=" + uid);
        if (login) {
            httppost.setHeader("Cookie", sidcookie);
        }
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
//        mpEntity.addPart("UPLOAD_IDENTIFIER", new StringBody(uploadid));
        mpEntity.addPart("file[]", cbFile);
        mpEntity.addPart("domain", new StringBody("0"));
        httppost.setEntity(mpEntity);
        System.out.println("Now uploading your file into 1fichier...........................");
        System.out.println("Now executing......." + httppost.getRequestLine());
        HttpResponse response = httpclient.execute(httppost);
//        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (response.containsHeader("Location")) {
            uploadresponse = response.getFirstHeader("Location").getValue();
            System.out.println("Upload location link : " + uploadresponse);
        } else {
            throw new Exception("There might be a problem with your internet connection or server error. Please try again");
        }
    }

    public static void loginOneFichier() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to 1fichier.com");
        HttpPost httppost = new HttpPost("http://www.1fichier.com/en/login.pl");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();

        formparams.add(new BasicNameValuePair("mail", "007007dinesh@gmail.com"));
        formparams.add(new BasicNameValuePair("pass", ""));
        formparams.add(new BasicNameValuePair("Login", "Login"));


//        formparams.add(new BasicNameValuePair("remember", "1"));
//        formparams.add(new BasicNameValuePair("username", ""));
//        formparams.add(new BasicNameValuePair("password", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);

        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            if (escookie.getName().equalsIgnoreCase("SID")) {
                sidcookie = "SID=" + escookie.getValue();
                System.out.println(sidcookie);
                login = true;
            }
        }

        if (login) {
            System.out.println("1fichier Login successful :)");
            
        } else {
            System.out.println("1fichier Login failed :(");
        }


    }
}
