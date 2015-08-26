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
public class SendSpaceUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static String tmp;
    private static String sidcookie, ssuicookie;
    private static BufferedReader br;
    private static String destinationDir;
    private static String signature, postURL;
    private static File file;
    private static String uploadresponse;
    private static String downloadlink = "", deletelink = "";
    private static String ssalcookie = "";
    private static boolean login = false;
    private static String userID = "";
    private static String progressURL = "";
    private static String uploadID = "";

    public static void main(String[] args) throws Exception {

        initialize();
//        loginSendSpace();
        getDynamicSendSpaceValues();
        fileUpload();

    }

    public static void getDynamicSendSpaceValues() throws Exception {

        String k = "";
        if (login) {
            System.out.println("Getting sendspace page after login success");
            u = new URL("http://www.sendspace.com/");
            uc = (HttpURLConnection) u.openConnection();
            uc.setRequestProperty("Cookie", sidcookie + ";" + ssuicookie + ";" + ssalcookie);
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));

            while ((tmp = br.readLine()) != null) {
                k += tmp;
            }
        } else {
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));

            while ((tmp = br.readLine()) != null) {
                k += tmp;
            }

        }

        System.out.println(k);

//        System.out.println("Getting sendspace dynamic upload link");
//        if (login) {
//            postURL = parseResponse(k, "method=\"post\" action=\"http://", "\"", false);
//        } else {
//            postURL = parseResponse(k, "method=\"post\" action=\"http://", "\"", false);
//        }
//        postURL = "http://" + postURL;
//        System.out.println("Post URL : " + postURL);

        progressURL = parseResponse(k, "name=\"PROGRESS_URL\" value=\"", "\"", false);
        System.out.println("Progress URL : " + progressURL);
        String tmpDomain = progressURL.substring(0, progressURL.indexOf("progress"));
        uploadID = progressURL.substring(progressURL.indexOf("UPLOAD_IDENTIFIER"));
        System.out.println("Upload ID : " + uploadID);
        destinationDir = parseResponse(k, "\"DESTINATION_DIR\"	value=\"", "\"", false);
        System.out.println("destinationdir : " + destinationDir);
        signature = parseResponse(k, "\"signature\" value=\"", "\"", false);
        System.out.println("signature : " + signature);
        //\"userid\" value=\"
        if (login) {
            userID = parseResponse(k, "\"userid\" value=\"", "\"", false);
            System.out.println("User ID : " + userID);
        }

        postURL = tmpDomain + "upload?SPEED_LIMIT=0&MAX_FILE_SIZE=314572800&" + uploadID + "&DESTINATION_DIR=" + destinationDir;
        System.out.println("Post URL " + postURL);

    }

    private static void initialize() throws Exception {
        System.out.println("Getting startup cookie from sendspace.com");
        u = new URL("http://www.sendspace.com/");
        uc = (HttpURLConnection) u.openConnection();
//        uc.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                if (tmp.contains("SID")) {
                    sidcookie = tmp;
                }
                if (tmp.contains("ssui")) {
                    ssuicookie = tmp;
                }
            }
        }
        sidcookie = sidcookie.substring(0, sidcookie.indexOf(";"));
        ssuicookie = ssuicookie.substring(0, ssuicookie.indexOf(";"));
        System.out.println("sidcookie: " + sidcookie);
        System.out.println("ssuicookie: " + ssuicookie);

    }

    public static String parseResponse(String response, String stringStart, String stringEnd, boolean lastindexof) {
        if (!lastindexof) {
            response = response.substring(response.indexOf(stringStart));
        } else {
            response = response.substring(response.lastIndexOf(stringStart));
        }
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void fileUpload() throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postURL);
        httppost.setHeader("Cookie", sidcookie + ";" + ssuicookie);
        file = new File("/home/vigneshwaran/Downloads/DropBoxUploaderPlugin.java");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("PROGRESS_URL", new StringBody(progressURL));
        mpEntity.addPart("DESTINATION_DIR", new StringBody(destinationDir));
        mpEntity.addPart("js_enabled", new StringBody("1"));
        mpEntity.addPart("signature", new StringBody(signature));
        mpEntity.addPart("upload_files", new StringBody(""));
        if (login) {
            mpEntity.addPart("userid", new StringBody(userID));
        }
        mpEntity.addPart("terms", new StringBody("1"));
        mpEntity.addPart("file[]", new StringBody(""));
        mpEntity.addPart("description[]", new StringBody(""));
        mpEntity.addPart("upload_file[]", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into sendspace.com");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }
//        if (resEntity != null) {
//            resEntity.consumeContent();
//        }


//        System.out.println("resp : " + uploadresponse);
        downloadlink = parseResponse(uploadresponse, "Download Link", "target", false);
        deletelink = parseResponse(uploadresponse, "Delete File Link", "target", false);
        downloadlink = downloadlink.replaceAll("\\s+", " ");
        deletelink = deletelink.replaceAll("\\s+", " ");
        downloadlink = parseResponse(downloadlink, "<a href=\"", "\"", false);
        deletelink = parseResponse(deletelink, "href=\"", "\"", false);
        System.out.println("Download link : " + downloadlink);
        System.out.println("Delete link : " + deletelink);
        httpclient.getConnectionManager().shutdown();
    }

    public static void loginSendSpace() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to sendspace");
        HttpPost httppost = new HttpPost("http://www.sendspace.com/login.html");
        httppost.setHeader("Cookie", sidcookie + ";" + ssuicookie);
//        httppost.setHeader("Referer", "http://www.filesonic.in/");
//        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("action", "login"));
        formparams.add(new BasicNameValuePair("submit", "login"));
        formparams.add(new BasicNameValuePair("target", "%252F"));
        formparams.add(new BasicNameValuePair("action_type", "login"));
        formparams.add(new BasicNameValuePair("remember", "1"));
        formparams.add(new BasicNameValuePair("username", ""));
        formparams.add(new BasicNameValuePair("password", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);

        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            if (escookie.getName().equalsIgnoreCase("ssal")) {
                ssalcookie = escookie.getName() + "=" + escookie.getValue();
                System.out.println(ssalcookie);
                login = true;
            }

        }

    }
}
