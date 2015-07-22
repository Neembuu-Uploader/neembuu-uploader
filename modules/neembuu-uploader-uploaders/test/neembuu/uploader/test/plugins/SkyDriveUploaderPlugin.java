/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
public class SkyDriveUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static String tmp;
    private static BufferedReader br;
    private static String loginurl;
    private static String msprcookie = "";
    private static String mspokcookie = "";
    private static PrintWriter pw;
    private static String ppft;
    private static String location;

    public static void main(String[] args) throws Exception {
        initialize();
        loginSkyDrive();
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void initialize() throws Exception {
        System.out.println("Getting startup cookie from login.live.com");
        u = new URL("https://login.live.com/");
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();

        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
//                System.out.println(tmp);
                if (tmp.contains("MSPRequ")) {
                    msprcookie = tmp;
                }
                if (tmp.contains("MSPOK")) {
                    mspokcookie = tmp;
                }

            }
        }
        msprcookie = msprcookie.substring(0, msprcookie.indexOf(";"));
        mspokcookie = mspokcookie.substring(0, mspokcookie.indexOf(";"));
        System.out.println("msprcookie : " + msprcookie);
        System.out.println("mspokcookie : " + mspokcookie);

        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
//        System.out.println(k);
        loginurl = parseResponse(k, "srf_uPost='", "'");
        System.out.println("Login URL : " + loginurl);
        ppft = parseResponse(k, "value=\"", "\"");
        System.out.println(ppft);

//        postURL = parseResponse(k, "action = \"", "\"");
//        generateUploadBoxID();
//        postURL = postURL + uid;
//        System.out.println("Post URL : " + postURL);
//        server = parseResponse(k, "name=\"server\" value=\"", "\"");
//        System.out.println(server);

    }

    public static void loginSkyDrive() throws Exception {


        System.out.println("login ");
        u = new URL(loginurl);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", msprcookie + ";" + mspokcookie);
        uc.setDoOutput(true);
        uc.setRequestMethod("POST");
        uc.setInstanceFollowRedirects(false);
        pw = new PrintWriter(new OutputStreamWriter(uc.getOutputStream()), true);
        pw.print("login=dinesh007007%40hotmail.com&passwd=&SI=Sign+in&type=11&LoginOptions=3&NewUser=1&MEST=&PPSX=Passpor&PPFT=" + ppft + "&PwdPad=&sso=&i1=&i2=1&i3=10524&i4=&i12=1&i13=&i14=437&i15=624&i16=3438");
        pw.flush();
        pw.close();
        System.out.println(uc.getResponseCode());
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                System.out.println(tmp);
            }
        }
        location = uc.getHeaderField("Location");
        System.out.println("Location : " + location);
        System.out.println("going to open paaport page");
        DefaultHttpClient d = new DefaultHttpClient();
        HttpGet hg = new HttpGet("https://skydrive.live.com");
        hg.setHeader("Cookie",msprcookie + ";" + mspokcookie);
        HttpResponse execute = d.execute(hg);
        HttpEntity entity = execute.getEntity();
        System.out.println(EntityUtils.toString(entity));
        System.out.println(execute.getStatusLine());
        Header[] allHeaders = execute.getAllHeaders();
        for (int i = 0; i < allHeaders.length; i++) {
            System.out.println(allHeaders[i].getName() + " : " + allHeaders[i].getValue());
        }
        //        u = new URL(location);
        //        uc = (HttpURLConnection) u.openConnection();
        //        uc.setRequestProperty("Host", "accountservices.passport.net");
        //        uc.setRequestProperty("Connection", "keep-alive");
        //        uc.setRequestProperty("Cache-Control", "max-age=0");
        //        uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.186 Safari/535.1");
        //        uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        //        uc.setRequestProperty("Referer", "https://login.live.com/");
        //        uc.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
        //        uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        //        uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
        //        headerFields = uc.getHeaderFields();
        //        if (headerFields.containsKey("Set-Cookie")) {
        //            List<String> header = headerFields.get("Set-Cookie");
        //            for (int i = 0; i < header.size(); i++) {
        //                tmp = header.get(i);
        //                System.out.println(tmp);
        //            }
        //        }
        //        System.out.println(uc.getResponseCode());
        //        location = uc.getHeaderField("Location");
        //        System.out.println("Location : " + location);
        //        HttpParams params = new BasicHttpParams();
        //        params.setParameter(
        //                "http.useragent",
        //                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        //        DefaultHttpClient httpclient = new DefaultHttpClient(params);
        //
        //        System.out.println("Trying to log in to login.live.com");
        //        HttpPost httppost = new HttpPost(loginurl);
        //        httppost.setHeader("Cookie", msprcookie + ";" + mspokcookie);
        //        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        //        formparams.add(new BasicNameValuePair("login", "dinesh007007@hotmail.com"));
        //        formparams.add(new BasicNameValuePair("passwd", "hanumanitachi"));
        //        formparams.add(new BasicNameValuePair("SI", "Sign in"));
        ////        formparams.add(new BasicNameValuePair("back", ""));
        //        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        //        httppost.setEntity(entity);
        //        HttpResponse httpresponse = httpclient.execute(httppost);
        //        HttpEntity resEntity = httpresponse.getEntity();
        //        System.out.println(httpresponse.getStatusLine());

    }

    private static void getData(String myurl) throws Exception {
        u = new URL(myurl);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Referer", "https://login.live.com/");
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }

    }
}
