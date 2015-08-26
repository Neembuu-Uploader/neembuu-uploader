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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
public class BayFilesUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String tmp;
    private static String postURL;
    private static File file;
    private static String uploadresponse;
    private static String downloadlink;
    private static String deletelink;
    private static String sessioncookie;
    private static boolean login = false;

    public static void main(String[] args) throws Exception {
        loginBayFiles();
        initialize();
        fileUpload();
    }

    private static void initialize() throws Exception {
        

        System.out.println("Getting upload url from bayfiles.com");
        if (login) {
            u = new URL("http://api.bayfiles.com/v1/file/uploadUrl?session=" + sessioncookie);
        } else {
            u = new URL("http://api.bayfiles.com/v1/file/uploadUrl");
        }
        uc = (HttpURLConnection) u.openConnection();
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            System.out.println(tmp);
            k += tmp;
        }

        if (k.contains("session expired")) {
            System.out.println("Bayfiles session expired.So login once again.....");
            loginBayFiles();
            initialize();
        }

        postURL = parseResponse(k, "\"uploadUrl\":\"", "\"");
        postURL = postURL.replaceAll("\\\\", "");
        System.out.println("Post URL : " + postURL);
        if (postURL.contains("s6.")) {
            System.out.println("Got s6 domain. So re-initializing.....");
            initialize();
        }
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
        file = new File("/home/vigneshwaran/Documents/TNEB Online Payment 3.pdf");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("file", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into bayfiles.com");
        HttpResponse response = httpclient.execute(httppost);
//        HttpEntity resEntity = response.getEntity();
        uploadresponse = EntityUtils.toString(response.getEntity());

        System.out.println(response.getStatusLine());
//        if (resEntity != null) {
//            uploadresponse = EntityUtils.toString(resEntity);
//        }
//  
        System.out.println("Upload response : " + uploadresponse);
        downloadlink = parseResponse(uploadresponse, "\"downloadUrl\":\"", "\"");
        downloadlink = downloadlink.replaceAll("\\\\", "");
        deletelink = parseResponse(uploadresponse, "\"deleteUrl\":\"", "\"");
        deletelink = deletelink.replaceAll("\\\\", "");
        System.out.println("Download link : " + downloadlink);
        System.out.println("Delete link : " + deletelink);
    }

    public static void loginBayFiles() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to bayfiles.com");

        HttpPost httppost = new HttpPost("http://api.bayfiles.com/v1/account/login/007007dinesh/passwordhere");

        HttpResponse httpresponse = httpclient.execute(httppost);

        String loginResponse = EntityUtils.toString(httpresponse.getEntity());
        if (loginResponse.contains("session")) {
            login = true;
            sessioncookie = parseResponse(loginResponse, "\"session\":\"", "\"");
            System.out.println("Session : "+sessioncookie);
            System.out.println("Bayfiles.com login succeeded :)");
        } else {
            login = false;
            System.out.println("BayFiles.com Login failed :(");
        }

    }
}
