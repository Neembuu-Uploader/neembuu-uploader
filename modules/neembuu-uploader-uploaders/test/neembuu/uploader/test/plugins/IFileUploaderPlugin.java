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
import java.util.List;
import org.apache.http.HttpEntity;
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
 * @author dinesh
 */
public class IFileUploaderPlugin {

    private static String loginresponse;
    private static String apikey;
    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String postURL;
    private static File file;
    private static String uploadresponse = "";
    private static String file_ukey;
    private static String downloadlink;

    public static void main(String[] args) throws Exception {
        loginIFile();

        u = new URL("http://ifile.it/api-fetch_upload_url.api");
        uc = (HttpURLConnection) u.openConnection();

        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String temp = "", k = "";
        while ((temp = br.readLine()) != null) {
            System.out.println(temp);
            k += temp;
        }

        uc.disconnect();
        postURL = parseResponse(k, "upload_url\":\"", "\"");
        postURL = postURL.replaceAll("\\\\", "");
        System.out.println("Post URL : " + postURL);
        fileUpload();


    }

    public static void loginIFile() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to ifile.it");
        HttpPost httppost = new HttpPost("https://secure.ifile.it/api-fetch_apikey.api");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("username", "007007dinesh"));
        formparams.add(new BasicNameValuePair("password", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        loginresponse = EntityUtils.toString(httpresponse.getEntity());
        System.out.println("response : " + loginresponse);
        if (loginresponse.contains("akey")) {
            apikey = parseResponse(loginresponse, "akey\":\"", "\"");
            System.out.println("IFile Login sccuess :)");
            System.out.println("API key : " + apikey);
        }else{
            System.out.println("IFile login failed :(");
        }


    }

    private static void fileUpload() throws Exception {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postURL);
        file = new File("C:\\Documents and Settings\\dinesh\\Desktop\\FileSonicUploaderPlugin.java");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("akey", new StringBody(apikey));
        mpEntity.addPart("Filedata", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into ifile.it");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();

        httpclient.getConnectionManager().shutdown();
        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
            System.out.println("Upload response : " + uploadresponse);
        } else {
            throw new Exception("There might be a problem with your internet connection or server error. Please try after some time :(");
        }

        if (uploadresponse.contains("\"status\":\"ok\"")) {
            System.out.println("File uploaded successfully :)");
            file_ukey = parseResponse(uploadresponse, "\"ukey\":\"", "\"");
            System.out.println("File ukey : " + file_ukey);
            //http://ifile.it/6phxv2z

            downloadlink = "http://ifile.it/" + file_ukey + "/" + file.getName();
            System.out.println("Download link : " + downloadlink);
        } else {
            throw new Exception("There might be a problem with your internet connection or server error. Please try after some time :(");
        }

//  



    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }
}
