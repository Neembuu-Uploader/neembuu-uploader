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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
public class FileFactoryUploadPlugin {

    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static URL u;

    private static String membershipcookie;
    private static String downloadlink;
    private static String uname = "";
    private static String pwd = "";

    public static void main(String[] args) throws Exception {
        if (uname.isEmpty() || pwd.isEmpty()) {
            System.out.println("Please give valid username,pwd");
            return;
        }

    
        loginFileFactory();
        
        fileUpload();
//       String s= getData("http://www.mediafire.com");
        //System.out.println(s);
    }

    
    /*
     * A basic method for reading a particular web page. It will return the read
     * data as a String.
     */

    public static String getData(String url) {


        try {
            u = new URL(url);
            uc = (HttpURLConnection) u.openConnection();
            uc.setRequestProperty("Cookie", membershipcookie);
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
//                System.out.println(temp);             
                k += temp;
            }
            br.close();
            u = null;
            uc = null;
            return k;
        } catch (Exception e) {
            System.out.println("exception : " + e.toString());
            return "";
        }

    }
    private static boolean loginsuccessful;


    public static void loginFileFactory() throws Exception {
        loginsuccessful = false;
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to filefactory.com");
        HttpPost httppost = new HttpPost("http://www.filefactory.com/member/login.php");
//        httppost.setHeader("Cookie", filefactorycookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("redirect", "/"));
        formparams.add(new BasicNameValuePair("email", uname));
        formparams.add(new BasicNameValuePair("password", pwd));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            System.out.println(escookie.getName() + " : " + escookie.getValue());
            if (escookie.getName().equalsIgnoreCase("ff_membership")) {
                membershipcookie = escookie.getValue();
                System.out.println(membershipcookie);
                loginsuccessful=true;
                System.out.println("FileFactory Login successful :)");
                break;
            }
        }
        if (!loginsuccessful) {
            System.out.println("FileFactory login failed :(");
            throw new Exception();
        }

//        System.out.println(EntityUtils.toString(httpresponse.getEntity()));

    }
    
    public static void fileUpload() throws Exception {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://upload.filefactory.com/upload.php");
//        httppost.setHeader("Cookie", cookie + ";" + membershipcookie);
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        //reqEntity.addPart("string_field",new StringBody("field value"));
        File f = new File("/home/vigneshwaran/Documents/TNEB Online Payment 3.pdf");
        FileBody bin = new FileBody(f);
        reqEntity.addPart("Filedata", bin);
        System.out.println(URLDecoder.decode(membershipcookie, "UTF-8")); 
        
        reqEntity.addPart("cookie", new StringBody(URLDecoder.decode(membershipcookie, "UTF-8")));
        reqEntity.addPart("Filename", new StringBody(f.getName())); 
        reqEntity.addPart("folderViewhash", new StringBody("0")); 
        
//        reqEntity.addPart("upload", new StringBody("Submit Query"));
        httppost.setEntity(reqEntity);
        System.out.println("Now uploading your file into filefactory.com. Please wait......................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        String page = "";
        if (resEntity != null) {
            page = EntityUtils.toString(resEntity);
            System.out.println("PAGE :" + page);
        }
        //http://www.filefactory.com/file/complete.php/6nseodiow47d/
        downloadlink = getData("http://www.filefactory.com/file/complete.php/" + page);
        
        downloadlink = downloadlink.substring(downloadlink.indexOf("<div class=\"innerText\">"));
        downloadlink = downloadlink.replace("<div class=\"innerText\">", "");
        downloadlink = downloadlink.substring(0, downloadlink.indexOf("</p>"));
        downloadlink = downloadlink.substring(downloadlink.indexOf("<p>"));
        downloadlink=downloadlink.replaceAll("<p>","");
        System.out.println("Download Link : " + downloadlink);
    }
}
