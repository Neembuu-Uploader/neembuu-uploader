/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author dinesh
 */
public class SugarSyncUploaderPlugin {
    /* SugarSync SugarSyncUploaderPlugin API url */

    private static String AUTH_API_URL = "https://api.sugarsync.com/authorization";
    private static String USER_INFO_API_URL = "https://api.sugarsync.com/user";
    /* The User-Agent HTTP Request header's value  */
    /* The template used for creating the request  */
    private static String AUTH_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<authRequest>"
            + "<username>%s</username>"
            + "<password>%s</password>"
            + "<accessKeyId>%s</accessKeyId>"
            + "<privateAccessKey>%s</privateAccessKey>"
            + "</authRequest>";
    private static String CREATE_FILE_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
            + "<file>"
            + "<displayName>%s</displayName>"
            + "<mediaType>%s</mediaType>"
            + "</file>";
    private static URL u;
    private static HttpURLConnection uc;
    private static PrintWriter pw;
    private static BufferedReader br;
    private static String auth_token = "";
    private static String upload_folder_url;
    private static String SugarSync_File_Upload_URL;
    private static File file;

    public static void setHttpHeader() {

        try {
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.sugarsync.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "https://www.sugarsync.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/xml");
            if (uc.getURL().toString().equals(upload_folder_url)) {
//                System.out.println("setting auth token for post .....");
                uc.setRequestProperty("Authorization", auth_token);
            }
            uc.setRequestMethod("POST");
            uc.setInstanceFollowRedirects(false);


        } catch (Exception e) {
            System.out.println("ex" + e.toString());
        }
    }
    /*
     * This method is used to write the POST data like username and password.
     * This takes the "content" value as a parameter which needs to be posted.
     * 
     */

    public static void writeHttpContent(String content) {

        try {

//            System.out.println(content);
//            System.out.println(uc.getURL());
            pw = new PrintWriter(new OutputStreamWriter(uc.getOutputStream()), true);
            pw.print(content);
            pw.flush();
            pw.close();
            System.out.println("res : " + uc.getResponseCode());
            if (uc.getResponseCode() == 401) {
                System.out.println("hey SugarSync login failed :(");
                return;
            }
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));


            String k = "", tmp = "";
            while ((tmp = br.readLine()) != null) {
                System.out.println(tmp);
                k += tmp;
            }
            if (uc.getHeaderFields().containsKey("Location")) {
                if (uc.getURL().toString().equals(AUTH_API_URL)) {
                    System.out.println("SugarSync login successful :)");
                    auth_token = uc.getHeaderField("Location");
                    System.out.println("Auth token : " + auth_token);
                } else {
                    SugarSync_File_Upload_URL = uc.getHeaderField("Location");
                    SugarSync_File_Upload_URL = SugarSync_File_Upload_URL + "/data";
                    System.out.println("Post URL : " + SugarSync_File_Upload_URL);
                }
            } else {
                if (uc.getURL().toString().equals(AUTH_API_URL)) {
                    System.out.println("SugarSync login failed :(");
                } else {
                    System.out.println("There might be problem interface getting Upload URL from SugarSync. Please try after some time :(");
                }
            }

        } catch (Exception e) {
            System.out.println("ex " + e.toString());
        }



    }

    public static void postData(String content, String posturl) {
        try {

            u = new URL(posturl);
            setHttpHeader();
            writeHttpContent(content);
            u = null;
            uc = null;
        } catch (Exception e) {
            System.out.println("exception " + e.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        AUTH_REQUEST = String.format(AUTH_REQUEST, "007007dinesh@gmail.com", "", "MTc5MjY5ODEzMzI1MDQ0MDQwMjQ", "Mjc5NDgxOWU3ZjRmNGQxODgzMzY4N2QyNGUxN2VkODE");
        System.out.println("Trying to login into SugarSync.........");
        postData(AUTH_REQUEST, AUTH_API_URL);
        System.out.println("Getting user info...........");
        getUserInfo(USER_INFO_API_URL);
        
//      

        file = new File("E:/Projects/NarutoMangaDownloader v0.2.1.jar");
        String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        CREATE_FILE_REQUEST = String.format(CREATE_FILE_REQUEST, file.getName(), ext + " file");
        System.out.println("now creating file request............");
        postData(CREATE_FILE_REQUEST, upload_folder_url);
        fileUpload();
    }

    private static void fileUpload() throws Exception {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        // int Min = 1, Max = 10;
        //Min + (int)(Math.random() * ((Max - Min) + 1))
        //1+(int)(Math.random() * ((10 - 1) + 1))
        //1+(int)(Math.random() * 10)
//        int k = 1 + (int) (Math.random() * 10);a


        HttpPut httpput = new HttpPut(SugarSync_File_Upload_URL);
        httpput.setHeader("Authorization", auth_token);
//        httpput.setHeader("Content-Length", String.valueOf(file.length()));  
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        FileBody bin = new FileBody(file);
        reqEntity.addPart("", bin);

        httpput.setEntity(reqEntity);
        System.out.println("Now uploading your file into sugarsync........ Please wait......................");
        System.out.println("Now executing......." + httpput.getRequestLine());
        HttpResponse response = httpclient.execute(httpput);
        System.out.println(response.getStatusLine());

        if (response.getStatusLine().getStatusCode() == 204) {
            System.out.println("File uploaded successfully :)");
        } else {
            throw new Exception("There might be problem with your internet connection or server error. Please try again some after time :(");
        }

    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {

        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void getUserInfo(String url) throws Exception {
        u = new URL(url);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestMethod("GET");
        uc.setRequestProperty("Authorization", auth_token);
        uc.setRequestProperty("Host", "api.sugarsync.com");
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));

        String k = "", tmp = "";
        while ((tmp = br.readLine()) != null) {
            System.out.println(tmp);
            k += tmp;
        }
        if (url.equals(USER_INFO_API_URL)) {
            upload_folder_url = parseResponse(k, "<magicBriefcase>", "</magicBriefcase>");
            System.out.println("Upload_Folder : " + upload_folder_url);
        }

    }
}
