/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
public class ImageShackUploaderPlugin {

    private static String uploadresponse;
    private static URL u;
    private static HttpURLConnection uc;
    private static String tmp;
    private static String langcookie;
    private static String latestcookie;
    private static String uncookie;
    private static String imgshckcookie;
    private static String phpsessioncookie;
    private static String newcookie;
    private static String myidcookie;
    private static String myimagescookie;
    private static String usercookie;
    private static boolean login = false;
    private static BufferedReader br;
    private static String upload_key;

    public static void main(String[] args) throws Exception {
        initialize();
        loginImageShack();
        fileUpload();
    }

    private static void initialize() throws Exception {
        System.out.println("Getting startup cookie from imageshack.us");
        u = new URL("http://imageshack.us/");
        uc = (HttpURLConnection) u.openConnection();
//        uc.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                if (tmp.contains("lang")) {
                    langcookie = tmp;
                }
                if (tmp.contains("latest")) {
                    latestcookie = tmp;
                }
                if (tmp.contains("un_")) {
                    uncookie = tmp;
                }
                if (tmp.contains("imgshck")) {
                    imgshckcookie = tmp;
                }
                if (tmp.contains("PHPSESSID")) {
                    phpsessioncookie = tmp;
                }
                if (tmp.contains("new_")) {
                    newcookie = tmp;
                }
            }
        }

        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
        upload_key = parseResponse(k, "name=\"key\" value=\"", "\"");
        System.out.println("upload_key : " + upload_key);
        langcookie = langcookie.substring(0, langcookie.indexOf(";"));
        latestcookie = latestcookie.substring(0, latestcookie.indexOf(";"));
        uncookie = uncookie.substring(0, uncookie.indexOf(";"));
        imgshckcookie = imgshckcookie.substring(0, imgshckcookie.indexOf(";"));
        phpsessioncookie = phpsessioncookie.substring(0, phpsessioncookie.indexOf(";"));
        newcookie = newcookie.substring(0, newcookie.indexOf(";"));
        System.out.println("langcookie : " + langcookie);
        System.out.println("latestcookie : " + latestcookie);
        System.out.println("uncookie : " + uncookie);
        System.out.println("imgshckcookie : " + imgshckcookie);
        System.out.println("phpsessioncookie : " + phpsessioncookie);
        System.out.println("newcookie : " + newcookie);
    }

    private static void loginImageShack() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to imageshack.us");
        HttpPost httppost = new HttpPost("http://imageshack.us/auth.php");
        httppost.setHeader("Referer", "http://www.uploading.com/");
        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        httppost.setHeader("Cookie", newcookie + ";" + phpsessioncookie + ";" + imgshckcookie + ";" + uncookie + ";" + latestcookie + ";" + langcookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("username", ""));
        formparams.add(new BasicNameValuePair("password", ""));
        formparams.add(new BasicNameValuePair("stay_logged_in", ""));
        formparams.add(new BasicNameValuePair("format", "json"));
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
            if (escookie.getName().equalsIgnoreCase("myid")) {
                myidcookie = escookie.getValue();
                System.out.println(myidcookie);
                login = true;

            }
            if (escookie.getName().equalsIgnoreCase("myimages")) {
                myimagescookie = escookie.getValue();
                System.out.println(myimagescookie);
            }
            if (escookie.getName().equalsIgnoreCase("isUSER")) {
                usercookie = escookie.getValue();
                System.out.println(usercookie);
            }
        }

    }

    private static void fileUpload() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        // int Min = 1, Max = 10;
        //Min + (int)(Math.random() * ((Max - Min) + 1))
        //1+(int)(Math.random() * ((10 - 1) + 1))
        //1+(int)(Math.random() * 10)
//        int k = 1 + (int) (Math.random() * 10);
        File f = new File("e:/386241_2574873925227_1055681973_32612922_2023068366_n.jpg");
        String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);
        HttpPost httppost = null;
        if (ext.equals("jpeg")
                || ext.equals("jpg")
                || ext.equals("bmp")
                || ext.equals("gif")
                || ext.equals("png")
                || ext.equals("tiff")) {
            httppost = new HttpPost("http://www.imageshack.us/upload_api.php");
        }
        if (ext.equals("avi")
                || ext.equals("mkv")
                || ext.equals("mpeg")
                || ext.equals("mp4")
                || ext.equals("mov")
                || ext.equals("3gp")
                || ext.equals("flv")
                || ext.equals("3gpp")) {
            httppost = new HttpPost("http://render.imageshack.us/upload_api.php");
        }

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("Filename", new StringBody(f.getName()));
        reqEntity.addPart("optimage", new StringBody("1"));
        reqEntity.addPart("new_flash_uploader", new StringBody("y"));
        reqEntity.addPart("rembar", new StringBody("0"));
        reqEntity.addPart("myimages", new StringBody("null"));
        reqEntity.addPart("optsize", new StringBody("optimize"));
        reqEntity.addPart("rem_bar", new StringBody("0"));
        reqEntity.addPart("key", new StringBody(upload_key));
        if (login) {
            reqEntity.addPart("isUSER", new StringBody(usercookie));
            reqEntity.addPart("myimages", new StringBody(myimagescookie));
        } else {
            reqEntity.addPart("isUSER", new StringBody("null"));
        }
        reqEntity.addPart("swfupload", new StringBody("1"));
        reqEntity.addPart("ulevel", new StringBody("null"));
        reqEntity.addPart("always_opt", new StringBody("null"));
        FileBody bin = new FileBody(f);
        reqEntity.addPart("Filedata", bin);
        reqEntity.addPart("upload", new StringBody("Submit Query"));
        httppost.setEntity(reqEntity);
        System.out.println("Now uploading your file into imageshack.us Please wait......................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();

        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
            System.out.println("PAGE :" + uploadresponse);
            uploadresponse = parseResponse(uploadresponse, "<image_link>", "<");
            System.out.println("Download Link : " + uploadresponse);
        }
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {

        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }
}
