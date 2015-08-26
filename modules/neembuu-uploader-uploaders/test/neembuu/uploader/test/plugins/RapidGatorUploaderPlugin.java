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
import java.nio.charset.Charset;
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
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author vigneshwaran
 */
public class RapidGatorUploaderPlugin {

    static final String UPLOAD_ID_CHARS = "1234567890qwertyuiopasdfghjklzxcvbnm";
    private static String uid;
    private static URL u;
    private static BufferedReader br;
    private static HttpURLConnection uc;
    private static String uploadURL;
    private static File file;
    private static String uploadresponse;
    private static String downloadlink;
    private static String deletelink;
    private static StringBuilder loginCookie = null;

    public static void main(String[] args) throws Exception {
        //form_url = "
        loginRapidGator();
//        System.out.println(URLDecoder.decode("http://pr6.rapidgator.net/?r=upload/jsonprogress&data%5B0%5D%5Buuid%5D=737e9d4880986c00040dbd9706a6dd38&data%5B0%5D%5Bstart_time%5D=1347291324", "UTF-8"));
//        System.exit(0);
        generateRapidGatorID();
        initialize();

        fileUpload();
    }

    public static void generateRapidGatorID() {
        StringBuilder sb = new StringBuilder();
        //sb.append(new Date().getTime() / 1000);
        for (int i = 0; i < 32; i++) {
            int idx = 1 + (int) (Math.random() * 35);
            sb.append(UPLOAD_ID_CHARS.charAt(idx));
        }
        uid = sb.toString();
//        System.out.println("uid : " + uid + " - " + uid.length());
    }

    private static void initialize() throws Exception {


        System.out.println("Getting upload url from rapidgator");
        u = new URL("http://rapidgator.net/");

        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", loginCookie.toString());
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "", tmp = "";
        while ((tmp = br.readLine()) != null) {
//            System.out.println(tmp);
            k += tmp;
        }


        uploadURL = parseResponse(k, "form_url = \"", "\"");
        uploadURL += uid + "&folder_id=0";
        System.out.println("Upload URL :" + uploadURL);

        System.out.println();
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void fileUpload() throws Exception {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(uploadURL);
        httppost.setHeader("Cookie", loginCookie.toString());
        file = new File("/media/backup/Projects/NU/NeembuuUploader/test/neembuuuploader/test/plugins/RapidShareUploaderPlugin.java");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("file", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into rapidgator.com");
        String uploadTime = String.valueOf(new Date().getTime());
        HttpResponse response = httpclient.execute(httppost);

        System.out.println(response.getStatusLine());
        System.out.println(EntityUtils.toString(response.getEntity()));

        u = null;
        uc = null;
        br = null;
        u = new URL(uploadURL.substring(0, uploadURL.indexOf("/?")) + "/?r=upload/jsonprogress&data%5B0%5D%5Buuid%5D=" + uid + "&data%5B0%5D%5Bstart_time%5D=" + uploadTime);
        uc = (HttpURLConnection) u.openConnection();
//        System.out.println("Checking url.." + uc.getURL());
        uc.setRequestProperty("Cookie", loginCookie.toString());
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "", tmp = "";
        while ((tmp = br.readLine()) != null) {
            System.out.println(tmp);
            k += tmp;
        }


        downloadlink = parseResponse(k, "\"download_url\":\"", "\"");
        downloadlink = downloadlink.replaceAll("\\\\", "");
        deletelink = parseResponse(k, "\"remove_url\":\"", "\"");
        deletelink = deletelink.replaceAll("\\\\", "");
        System.out.println("Download link : " + downloadlink);
        System.out.println("Delete link : " + deletelink);

        //http://pr6.rapidgator.net/?r=upload/jsonprogress&data%5B0%5D%5Buuid%5D=737e9d4880986c00040dbd9706a6dd38&data%5B0%5D%5Bstart_time%5D=1347291324


//  
//        System.out.println("Upload response : " + uploadresponse);
//        downloadlink = parseResponse(uploadresponse, "\"downloadUrl\":\"", "\"");
//        downloadlink = downloadlink.replaceAll("\\\\", "");
//        deletelink = parseResponse(uploadresponse, "\"deleteUrl\":\"", "\"");
//        deletelink = deletelink.replaceAll("\\\\", "");
//        System.out.println("Download link : " + downloadlink);
//        System.out.println("Delete link : " + deletelink);
    }

    public static void loginRapidGator() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        loginCookie = new StringBuilder();

        System.out.println("Trying to log in to rapidgator.net");

        HttpPost httppost = new HttpPost("http://rapidgator.net/auth/login");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
//        formparams.add(new BasicNameValuePair("t", tvalue));
        formparams.add(new BasicNameValuePair("LoginForm[email]:", "007007dinesh@gmail.com"));
        formparams.add(new BasicNameValuePair("LoginForm[password]:", ""));


//        formparams.add(new BasicNameValuePair("remember", "1"));
//        formparams.add(new BasicNameValuePair("username", ""));
//        formparams.add(new BasicNameValuePair("password", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);


//        String loginResponse = EntityUtils.toString(httpresponse.getEntity());


        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            loginCookie.append(escookie.getName()).append("=").append(escookie.getValue()).append(";");
            System.out.println(escookie.getName() + " : " + escookie.getValue());

        }

        if (loginCookie.toString().contains("user")) {
            System.out.println("RapidGator Login succeeded :)");
        } else {
            System.out.println("RapidGator login failed :(");
        }
    }
}
