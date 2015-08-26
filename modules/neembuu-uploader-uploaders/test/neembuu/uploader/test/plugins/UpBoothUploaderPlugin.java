/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import static neembuu.uploader.test.plugins.BayFilesUploaderPlugin.parseResponse;
import org.apache.http.Header;
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
 * @author dsivaji
 */
public class UpBoothUploaderPlugin {

    private static File file;
    private static String fileHostingCookie;

    public static void main(String[] args) {
        try {
            loginUpBooth();
            fileUpload();

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static void loginUpBooth() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to upbooth.com");

        HttpPost httppost = new HttpPost("http://upbooth.com/login.php?aff=1db2f3b654350bf4");


        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
//        formparams.add(new BasicNameValuePair("action", "login"));
        formparams.add(new BasicNameValuePair("loginUsername", "dinesh"));
        formparams.add(new BasicNameValuePair("loginPassword", ""));
        formparams.add(new BasicNameValuePair("submit", "Sign in"));
        formparams.add(new BasicNameValuePair("submitme", "1"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
//        String loginResponse = EntityUtils.toString(httpresponse.getEntity());
        if (httpresponse.getFirstHeader("Location") != null) {
            System.out.println("Login success");
        } else {
            System.out.println("Login failed");
        }

        Header[] allHeaders = httpresponse.getAllHeaders();
        fileHostingCookie = "";
        for (int i = 0; i < allHeaders.length; i++) {
            if (allHeaders[i].getValue().contains("filehosting")) {
                fileHostingCookie = allHeaders[i].getValue();
            }
        }
        System.out.println("Cookie : " + fileHostingCookie);
        //        if (loginResponse.contains("session")) {
        //            login = true;
        //            sessioncookie = parseResponse(loginResponse, "\"session\":\"", "\"");
        //            System.out.println("Session : " + sessioncookie);
        //            System.out.println("Bayfiles.com login succeeded :)");
        //        } else {
        //            login = false;
        //            System.out.println("BayFiles.com Login failed :(");
        //        }

    }

    private static void fileUpload() throws Exception {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://upbooth.com/uploadHandler.php?r=upbooth.com&p=http?aff=1db2f3b654350bf4");
        httppost.addHeader("Cookie", fileHostingCookie);
        file = new File("c:/Dinesh/Naruto_Face.jpg");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        ContentBody cbFile = new FileBody(file);
        String cookie = fileHostingCookie.substring(fileHostingCookie.indexOf("=") + 1);
        cookie = cookie.substring(0, cookie.indexOf(";"));
        System.out.println(cookie);

        mpEntity.addPart("=_sessionid", new StringBody(cookie));
        mpEntity.addPart("files[]", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into upbooth.com");
        HttpResponse response = httpclient.execute(httppost);
        //        HttpEntity resEntity = response.getEntity();
        String uploadresponse = EntityUtils.toString(response.getEntity());

        System.out.println(response.getStatusLine());
//        if (resEntity != null) {
//            uploadresponse = EntityUtils.toString(resEntity);
//        }
//  
        System.out.println("Upload response : " + uploadresponse);
        String downloadlink = parseResponse(uploadresponse, "\"url\":\"", "\"");
        downloadlink = downloadlink.replaceAll("\\\\", "");
        String deletelink = parseResponse(uploadresponse, "\"delete_url\":\"", "\"");
        deletelink = deletelink.replaceAll("\\\\", "");
        System.out.println("Download link : " + downloadlink);
        System.out.println("Delete link : " + deletelink);
    }
}
