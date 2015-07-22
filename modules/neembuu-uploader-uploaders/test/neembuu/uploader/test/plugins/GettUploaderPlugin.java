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

/**
 *
 * @author dinesh
 */
public class GettUploaderPlugin {

    static final String UPLOAD_ID_CHARS = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
    private static String AUTH_API_URL = "https://open.ge.tt/1/users/login";

    /* The User-Agent HTTP Request header's value  */
    /* The template used for creating the request  */
    private static String AUTH_REQUEST_TEMPLATE = "{\"apikey\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}";
    private static String CREATE_FILE_REQUEST = "{"
            + "\"filename\":\""
            + "%s\","
            + "}";
    private static URL u;
    private static HttpURLConnection uc;
    private static PrintWriter pw;
    private static BufferedReader br;
    private static String auth_token = "";
    private static String upload_folder_url;
    private static String SugarSync_File_Upload_URL;
    private static File file;
    private static String access_token;
    private static String uid;

    public static void main(String[] args) throws Exception {

        file = new File("E:\\Projects\\NU\\NeembuuUploader\\test\\neembuuuploader\\test\\plugins\\BoxUploaderPlugin.java");
        String s = String.format(AUTH_REQUEST_TEMPLATE, "tvkxn6yp51wjhsemi78zvx0i0rjinqaor", "username", "password");
        System.out.println(s);
        postData(s, AUTH_API_URL);
//        u = new URL("https://open.ge.tt/1/shares?accesstoken=" + access_token);
//
//        uc = (HttpURLConnection) u.openConnection();
////        System.out.println(uc.getURL());
//        
//        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
//
//        String k = "", tmp = "";
//        while ((tmp = br.readLine()) != null) {
//            System.out.println(tmp);
//            k += tmp;
//        }
        s = "";
        s = String.format(CREATE_FILE_REQUEST, file.getName());
        System.out.println(s);
        generateGettID();
        postData(s, "https://open.ge.tt//1/files/"+uid+"/create?accesstoken="+access_token);
    }

    public static void generateGettID() {
        StringBuilder sb = new StringBuilder();
        //sb.append(new Date().getTime() / 1000);
        for (int i = 0; i < 7; i++) {
            int idx = 1 + (int) (Math.random() * 61);
            sb.append(UPLOAD_ID_CHARS.charAt(idx));
        }
        uid = sb.toString();
        System.out.println("uid : " + uid + " - " + uid.length());
    }

    public static void setHttpHeader() {

        try {
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.ge.tt");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://www.ge.tt/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/xml");
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

            System.out.println(content);
            System.out.println(uc.getURL());
            System.out.println("b4");
            pw = new PrintWriter(new OutputStreamWriter(uc.getOutputStream()), true);
            
            pw.print(content);
            
            pw.flush();
            pw.close();
            System.out.println("aftr");
//            System.out.println("res : " + uc.getResponseCode());
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));

            System.out.println("sssssssss");
            String k = "", tmp = "";
            while ((tmp = br.readLine()) != null) {
                System.out.println(tmp);
                k += tmp;
            }
            if (content.contains("apikey")) {
                access_token = parseResponse(k, "\"accesstoken\":\"", "\"");
                System.out.println("Access Token : " + access_token);
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

    public static String parseResponse(String response, String stringStart, String stringEnd) {

        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }
}
