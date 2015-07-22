/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.*;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class LocalhostrUploaderPlugin {

//    private static String cfduidcookie;
//    private static URL u;
//    private static HttpURLConnection uc;
//    private static boolean login = false;
//    private static String cookies = "";
//    private static BufferedReader br;
//    private static File file;
//    private static DefaultHttpClient httpclient;
    private static String downloadlink;
    private static String sessioncookie;
    private static boolean login;

    public static void main(String[] args) throws IOException {

//        httpclient = new DefaultHttpClient();
//        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        loginLocalhostr();
        System.exit(0);
//        initialize();

        /*
         * Accept	text/html,application/xhtml+xml,application/xml;q=0.9,*;q=0.8
         * Accept-Encoding	gzip, deflate Accept-Language	en-us,en;q=0.5
         * Access-Control-Request-Me...	POST Cache-Control	no-cache Connection
         * keep-alive Host	api.localhostr.com Origin	http://localhostr.com
         * Pragma	no-cache User-Agent	Mozilla/5.0 (X11; Ubuntu; Linux x86_64;
         * rv:13.0) Gecko/20100101 Firefox/13.0.
         */
//        HttpOptions httpoptions = new HttpOptions("http://api.localhostr.com/file");
//        httpoptions.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//        httpoptions.setHeader("Accept-Encoding", "gzip, deflate");
//        httpoptions.setHeader("Accept-Language", "en-us,en;q=0.5");
//        httpoptions.setHeader("Access-Control-Request-Method", "POST");
//        httpoptions.setHeader("Cache-Control", "no-cache");
//        httpoptions.setHeader("Connection", "keep-alive");
//        httpoptions.setHeader("Host", "api.localhostr.com");
//        httpoptions.setHeader("Origin", "http://localhostr.com");
//        httpoptions.setHeader("Pragma", "no-cache");
//        httpoptions.setHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64;rv:13.0) Gecko/20100101 Firefox/13.0.");
//
//        HttpResponse myresponse = httpclient.execute(httpoptions);
//        HttpEntity myresEntity = myresponse.getEntity();
//        System.out.println("HTTP OPTIONS response : " + EntityUtils.toString(myresEntity));
//        Header[] allHeaders = myresponse.getAllHeaders();
//        for (int i = 0; i < allHeaders.length; i++) {
//            System.out.println(allHeaders[i].getName() + " : " + allHeaders[i].getValue());
//        }

//        System.exit(0);
        fileUpload();
//        loginEsnips();

//        System.out.println("Getting upload cookies from esnips.com");
//       // http://www.esnips.com/upload.php?method=html_single
//
//        getData(loginsuccesspage);


//        HttpGet hg=new HttpGet("http://www.esnips.com/upload.php?method=html_single");


    }

//    public static String getData(String url) {
//
//        try {
//            u = new URL(url);
//            uc = (HttpURLConnection) u.openConnection();
//            uc.setDoOutput(true);
//            uc.setRequestProperty("Host", "www.esnips.com");
//            uc.setRequestProperty("Connection", "keep-alive");
//            uc.setRequestProperty("Referer", "http://www.esnips.com/upload.php");
//            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
//            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//            uc.setRequestProperty("Accept-Encoding", "html");
//            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
//            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
//            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            uc.setRequestProperty("Cookie", cookies + cfduidcookie + ";");
//            uc.setRequestMethod("GET");
//            uc.setInstanceFollowRedirects(false);
//            System.out.println(uc.getResponseCode());
//            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
//            String temp = "", k = "";
//            while ((temp = br.readLine()) != null) {
//                System.out.println(temp);
//                k += temp;
//            }
//
//            return k;
//        } catch (Exception e) {
//            System.out.println("exception : " + e.toString());
//            return "";
//        }
//
//    }
//    private static void initialize() throws IOException {
//        System.out.println("Getting startup cookies from localhostr.com");
//        HttpGet httpget = new HttpGet("http://localhostr.com/");
//        if (login) {
//            httpget.setHeader("Cookie", sessioncookie);
//        }
//        HttpResponse myresponse = httpclient.execute(httpget);
//        HttpEntity myresEntity = myresponse.getEntity();
//        localhostrurl = EntityUtils.toString(myresEntity);
//        localhostrurl = parseResponse(localhostrurl, "url : '", "'");
//        System.out.println("Localhost url : " + localhostrurl);
//        InputStream is = myresponse.getEntity().getContent();
//        is.close();
//    }
//    public static String parseResponse(String response, String stringStart, String stringEnd) {
//        response = response.substring(response.indexOf(stringStart));
//        response = response.replace(stringStart, "");
//        response = response.substring(0, response.indexOf(stringEnd));
//        return response;
//    }
//  
    public static void loginLocalhostr() throws IOException {

        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to localhostr");
        HttpPost httppost = new HttpPost("http://localhostr.com/login");
        httppost.setHeader("Referer", "http://www.localhostr.com/");
        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("email", "007007dinesh@gmail.com"));
        formparams.add(new BasicNameValuePair("password", ""));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);





        if (httpresponse.getLastHeader("Location").toString().contains("login")) {
            System.out.println("localhostr Login failed :(");
        } else {
            login = true;
            System.out.println("localhostr Login Success :) ");
        }

    }

    public static void fileUpload() throws IOException {

        File file = new File("/home/vigneshwaran/VIGNESH/nu-test5.txt");
        DefaultHttpClient httpclient = new DefaultHttpClient();

        UsernamePasswordCredentials upc = new UsernamePasswordCredentials("007007dinesh@gmail.com", "");


        HttpPost httppost = new HttpPost("http://api.localhostr.com/file");
        try {
            httppost.addHeader(new BasicScheme().authenticate(upc, httppost));
        } catch (AuthenticationException ex) {
            Logger.getLogger(LocalhostrUploaderPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("name", new StringBody(file.getName()));
        mpEntity.addPart("file", cbFile);


        httppost.setEntity(mpEntity);
        System.out.println("Now uploading your file into localhost...........................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (resEntity != null) {

            String tmp = EntityUtils.toString(resEntity);
            System.out.println(tmp);
            downloadlink = tmp.substring(tmp.indexOf("http:"));
            downloadlink = downloadlink.substring(0, downloadlink.indexOf("\""));
            System.out.println("download link : " + downloadlink);

        }
//           uploadresponse = response.getLastHeader("Location").getValue();
//        System.out.println("Upload response : " + uploadresponse);
    }
}
