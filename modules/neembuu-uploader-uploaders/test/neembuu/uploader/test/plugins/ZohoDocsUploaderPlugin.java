/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class ZohoDocsUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static String uname = "007007dinesh";
    private static String pwd = "*************************";
    private static File file;
    private static StringBuilder cookies;
    private static boolean login = false;

    public static void main(String[] args) {
        try {
            cookies = new StringBuilder();
            loginZohoDocs();
            if (login) {
                fileUpload();
            }
        } catch (IOException ex) {
            Logger.getLogger(ZohoDocsUploaderPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void loginZohoDocs() throws IOException {



        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to Zoho Docs");
        HttpPost httppost = new HttpPost("https://accounts.zoho.com/login");

        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("LOGIN_ID", uname));
        formparams.add(new BasicNameValuePair("PASSWORD", pwd));
        formparams.add(new BasicNameValuePair("IS_AJAX", "true"));
        formparams.add(new BasicNameValuePair("remember", "-1"));
        formparams.add(new BasicNameValuePair("servicename", "ZohoPC"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);

        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;

        while (it.hasNext()) {
            escookie = it.next();

            cookies.append(escookie.getName()).append("=").append(escookie.getValue()).append(";");
            System.out.println(cookies);
        }
        if (cookies.toString().contains(uname)) {
            login = true;
        }
        if (login) {
            System.out.println("Zoho Docs Login Success");

        } else {
            System.out.println("Zoho Docs Login failed");
        }

    }

    public static void fileUpload() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        file = new File("C:\\Documents and Settings\\dinesh\\Desktop\\GeDotttUploaderPlugin.java");
        HttpPost httppost = new HttpPost("https://docs.zoho.com/uploadsingle.do?isUploadStatus=false&folderId=0&refFileElementId=refFileElement0");
        httppost.setHeader("Cookie", cookies.toString());
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("multiupload_file", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("Now uploading your file into zoho docs...........................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (resEntity != null) {

            String tmp = EntityUtils.toString(resEntity);
//            System.out.println(tmp);
            if(tmp.contains("File Uploaded Successfully"))
                System.out.println("File Uploaded Successfully");

        }
        //    uploadresponse = response.getLastHeader("Location").getValue();
        //  System.out.println("Upload response : " + uploadresponse);
    }
}
