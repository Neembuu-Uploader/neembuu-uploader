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
 * @author Dinesh
 */
public class GrUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static String gruploadlink;
    private static BufferedReader br;
    private static long UploadID;
    private static File file;
    private static String uploadresponse;
    private static String tmpserver;
    private static String refererURL;
    private static String fnvalue;
    private static String downloadlink;
    private static String deletelink;
    private static String logincookie;
    private static String xfsscookie;
    private static boolean login = false;

    public static void main(String[] args) throws Exception {


        loginGrUpload();

        initialize();

        fileUpload();

        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);


        HttpPost httppost = new HttpPost("http://grupload.com/");
        if (login) {
            httppost.setHeader("Cookie", logincookie + ";" + xfsscookie);
        }
//        httppost.setHeader("Referer", "http://www.filesonic.in/");
//        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("op", "upload_result"));
        formparams.add(new BasicNameValuePair("fn", fnvalue));
        formparams.add(new BasicNameValuePair("st", "OK"));


        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);

        uploadresponse = EntityUtils.toString(httpresponse.getEntity());
        downloadlink = parseResponse(uploadresponse, "Download Link", "</textarea>");

        downloadlink = downloadlink.substring(downloadlink.indexOf("http://"));
        System.out.println("Download Link : " + downloadlink);
        deletelink = parseResponse(uploadresponse, "Delete Link", "</textarea>");
        deletelink = deletelink.substring(deletelink.indexOf("http://"));
        System.out.println("Delete Link : " + deletelink);




    }

    private static void initialize() throws IOException {

        u = new URL("http://www.grupload.com");
        uc = (HttpURLConnection) u.openConnection();
        if (login) {
            uc.setRequestProperty("Cookie", logincookie + ";" + xfsscookie);
        }
        System.out.println("Getting dynamic grupload upload link value ........");
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
            //System.out.println(temp);
            k += temp;
        }



        gruploadlink = parseResponse(k, "action=\"", "\"");
//
        tmpserver = gruploadlink.substring(0, gruploadlink.indexOf("/cgi-bin"));
        System.out.println("Temp Server : " + tmpserver);
//        serverID = gruploadlink.substring(gruploadlink.lastIndexOf("/") + 1);
//        System.out.println("Server ID : " + serverID);

        UploadID = (long) (Math.floor(Math.random() * 900000000000L) + 100000000000L);
        gruploadlink += UploadID + "&js_on=1";
        System.out.println(gruploadlink);

        //http://s13.grupload.com/tmp/status.html?162672640171=VipFileUploaderPlugin.java=grupload.com/
        //http://s13.grupload.com/tmp/status.html?318496200230=sky.java=grupload.com/
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    public static String getData(String url) {

        try {
            u = new URL(url);
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.grupload.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://grupload.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (login) {
                uc.setRequestProperty("Cookie", logincookie + ";" + xfsscookie);
            }
            uc.setRequestMethod("GET");
            uc.setInstanceFollowRedirects(false);
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
                //System.out.println(temp);
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

    public static void fileUpload() throws IOException {





        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        file = new File("C:\\Documents and Settings\\dinesh\\Desktop\\GrUploaderPlugin.java");
        HttpPost httppost = new HttpPost(gruploadlink);
        if (login) {

            httppost.setHeader("Cookie", logincookie + ";" + xfsscookie);
        }
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("upload_type", new StringBody("file"));
//        mpEntity.addPart("srv_id", new StringBody(serverID));
        if (login) {
            mpEntity.addPart("sess_id", new StringBody(xfsscookie.substring(5)));
        }
        mpEntity.addPart("srv_tmp_url", new StringBody(tmpserver + "/tmp"));
        mpEntity.addPart("file_0", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("Now uploading your file into grupload...........................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (resEntity != null) {

            String tmp = EntityUtils.toString(resEntity);
            System.out.println("Upload response : " + tmp);

            fnvalue = parseResponse(tmp, "name='fn'>", "<");
            System.out.println("fn value : " + fnvalue);

        }
//        uploadresponse = response.getLastHeader("Location").getValue();
//        System.out.println("Upload response : " + uploadresponse);
    }

    public static void loginGrUpload() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to grupload.com");
        HttpPost httppost = new HttpPost("http://grupload.com/");

//        httppost.setHeader("Referer", "http://www.filesonic.in/");
//        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();

        formparams.add(new BasicNameValuePair("op", "login"));
        formparams.add(new BasicNameValuePair("redirect", "http://grupload.com"));
        formparams.add(new BasicNameValuePair("login", "007007dinesh"));
        formparams.add(new BasicNameValuePair("password", "************************"));



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
            if (escookie.getName().equalsIgnoreCase("login")) {
                logincookie = "login=" + escookie.getValue();
                System.out.println(logincookie);
            }
            if (escookie.getName().equalsIgnoreCase("xfss")) {
                xfsscookie = "xfss=" + escookie.getValue();
                System.out.println(xfsscookie);
                login = true;
            }
        }

        if (login) {
            System.out.println("Grupload Login successful :)");
        } else {
            System.out.println("Grupload Login failed :(");
        }


    }
}
