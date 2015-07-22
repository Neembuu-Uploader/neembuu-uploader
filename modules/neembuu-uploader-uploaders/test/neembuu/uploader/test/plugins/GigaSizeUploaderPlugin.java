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
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class GigaSizeUploaderPlugin {

    private static BufferedReader br;
    private static String formtoken;
    private static String uname = "007007dinesh@gmail.com";
    private static String pwd = "dineshvignesh";
    private static boolean login = false;
    private static String uploadid;
    private static String sid;
    private static File file;
    private static String downloadlink;
    static final String UPLOAD_ID_CHARS = "1234567890abcdef";
    private static String uid;

    public static void generateGigasizeID() {
        StringBuilder sb = new StringBuilder();
        //sb.append(new Date().getTime() / 1000);
        for (int i = 0; i < 32; i++) {
            int idx = 1 + (int) (Math.random() * 15);
            sb.append(UPLOAD_ID_CHARS.charAt(idx));
        }
        uid = sb.toString();
        System.out.println("uid : " + uid + " - " + uid.length());
    }

    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {
            try {
                cookies = new StringBuilder();

                initialize();


//            loginGigaSize();
                fileUpload();
                getDownloadLink();
            } catch (IOException ex) {
                Logger.getLogger(GigaSizeUploaderPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    private static URL u;
    private static HttpURLConnection uc;
    private static StringBuilder cookies;

    public static String getData(String url) {

        try {
            u = new URL(url);
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "http://www.gigasize.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://gigasize.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            uc.setRequestProperty("Cookie", cookies.toString());
            uc.setRequestMethod("GET");
            uc.setInstanceFollowRedirects(false);
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
//                System.out.println(temp);
                k += temp;
            }
//            br.close();
//            u = null;
//            uc = null;
            return k;
        } catch (Exception e) {
            System.out.println("exception : " + e.toString());
            return "";
        }

    }

    private static void initialize() throws IOException {
//        System.out.println("Getting startup cookies from gigasize.com");
        u = new URL("http://www.gigasize.com/");
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                String tmp = header.get(i);
                cookies.append(tmp);
            }
//            System.out.println(cookies);
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
//                System.out.println(temp);
                k += temp;
            }
            uploadid = parseResponse(k, "name=\"UPLOAD_IDENTIFIER\" value=\"", "\"");
            sid = parseResponse(k, "name=\"sid\" value=\"", "\"");
            System.out.println("Upload ID : " + uploadid);
//            System.out.println("Sid : " + sid);

        }

//        formtoken = getData("http://www.gigasize.com/formtoken");
//        System.out.println(formtoken);
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    public static void loginGigaSize() throws IOException {



        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to Giga Size");
        HttpPost httppost = new HttpPost("http://www.gigasize.com/signin");

        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("func", ""));
        formparams.add(new BasicNameValuePair("token", formtoken));
        formparams.add(new BasicNameValuePair("signRem", "1"));
        formparams.add(new BasicNameValuePair("email", uname));
        formparams.add(new BasicNameValuePair("password", pwd));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);

        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;

        cookies.setLength(0);
        while (it.hasNext()) {
            escookie = it.next();

            cookies.append(escookie.getName()).append("=").append(escookie.getValue()).append(";");

        }
        System.out.println(cookies);

        if (cookies.toString().contains("MIIS_GIGASIZE_AUTH")) {
            login = true;
        }

        if (login) {
            System.out.println("GigaSize Login Success");

        } else {
            System.out.println("GigaSize Login failed");
        }


    }

    public static void fileUpload() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        file = new File("/media/backup/Projects/NU/NeembuuUploader/test/neembuuuploader/test/plugins/RapidShareUploaderPlugin.java");
//        file = new File("e:\\dinesh.txt");
        HttpPost httppost = new HttpPost("http://www.gigasize.com/uploadie");
        httppost.setHeader("Cookie", cookies.toString());
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("UPLOAD_IDENTIFIER", new StringBody(uploadid));
        mpEntity.addPart("sid", new StringBody(sid));
        mpEntity.addPart("fileUpload1", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("Now uploading your file into Gigasize...........................");
        System.out.println("Now executing......." + httppost.getRequestLine());
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            sid = "";
            sid = EntityUtils.toString(resEntity);
            System.out.println("After upload sid value : " + sid);

        }
        //    uploadresponse = response.getLastHeader("Location").getValue();
        //  System.out.println("Upload response : " + uploadresponse);
    }

    private static void getDownloadLink() {
//  http://www.gigasize.com/status.php?sid=ef97826c95fbcdf2fc5b0970935f961d&rnd=39660591213032603
        String uploadCompleteID;
        long randID;
        do {
            randID = (long) Math.floor(Math.random() * 90000000000000000L) + 10000000000L;
            uploadCompleteID = getData("http://www.gigasize.com/status.php?sid=" + sid + "&rnd=" + randID);
        } while (!uploadCompleteID.contains("done"));
        uploadCompleteID = parseResponse(uploadCompleteID, "\\/uploadcompleteie\\/", "\"");
        System.out.println("Upload Complete ID : " + uploadCompleteID);
        //http://www.gigasize.com/uploadcomplete/MzIwMDIxMzc1NHwwNTNiMDkwNDdkMWNlMTZjNGJhMTJiMTRhNmVjZjM0MQ==
        downloadlink = getData("http://www.gigasize.com/uploadcompleteie/" + uploadCompleteID);
        downloadlink = parseResponse(downloadlink, "Download URL:</span> <a href=\"", "\"");
        System.out.println("Download URL : " + downloadlink);
    }
}