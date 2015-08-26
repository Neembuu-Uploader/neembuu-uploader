/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.File;
import java.nio.charset.Charset;
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
 * @author dinesh
 */
public class FileDenUploaderPlugin {

    
    //Disallowed File Types: html, htm, php, php3, phtml, htaccess, htpasswd, cgi, pl, asp, aspx, cfm, exe, ade, adp, bas, bat, chm, cmd, com, cpl, crt, hlp, hta, inf, ins, isp, jse, lnk, mdb, mde, msc, msi, msp, mst, pcd, pif, reg, scr, sct, shs, url, vbe, vbs, wsc, wsf, wsh, shb, js, vb, ws, mdt, mdw, mdz, shb, scf, pl, pm, dll
    
    private static File file;
    private static String uploadresponse;
    private static String downloadlink;
    private static StringBuilder cookies;
    private static boolean login = false;

    public static void main(String[] args) throws Exception {
        loginFileDen();
        fileUpload();
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void fileUpload() throws Exception {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://www.fileden.com/upload_old.php");
        httppost.setHeader("Cookie", cookies.toString());
        file = new File("C:\\Documents and Settings\\dinesh\\Desktop\\ImageShackUploaderPlugin.java");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        mpEntity.addPart("action", new StringBody("upload"));
        mpEntity.addPart("upload_to", new StringBody(""));
        mpEntity.addPart("overwrite_option", new StringBody("overwrite"));
        mpEntity.addPart("thumbnail_size", new StringBody("small"));
        mpEntity.addPart("create_img_tags", new StringBody("1"));
        mpEntity.addPart("file0", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into fileden");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();


        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }


        System.out.println(uploadresponse);
        downloadlink = parseResponse(uploadresponse, "'link':'", "'");
        System.out.println("Download link : " + downloadlink);
        httpclient.getConnectionManager().shutdown();
    }

    public static void loginFileDen() throws Exception {


        cookies = new StringBuilder();
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to fileden.com");
        HttpPost httppost = new HttpPost("http://www.fileden.com/account.php?action=login");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("action", "login"));
        formparams.add(new BasicNameValuePair("task", "login"));
        formparams.add(new BasicNameValuePair("username", "007007dinesh"));
        formparams.add(new BasicNameValuePair("password", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        System.out.println("Getting cookies........");
        //System.out.println(EntityUtils.toString(httpresponse.getEntity()));
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            cookies.append(escookie.getName()).append("=").append(escookie.getValue()).append(";");
        }
        if (cookies.toString().contains("uploader_username")) {
            login = true;
        }
        if (login) {
            System.out.println("FileDen Login success :)");
            System.out.println(cookies);
        } else {
            System.out.println("FileDen Login failed :(");
        }


    }
}
