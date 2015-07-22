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

/**
 *
 * @author Dinesh
 */
public class UploadBoxUploaderPlugin {

    private static String uid;
    private static URL u;
    private static HttpURLConnection uc;
    private static String tmp;
    private static String sidcookie;
    private static BufferedReader br;
    private static String postURL, server;
    private static File file;
    private static String uploadresponse;
    private static String downloadlink;
    private static String deletelink;

    public static void main(String[] args) throws Exception {
        initialize();
        loginUploadBox();
        tmp = getData("http://uploadbox.com/");
        postURL = parseResponse(tmp, "action = \"", "\"");
        generateUploadBoxID();
        postURL = postURL + uid;
        System.out.println("Post URL : " + postURL);
        server = parseResponse(tmp, "name=\"server\" value=\"", "\"");
        System.out.println(server);
        fileUpload();
    }

    private static void generateUploadBoxID() throws Exception {
        String rand = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(rand.charAt((int) Math.round(1 + (int) (Math.random() * 60))));
        }
        uid = sb.toString();
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void initialize() throws Exception {
        System.out.println("Getting startup cookie from uploadbox.com");
        u = new URL("http://uploadbox.com/");
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();

        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                if (tmp.contains("sid")) {
                    sidcookie = tmp;
                }

            }
        }
        sidcookie = sidcookie.substring(0, sidcookie.indexOf(";"));
        System.out.println("sidcookie : " + sidcookie);

        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }

        postURL = parseResponse(k, "action = \"", "\"");
        generateUploadBoxID();
        postURL = postURL + uid;
        System.out.println("Post URL : " + postURL);
        server = parseResponse(k, "name=\"server\" value=\"", "\"");
        System.out.println(server);

    }

    private static void fileUpload() throws Exception {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postURL);
        httppost.setHeader("Cookie", sidcookie);
        file = new File("h:/install.txt");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("filepc", cbFile);
        mpEntity.addPart("server", new StringBody(server));
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into uploadbox.com");
        HttpResponse response = httpclient.execute(httppost);
        System.out.println(response.getStatusLine());
        uploadresponse = response.getLastHeader("Location").getValue();
        uploadresponse = getData(uploadresponse);
        downloadlink = parseResponse(uploadresponse, "name=\"loadlink\" id=\"loadlink\" class=\"text\" onclick=\"this.select();\" value=\"", "\"");
        deletelink = parseResponse(uploadresponse, "name=\"deletelink\" id=\"deletelink\" class=\"text\" onclick=\"this.select();\" value=\"", "\"");
        System.out.println("Download link " + downloadlink);
        System.out.println("deletelink : " + deletelink);
    }

    private static String getData(String myurl) throws Exception {
        u = new URL(myurl);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", sidcookie);
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
        return k;
    }

    public static void loginUploadBox() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to uploadbox.com");
        HttpPost httppost = new HttpPost("http://www.uploadbox.com/en");
        httppost.setHeader("Cookie", sidcookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("login", ""));
        formparams.add(new BasicNameValuePair("passwd", ""));
        formparams.add(new BasicNameValuePair("ac", "auth"));
        formparams.add(new BasicNameValuePair("back", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        HttpEntity resEntity = httpresponse.getEntity();
        System.out.println(httpresponse.getStatusLine());

    }
}
