/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

/**
 *
 * @author Dinesh
 */
public class FileSonicUploaderPlugin {

    private static URL u;
    private static String tmp;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private String rolecookie = "", langcookie = "";
    private String postURL = "";
    private File file;
    private static String filesoniclink;
    private String uploadID;
    private boolean login;
    private String sessioncookie = "", mailcookie = "", namecookie = "", affiliatecookie = "";
    private String msgreadcookie = "", msgcheckcookie = "";
    private String uname = "007007dinesh@gmail.com";
    private String pwd = "";
    private String uploadresponse;

    public static void main(String[] args) throws Exception {
        ////        if (uname.isEmpty() || pwd.isEmpty()) {
        //            System.out.println("Please give valid username,pwd");
        //            return;
        //        }
        u = new URL("http://www.filesonic.com/");
        uc = (HttpURLConnection) u.openConnection();
        uc.setInstanceFollowRedirects(false);
        filesoniclink = uc.getHeaderField("Location");
        System.out.println(filesoniclink);

        new FileSonicUploaderPlugin().fileSonic();
    }

    public void fileSonic() throws Exception {
        initialize();
        loginFileSonic();

        String fileSonicDomain=getData(filesoniclink+"filesystem/browse#fileManager");
        fileSonicDomain=parseResponse(fileSonicDomain, "uploadServerHostname = '", "'");
        System.out.println(fileSonicDomain);
        



        uploadID = "upload_" + new Date().getTime() + "_" + sessioncookie.replace("PHPSESSID", "") + "_" + Math.round(Math.random() * 90000);

        //http://s342.filesonic.in/?callbackUrl=http://www.filesonic.in/upload-completed/:uploadProgressId&X-Progress-ID=upload_1344155322185_=h6u6cbt3oqth4s692fl6fj77e6_27941
        //http://s342.filesonic.in/?callbackUrl=http://www.filesonic.in/upload-completed/:uploadProgressId&X-Progress-ID=upload_1344152373069_801k78evotima0fqmnjgg36vk5_53991

        //http://web.eu.filesonic.in/?callbackUrl=http://www.filesonic.in/upload-completed/:uploadProgressId&X-Progress-ID=upload_1327210940211_i77aakt4e94g19ofl2b4lm9fj4_7689
        postURL = "http://"+fileSonicDomain+ "/?callbackUrl=" + filesoniclink + "upload-completed/:uploadProgressId&X-Progress-ID=" + uploadID;
        System.out.println("post URL : " + postURL);
        fileUpload();

    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {
        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    public String getData(String url) {

        try {
            u = new URL(url);
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", filesoniclink);
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://filesonic.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            uc.setRequestProperty("Cookie", langcookie + ";" + sessioncookie + ";" + mailcookie + ";" + namecookie + ";" + rolecookie + ";" + msgreadcookie + ";" + msgcheckcookie + ";");
            uc.setRequestMethod("GET");
            uc.setInstanceFollowRedirects(false);
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
//                System.out.println(temp);
                k += temp;
            }
//            br.close();
//            u = null;
//            uc = null;
            return k;
        } catch (Exception e) {
            System.out.println("exception : " + e.toString());
            return "";
        }

    }

    private void initialize() throws IOException {
        System.out.println("Getting startup cookies from filesonic...........");


        u = new URL(filesoniclink);
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                String tmp = header.get(i);
                if (tmp.contains("role")) {
                    rolecookie = tmp;
                    rolecookie = rolecookie.substring(0, rolecookie.indexOf(";"));
                }
                if (tmp.contains("lang")) {
                    langcookie = tmp;
                    langcookie = langcookie.substring(0, langcookie.indexOf(";"));
                }
            }
            System.out.println("role cookie : " + rolecookie);
            System.out.println("lang cookie : " + langcookie);
        }

    }

    public void fileUpload() throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        file = new File("/home/vigneshwaran/VIGNESH/dinesh.txt");
        HttpPost httppost = new HttpPost(postURL);
        httppost.setHeader("Cookie", langcookie + ";" + sessioncookie + ";" + mailcookie + ";" + namecookie + ";" + rolecookie + ";" + msgreadcookie + ";" + msgcheckcookie + ";");
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("files[]", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("Now uploading your file into filesonic...........................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
//        if (resEntity != null) {
//
//            String tmp = EntityUtils.toString(resEntity);
////            System.out.println("response : " + tmp);
//
//        }
        uploadresponse = response.getLastHeader("Location").getValue();
        System.out.println("Upload response URL : " + uploadresponse);
        uploadresponse = getData(uploadresponse);
        if (uploadresponse.contains("File was successfully uploaded")) {
            System.out.println("File was successfully uploaded :)");
        } else {
            throw new Exception("There might be a problem with your internet connecivity or server error. Please try after some time. :(");
        }
    }

    public void loginFileSonic() throws IOException {



        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to FileSonic");
        HttpPost httppost = new HttpPost(filesoniclink + "/user/login");
        httppost.setHeader("Referer", "http://www.filesonic.com/");
        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("email", uname));
        formparams.add(new BasicNameValuePair("password", pwd));
        formparams.add(new BasicNameValuePair("redirect", "/"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);

        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            if (escookie.getName().equalsIgnoreCase("PHPSESSID")) {
                sessioncookie = "PHPSESSID=" + escookie.getValue();
                System.out.println(sessioncookie);
            }
            if (escookie.getName().equalsIgnoreCase("email")) {
                mailcookie = "email=" + escookie.getValue();
                login = true;
                System.out.println(mailcookie);
            }
            if (escookie.getName().equalsIgnoreCase("nickname")) {
                namecookie = "nickname=" + escookie.getValue();
                System.out.println(namecookie);
            }
            if (escookie.getName().equalsIgnoreCase("isAffiliate")) {
                affiliatecookie = "isAffiliate=" + escookie.getValue();
                System.out.println(affiliatecookie);
            }
            if (escookie.getName().equalsIgnoreCase("role")) {
                rolecookie = "role=" + escookie.getValue();
                System.out.println(rolecookie);
            }

        }
        if (login) {
            System.out.println("Login Success");
//            getMessagingCookies();
        } else {
            System.out.println("Login failed");
        }

    }
}
