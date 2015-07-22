/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
public class UploadingdotcomUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static String tmp, sidcookie, timecookie, cachecookie, ucookie;
    private static BufferedReader br;
    private static String uploadinglink;
    private static String uploadresponse, postURL;
    private static File file;
    private static String sid;
    private static String downloadlink;
    private static PrintWriter pw;
    private static String uname = "007007dinesh@gmail.com";
    private static String pwd = "";
    private static String afterloginpage;
    private static boolean login = false;
    private static String fileID;

    public static void main(String[] args) throws Exception {

        if (uname.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please give valid username | password");
            return;
        }
        file = new File("/media/backup/Projects/NU/NeembuuUploader/test/neembuuuploader/test/plugins $ /media/backup/Projects/NU/NeembuuUploader/test/neembuuuploader/test/plugins/RapidShareUploaderPlugin.java");
//        initialize();
        loginUploadingdotcom();
//        getPreDownloadLink();
//        fileUpload();


    }

    private static void loginUploadingdotcom() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to uploading.com");
        HttpPost httppost = new HttpPost("http://uploading.com/general/login_form/?ajax");
        httppost.setHeader("Referer", "http://www.uploading.com/");
        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
//        httppost.setHeader("Cookie", sidcookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("email", uname));
        formparams.add(new BasicNameValuePair("password", pwd));
        formparams.add(new BasicNameValuePair("back_url", "http://uploading.com"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        HttpEntity en = httpresponse.getEntity();
        uploadresponse = EntityUtils.toString(en);
        System.out.println("Upload response : " + uploadresponse);
        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            System.out.println(escookie.getName()+" : "+escookie.getValue());
//            if (escookie.getName().equalsIgnoreCase("u")) {
//                ucookie = escookie.getName() + "=" + escookie.getValue();
//                System.out.println(ucookie);
//                login = true;
//
//            }
//            if (escookie.getName().equalsIgnoreCase("cache")) {
//                cachecookie = escookie.getName() + "=" + escookie.getValue();
//                System.out.println(cachecookie);
//            }
//            if (escookie.getName().equalsIgnoreCase("time")) {
//                timecookie = escookie.getName() + "=" + escookie.getValue();
//                System.out.println(timecookie);
//            }
        }
//
//        if (login) {
//            System.out.println("Uploading.com Login successful. :)");
//            afterloginpage = getData();
////        System.out.println("after : " + afterloginpage);
//            uploadinglink = parseResponse(afterloginpage, "upload_url\":\"", "\"");
//            uploadinglink = uploadinglink.replaceAll("\\\\", "");
//            System.out.println("New Upload link : " + uploadinglink);
//            postURL = uploadinglink;
//            sid = parseResponse(afterloginpage, "SID: '", "'");
//            System.out.println("New sid from site : " + sid);
//        } else {
//            login = false;
//            System.out.println("Uploading.com Login failed");
//        }
    }

    private static String getData() throws Exception {
        u = new URL("http://www.uploading.com");
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", sidcookie + ";" + ucookie + ";" + cachecookie + ";" + timecookie);
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
        return k;
    }

    private static void initialize() throws Exception {
        System.out.println("Getting startup cookie from uploading.com");
        u = new URL("http://uploading.com/");
        uc = (HttpURLConnection) u.openConnection();
//        uc.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                System.out.println(tmp);
                sidcookie=tmp;
            }
        }
//        sidcookie = sidcookie.substring(0, sidcookie.indexOf(";"));
//        cachecookie = cachecookie.substring(0, cachecookie.indexOf(";"));
//        timecookie = timecookie.substring(0, timecookie.indexOf(";"));
         br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
//        System.out.println("Getting uploading.com dynamic upload link");
//        uploadinglink = parseResponse(k, "upload_url\":\"", "\"");
//        uploadinglink = uploadinglink.replaceAll("\\\\", "");
//        System.out.println("Upload link : " + uploadinglink);
//        postURL = uploadinglink;
//        sid = parseResponse(k, "SID: '", "'");
//        System.out.println("sid from site : " + sid);
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {

        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void fileUpload() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postURL);
//        httppost.setHeader("Accept", "text/*");
//        httppost.setHeader("User-Agent", "Shockwave Flash");
//        httppost.setHeader("Connection", "Keep-Alive");
//        httppost.setHeader("Pragma", "no-cache");
        httppost.setHeader("Cookie", sidcookie + ";" + timecookie + ";" + cachecookie + ";" + ucookie);

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

        reqEntity.addPart("Filename", new StringBody(file.getName()));
        reqEntity.addPart("folder_id", new StringBody("0"));
        reqEntity.addPart("SID", new StringBody(sid));
        reqEntity.addPart("file", new StringBody(fileID));
        FileBody bin = new FileBody(file);
        reqEntity.addPart("file", bin);
        reqEntity.addPart("upload", new StringBody("Submit Query"));
        httppost.setEntity(reqEntity);
        System.out.println("Now uploading your file into uploading.com. Please wait......................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();

        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
            System.out.println("PAGE :" + uploadresponse);
            uploadresponse = parseResponse(uploadresponse, "answer\":\"", "\"");
        }
    }

    private static void getPreDownloadLink() throws IOException {

        //http://uploading.com/files/generate/?ajax

        DefaultHttpClient d = new DefaultHttpClient();
        HttpPost h = new HttpPost("http://uploading.com/files/generate/?ajax");
        h.setHeader("Cookie", sidcookie + ";" + ucookie + ";" + timecookie + ";" + cachecookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("name", file.getName()));
        formparams.add(new BasicNameValuePair("size", String.valueOf(file.length())));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        h.setEntity(entity);
        HttpResponse r = d.execute(h);
        HttpEntity e = r.getEntity();
        downloadlink = EntityUtils.toString(e);
        fileID = parseResponse(downloadlink, "file_id\":\"", "\"");
        downloadlink = parseResponse(downloadlink, "\"link\":\"", "\"");
        System.out.println("File ID : " + fileID);
        System.out.println("Download link : " + downloadlink);
    }
}
