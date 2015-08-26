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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
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
public class ZShareUploaderPlugin {

    private static HttpURLConnection uc = null;
    private static URL u = null;
    private static BufferedReader br;
    private static String zsharelink;
    
    private static File file;
    
    private static String uploadresponse = "";
    
    private static String linkpage = "";
    private static String downloadlink = "";
    private static String deletelink = "";
    
    
    
    private static String tmp = "";
    private static String uname = "007007dinesh";
    private static String pwd = "";
    private static String zsharedomain;
    private static String fnvalue;
    private static String xfsscookie;
    private static boolean login = false;

    public static void main(String[] args) throws IOException, Exception {
        if (uname.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please give valid username,pwd");
            return;
        }


        initialize();
//        loginZshare();

        fileUpload();
        getDownloadLink();
    }

    private static void getDownloadLink() throws Exception {


        // Note : If the response header contains redirection.i.e 302 Found and moved,use the below code to get that moved page 
        System.out.println("Now Getting Download link...");
        HttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(zsharedomain);
        if (login) {
            httppost.setHeader("Cookie", xfsscookie);
        }
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        mpEntity.addPart("fn", new StringBody(fnvalue));
        mpEntity.addPart("op", new StringBody("upload_result"));
        mpEntity.addPart("st", new StringBody("OK"));
        httppost.setEntity(mpEntity);

//        h.setHeader("Referer", postURL);
//        h.setHeader("Cookie", sidcookie + ";" + mysessioncookie);
        HttpResponse res = client.execute(httppost);
        HttpEntity entity = res.getEntity();
        linkpage = EntityUtils.toString(entity);
        System.out.println(linkpage);

        downloadlink = parseResponse(linkpage, "Direct Link:</b></td>", "</td>");
        downloadlink = parseResponse(downloadlink, "value=\"", "\"");
        deletelink = parseResponse(linkpage, "Delete Link:</b></td>", "</td>");
        deletelink = parseResponse(deletelink, "value=\"", "\"");
        System.out.println("Download link : " + downloadlink);
        System.out.println("Delete Link : " + deletelink);
    }

    private static void fileUpload() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost;
        if (login) {
            zsharelink = zsharelink.replaceAll("anon", "reg");
            httppost = new HttpPost(zsharelink);

        } else {
            httppost = new HttpPost(zsharelink);
        }
        httppost.setHeader("Cookie", xfsscookie);

        file = new File("/home/vigneshwaran/VIGNESH/naruto.txt");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        ContentBody cbFile = new FileBody(file);
        //mpEntity.addPart("sess_id", new StringBody(xfsscookie.replaceAll("xfss=", "")));
        mpEntity.addPart("file_0", cbFile);
        mpEntity.addPart("TOS", new StringBody("1"));
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into zshare");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }
        fnvalue = parseResponse(uploadresponse, "name='fn'>", "<");
        System.out.println("fn value : " + fnvalue);
        httpclient.getConnectionManager().shutdown();

    }

    private static void initialize() throws IOException, Exception {
        System.out.println("Getting zshare dynamic upload link");
        u = new URL("http://www.zshare.ma/");
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");





        br = new BufferedReader(new InputStreamReader(u.openStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }

        zsharelink = parseResponse(k, "action=\"", "\"");
        zsharelink = zsharelink.toLowerCase();
        zsharelink = zsharelink + generateZShareUID() + "&js_on=1&utype=anon&upload_type=file";
        zsharedomain = parseResponse(zsharelink, "http://", "cgi-bin");
        zsharedomain = "http://" + zsharedomain;
        System.out.println("ZShare Domain " + zsharedomain);
        System.out.println(zsharelink);

    }

    public static void loginZshare() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to zshare.ma");
        HttpPost httppost = new HttpPost(zsharedomain);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("op", "login"));
        formparams.add(new BasicNameValuePair("login", uname));
        formparams.add(new BasicNameValuePair("password", pwd));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;

        while (it.hasNext()) {
            escookie = it.next();
            System.out.println(escookie.getName() + "=" + escookie.getValue());
            if (escookie.getName().equalsIgnoreCase("xfss")) {
                xfsscookie = "xfss=" + escookie.getValue();
                System.out.println(xfsscookie);
                login = true;
                System.out.println("Zshare Login success :)");
            }

        }
        if (!login) {
            System.out.println("Zshare login failed :(");
        }

    }


    private static String generateZShareUID() {

        String UID = "";


        for (int i = 0; i < 12; i++) {
            UID += (int) (Math.random() * 10);
        }
        System.out.println(UID);
        return UID;
    }

    
    public static String parseResponse(String response, String stringStart, String stringEnd) {

        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    
}
