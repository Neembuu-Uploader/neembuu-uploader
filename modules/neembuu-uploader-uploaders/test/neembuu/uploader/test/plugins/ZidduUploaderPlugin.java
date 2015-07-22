/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

/**
 *
 * @author dinesh
 */
public class ZidduUploaderPlugin {

    private static String phpsessioncookie;
    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;

    public static void main(String[] args) throws Exception {

        loginZiddu();

        u = new URL("http://www.ziddu.com/upload.php");
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Host", "www.ziddu.com");
        uc.setRequestProperty("Connection", "Keep-alive");
        uc.setRequestProperty("Referer", "http://www.ziddu.com/login.php");
//        uc.setRequestProperty("", "");
//        uc.setRequestProperty("", "");
//        uc.setRequestProperty("", "");
//        uc.setRequestProperty("", "");
//        uc.setRequestProperty("", "");
        uc.setRequestProperty("Cookie", phpsessioncookie);
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "", tmp = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
            System.out.println(tmp);
        }

    }

    public static void loginZiddu() throws Exception {



        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to ziddu");
        HttpPost httppost = new HttpPost("http://www.ziddu.com/login.php");
        httppost.setHeader("Referer", "http://www.ziddu.com/");
        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
//        httppost.setHeader("Cookie", cfduidcookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("email", "007007dinesh@gmail.com"));
        formparams.add(new BasicNameValuePair("password", ""));

        formparams.add(new BasicNameValuePair("action", "LOGIN"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);

        System.out.println("Getting cookies........");
        System.out.println(httpresponse.getStatusLine());
        Header[] allHeaders = httpresponse.getAllHeaders();
        for (int i = 0; i < allHeaders.length; i++) {
            System.out.println(allHeaders[i].getName() + " = " + allHeaders[i].getValue());
        }
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
//            System.out.println(escookie.getName() + " = " + escookie.getValue());
            if (escookie.getName().contains("PHPSESSID")) {
                phpsessioncookie = escookie.getName() + " = " + escookie.getValue();

                //  System.out.println("session cookie : " + sessioncookie);
                //}
            }

//        if (httpresponse.getStatusLine().getStatusCode() == 302) {
//            login = true;
//            System.out.println("localhostr Login Success");
//        } else {
//            System.out.println("localhostr Login failed");
//        }

//        System.out.println(EntityUtils.toString(httpresponse.getEntity()));


            InputStream is = httpresponse.getEntity().getContent();
            is.close();

            System.out.println("php session cookie : " + phpsessioncookie);
        }
    }
}