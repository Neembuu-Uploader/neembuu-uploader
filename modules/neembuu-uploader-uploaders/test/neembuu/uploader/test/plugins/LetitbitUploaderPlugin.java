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
public class LetitbitUploaderPlugin {

//    static final String UPLOAD_ID_CHARS = "1234567890qwertyuiopasdfghjklzxcvbnm";
    private static String uid;
    private static URL u;
    private static HttpURLConnection uc;
    private static String tmp;
    private static String phpsessioncookie, debugcookie = "", downloadlink = "", deletelink = "";
    private static BufferedReader br;
    private static String server, postURL = "";
    private static File file;
    private static String base;
    private static String uploadresponse;
    private static String uploadpage;
    private static String logcookie;
    private static String pascookie;
    private static String hostcookie;
    private static String pin="";

    public static void main(String[] args) throws Exception {
        initialize();
        loginLetitbit();
        getData();
        fileUpload();
        uploadresponse = "http://letitbit.net/acupl_proxy.php?srv=" + server + "&uid=" + uid;
        tmp = getData(uploadresponse);
        // System.out.println("upload response : "+uploadresponse);
        tmp = parseResponse(tmp, "\"post_result\": \"", "\"");
        System.out.println("upload page : " + tmp);
        uploadpage = getData(tmp);
        //System.out.println(uploadpage);
        downloadlink = parseResponse(uploadpage, "Links to download files:", "</textarea>");
        downloadlink = downloadlink.substring(downloadlink.lastIndexOf(">") + 1);
        deletelink = parseResponse(uploadpage, "Links to delete files:", "</div>");
        deletelink = deletelink.replace("<br/>", "");
        deletelink = deletelink.substring(deletelink.lastIndexOf(">") + 1);
        System.out.println("Download Link : " + downloadlink);
        System.out.println("Delete Link : " + deletelink);
    }

    private static void getData() throws Exception{
        u = new URL("http://www.letitbit.net/");
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", phpsessioncookie+";"+debugcookie+";"+logcookie+";"+pascookie+";"+hostcookie);
         br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
        server = parseResponse(k, "ACUPL_UPLOAD_SERVER = '", "'");
        base = parseResponse(k, "\"base\" type=\"hidden\" value=\"", "\"");
        pin=parseResponse(k, "\"pin\" type=\"hidden\" value=\"", "\"");
        System.out.println("pin : "+pin);
        System.out.println("base : " + base);
        generateLetitbitID();
        System.out.println("server : " + server);
        postURL = "http://" + server + "/marker=" + uid;
        System.out.println("Post URL :" + postURL);
    }
    
    private static void generateLetitbitID() {

        String rand = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        sb.append(Long.toHexString(System.currentTimeMillis()).toUpperCase());
        sb.append("_");
        for (int i = 0; i < 40; i++) {
            // int Min = 1, Max = 60;
            //Min + (int)(Math.random() * ((Max - Min) + 1))
            //1+(int)(Math.random() * ((60 - 1) + 1))
            //1+(int)(Math.random() * 60)
//            int k=(int) Math.round(1+(int)(Math.random() * 60));
            sb.append(rand.charAt((int) Math.round(1 + (int) (Math.random() * 60))));
        }
        uid = sb.toString();
    }

    private static void initialize() throws Exception {
        System.out.println("Getting startup cookie from letitbit.net");
        u = new URL("http://www.letitbit.net/");
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
                if (tmp.contains("debug_panel")) {
                    debugcookie = tmp;
                }


            }
        }
        phpsessioncookie = phpsessioncookie.substring(0, phpsessioncookie.indexOf(";"));
        debugcookie = debugcookie.substring(0, debugcookie.indexOf(";"));
        System.out.println("phpsessioncookie: " + phpsessioncookie);
        System.out.println("debugcookie : " + debugcookie);

        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }

        server = parseResponse(k, "ACUPL_UPLOAD_SERVER = '", "'");
        base = parseResponse(k, "\"base\" type=\"hidden\" value=\"", "\"");
        System.out.println("base : " + base);
        generateLetitbitID();
        System.out.println("server : " + server);
        postURL = "http://" + server + "/marker=" + uid;
        System.out.println("Post URL :" + postURL);
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
        file = new File("h:\\Fantastic face.jpg");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("MAX_FILE_SIZE", new StringBody("2147483647"));
        mpEntity.addPart("owner", new StringBody(""));
        mpEntity.addPart("pin", new StringBody(pin));
        mpEntity.addPart("base", new StringBody(base));
        mpEntity.addPart("host", new StringBody("letitbit.net"));
//        mpEntity.addPart("filename", new StringBody(file.getName()));
        mpEntity.addPart("file0", cbFile);
//        if (login) {
//            mpEntity.addPart("memail", new StringBody(uploadmailid));
//        }
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into letitbit.net");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();


        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }
//  
        System.out.println("Upload response : " + uploadresponse);
    }

    private static String getData(String geturl) throws Exception {

        String k = "";
        u = new URL(geturl);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", debugcookie + ";" + phpsessioncookie);
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
        return k;
    }

    public static void loginLetitbit() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to letitbit.com");
        HttpPost httppost = new HttpPost("http://letitbit.net/");
        httppost.setHeader("Cookie", phpsessioncookie);
//        httppost.setHeader("Referer", "http://www.filesonic.in/");
//        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("act", "login"));
        formparams.add(new BasicNameValuePair("login", ""));
        formparams.add(new BasicNameValuePair("password", ""));


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
            if (escookie.getName().equalsIgnoreCase("log")) {
                logcookie = "log=" + escookie.getValue();
                System.out.println(logcookie);
            }
            if (escookie.getName().equalsIgnoreCase("pas")) {
                pascookie = "pas=" + escookie.getValue();

                System.out.println(pascookie);
            }
            if (escookie.getName().equalsIgnoreCase("host")) {
                hostcookie = "host=" + escookie.getValue();
                System.out.println(hostcookie);
            }
           


        }


    }
}
