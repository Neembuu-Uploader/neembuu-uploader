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
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
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
public class UGotFileUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static String tmp = "";
    private static String phpsessioncookie;
    private static BufferedReader br;
    private static String postURL;
    private static File file;
    private static String uploadresponse;
    private static String downloadlink;
    private static String deletelink;

    public static void main(String[] args) throws Exception {
        initialize();
        loginUGotFile();
        tmp = getData("http://ugotfile.com/");
        postURL = parseResponse(tmp, "action=\"", "\"");
        System.out.println("Post URL : " + postURL);
        
        fileUpload();
    }

    private static void initialize() throws Exception {
        System.out.println("Getting startup cookie from ugotfile.com");
        u = new URL("http://ugotfile.com/");
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();

        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                if (tmp.contains("PHPSESSID")) {
                    phpsessioncookie = tmp;
                }

            }
        }
        phpsessioncookie = phpsessioncookie.substring(0, phpsessioncookie.indexOf(";"));
        System.out.println("phpsessioncookie : " + phpsessioncookie);

        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }

        postURL = parseResponse(k, "action=\"", "\"");
        System.out.println("Post URL : " + postURL);
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
        file = new File("h:/Sakura haruno.jpg");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        mpEntity.addPart("Filedata", cbFile);
//        mpEntity.addPart("server", new StringBody(server));
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into ugotfile.com");
        HttpResponse response = httpclient.execute(httppost);
        System.out.println(response.getStatusLine());
        if (response != null) {
            uploadresponse = EntityUtils.toString(response.getEntity());
        }
        System.out.println("Upload Response : " + uploadresponse);
        downloadlink = parseResponse(uploadresponse, "[\"", "\"");
        downloadlink = downloadlink.replaceAll("\\\\/", "/");
        deletelink = parseResponse(uploadresponse, "\",\"", "\"");
        deletelink = deletelink.replaceAll("\\\\/", "/");
        System.out.println("Download Link : " + downloadlink);
        System.out.println("Delete Link : " + deletelink);
    }

    public static void loginUGotFile() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to ugotfile.com");
        HttpPost httppost = new HttpPost("http://ugotfile.com/user/login");
        httppost.setHeader("Cookie", phpsessioncookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("ugfLoginUserName", ""));
        formparams.add(new BasicNameValuePair("ugfLoginPassword", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        HttpEntity resEntity = httpresponse.getEntity();
        if (httpresponse.getStatusLine().toString().contains("302")) {
            tmp = httpresponse.getLastHeader("Location").getValue();
            System.out.println("UGotFile Login success");
        } else {
            System.out.println("UGotFile login failed");
        }
    }

    private static String getData(String myurl) throws Exception {
        u = new URL(myurl);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", phpsessioncookie);
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
        return k;
    }
}
