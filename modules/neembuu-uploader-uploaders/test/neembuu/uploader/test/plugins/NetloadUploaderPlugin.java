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
import org.apache.http.Header;
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

/**
 *
 * @author dinesh
 */
public class NetloadUploaderPlugin {
    
    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String postURL;
    private static File file;
    private static String uploadresponse;
    private static String phpsessioncookie;
    private static String downloadlink;
    private static String deletelink;
    private static String usercookie;
    private static boolean login;
    private static String upload_hash;
    
    public static void main(String[] args) throws Exception {
        initialize();
        loginNetload();
        fileUpload();
        
    }
    
    private static void initialize() throws IOException {
        
        
        if (login) {
            u = new URL("http://netload.in/index.php");
        } else {
            u = new URL("http://netload.in/");
        }
        uc = (HttpURLConnection) u.openConnection();
        if (login) {
            uc.setRequestProperty("Cookie", phpsessioncookie + ";" + usercookie);
            System.out.println("After login success, getting netload page again......");
        } else {
            System.out.println("gettig netload post url.......");
        }
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
//            System.out.println(temp);
            k += temp;
        }
        if (!login) {
            Map<String, List<String>> headerFields = uc.getHeaderFields();
            if (headerFields.containsKey("Set-Cookie")) {
                List<String> header = headerFields.get("Set-Cookie");
                for (int i = 0; i < header.size(); i++) {
                    String tmp = header.get(i);
                    if (tmp.contains("PHPSESSID")) {
                        phpsessioncookie = tmp;
                        phpsessioncookie = phpsessioncookie.substring(0, phpsessioncookie.indexOf(";"));
                    }
                    
                }
            }
            System.out.println("PHP session cookie : " + phpsessioncookie);
        }
        
        
        postURL = parseResponse(k, "action=\"http://", "\"");
        
        postURL = "http://" + postURL;
        
        System.out.println("postURL : " + postURL);
        
        if (login) {
            upload_hash = parseResponse(k, "\"upload_hash\" value=\"", "\"");
            System.out.println("Upload hash : " + upload_hash);
        }
        
    }
    
    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }
    
    private static String getData(String myurl) throws Exception {
        URL url = new URL(myurl);
        uc = (HttpURLConnection) url.openConnection();
        uc.setRequestProperty("Cookie", phpsessioncookie);
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
//                System.out.println(temp);
            k += temp;
        }
        br.close();
        return k;
    }
    
    private static void fileUpload() throws Exception {
        
        
        file = new File("C:\\Documents and Settings\\dinesh\\Desktop\\NU.txt");
        
        HttpClient httpclient = new DefaultHttpClient();
        
        HttpPost httppost = new HttpPost(postURL);
        if (login) {
            httppost.setHeader("Cookie", usercookie);
        }
        
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        if (login) {
            mpEntity.addPart("upload_hash", new StringBody(upload_hash));
        } 
        mpEntity.addPart("file", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into netload");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        
        
        System.out.println(response.getStatusLine());
        Header firstHeader = response.getFirstHeader("Location");
        System.out.println(firstHeader.getValue());
        uploadresponse = getData(firstHeader.getValue());
        System.out.println("Upload response : " + uploadresponse);
        downloadlink = parseResponse(uploadresponse, "The download link is: <br/>", "\" target=\"_blank\">");
        downloadlink = downloadlink.substring(downloadlink.indexOf("href=\""));
        downloadlink = downloadlink.replace("href=\"", "");
        System.out.println("download link : " + downloadlink);
        deletelink = parseResponse(uploadresponse, "The deletion link is: <br/>", "\" target=\"_blank\">");
        deletelink = deletelink.substring(deletelink.indexOf("href=\""));
        deletelink = deletelink.replace("href=\"", "");
        System.out.println("delete link : " + deletelink);
        httpclient.getConnectionManager().shutdown();
        
    }
    
    public static void loginNetload() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);
        
        System.out.println("Trying to log in to netload.in");
        HttpPost httppost = new HttpPost("http://netload.in/index.php");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("txtuser", "663167"));
        formparams.add(new BasicNameValuePair("txtpass", ""));
        formparams.add(new BasicNameValuePair("txtcheck", "login"));
        formparams.add(new BasicNameValuePair("txtlogin", "Login"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            if (escookie.getName().equalsIgnoreCase("cookie_user")) {
                usercookie = "cookie_user=" + escookie.getValue();
                System.out.println(usercookie);
                login = true;
                System.out.println("Netload Login success :)");
                initialize();
            }
        }
        if (!login) {
            System.out.println("Netload Login failed :(");
        }
    }
}
