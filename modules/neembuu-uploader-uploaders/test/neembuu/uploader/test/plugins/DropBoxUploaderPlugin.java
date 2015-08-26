/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
public class DropBoxUploaderPlugin {

    private static String UPLOAD_URL = "https://dl-web.dropbox.com/upload_cross_domain?filename=%s&dest=&%s";
    static String username = "dinesh007007@hotmail.com";
    static String password = "";
    private static URL u;
    private static HttpURLConnection uc;
    private static PrintWriter pw;
    private static boolean login = false;
    private static String gvcCookie = "", tCookie = "";
    private static StringBuilder dbCookies = null;

    public static void main(String[] args) throws Exception {
        username = URLEncoder.encode(username, "UTF-8");
        password = URLEncoder.encode(password, "UTF-8");




//        System.exit(0);
//        System.setProperty("java.net.useSystemProxies", "true");
        initialize();
        loginDB();
//        System.out.println(String.format(UPLOAD_URL, filename, tCookie));
//        fileUpload();
        if (login) {
//            dropBoxFileUpload();
            fileUpload_Using_Apache_Http_Client();
        }
    }

    private static void initialize() throws Exception {


        System.out.println("Init....");

        u = new URL("https://www.dropbox.com");

        uc = (HttpURLConnection) u.openConnection();
        String k = "", tmp = "";
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("set-cookie")) {
            List<String> header = headerFields.get("set-cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
//                System.out.println(tmp);
                if (tmp.contains("gvc=")) {
                    gvcCookie = tmp;
                    gvcCookie = tmp.substring(0, tmp.indexOf(";"));
                }
                if (tmp.contains("t=")) {
                    tCookie = tmp;
                    tCookie = tmp.substring(0, tmp.indexOf(";"));
                }
            }
        }

        System.out.println("gvc cookie : " + gvcCookie);
        System.out.println("tcookie : " + tCookie);
        u = null;
        uc = null;
    }

    public static void loginDB() throws IOException {

        System.out.println("now login");
        String LOGIN_CONTENT = tCookie + "&login_email=" + username + "&login_password=" + password + "&cont=https%3A%2F%2Fwww.dropbox.com%2F";
        u = new URL("https://www.dropbox.com/login");
        uc = (HttpURLConnection) u.openConnection();
        uc.setDoOutput(true);
        uc.setRequestProperty("Host", "www.dropbox.com");
        uc.setRequestProperty("Connection", "keep-alive");
        uc.setRequestProperty("Referer", "https://www.dropbox.com/");
        uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:17.0) Gecko/20100101 Firefox/17.0");
        uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        uc.setRequestProperty("Accept-Encoding", "html");
        uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        uc.setRequestProperty("Cookie", gvcCookie + ";" + tCookie);
        uc.setRequestMethod("POST");
        uc.setInstanceFollowRedirects(false);
        pw = new PrintWriter(new OutputStreamWriter(uc.getOutputStream()), true);
        pw.print(LOGIN_CONTENT);
        pw.flush();
        pw.close();

        String k = "", tmp = "";

        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("set-cookie")) {
            List<String> header = headerFields.get("set-cookie");

            dbCookies = new StringBuilder();
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                tmp = tmp.substring(0, tmp.indexOf(";"));
                if (tmp.startsWith("t=")) {
                    tCookie = tmp;
//                    System.out.println("--------->"+tCookie);
                }
                dbCookies.append(tmp).append(";");
            }
            System.out.println("DropBox Login Cookies : " + dbCookies);
        }

        if (dbCookies == null) {
            System.out.println("DropBox Login failed :(");

        } else {
            System.out.println("DropbBox login successful :)");
            login = true;
        }


    }

    private static void dropBoxFileUpload() throws IOException {


        String charset = "UTF-8";
        // String query = String.format("user=%s&password=%s", URLEncoder.encode(username, charset), URLEncoder.encode(password, charset));

        File binaryFile = new File("C:\\Dinesh\\Downloads\\FightMan.exe");
        String encodedFileName = binaryFile.getName();
        encodedFileName = URLEncoder.encode(encodedFileName, charset);
        encodedFileName = encodedFileName.replace("+", "%20");
        System.out.println(encodedFileName);

        UPLOAD_URL = String.format(UPLOAD_URL, encodedFileName, tCookie);
        System.out.println(UPLOAD_URL);
        System.out.println("uploading to " + UPLOAD_URL);

        HttpURLConnection connection = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();

        connection.setDoOutput(true);
        connection.setRequestProperty("Cookie", dbCookies.toString());
        connection.setRequestProperty("Content-Type", "text/html;");
        PrintWriter writer = null;
        try {
            OutputStream output = connection.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(output, charset), true); // true = autoFlush, important!

            InputStream input = null;

            try {
                input = new FileInputStream(binaryFile);
                long filelen = binaryFile.length();
                System.out.println("Length : " + filelen);

                byte[] buffer = new byte[1024];
                for (int length = 0; (length = input.read(buffer)) > 0;) {

                    output.write(buffer, 0, length);

                }
//                System.out.println("Now only terminating the file write loop");
                output.flush(); // Important! Output cannot be closed. Close of writer will close output as well.
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException logOrIgnore) {
                        System.out.println(logOrIgnore);
                    }
                }
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String k = "", tmp = "";
            while ((tmp = br.readLine()) != null) {
                System.out.println(tmp);
                k += tmp;
            }

            if (k.contains("winner")) {
                System.out.println("File Uploaded successfully into DropBox :)");

            } else {
                System.out.println("Upload failed :(");
            }

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {

        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    private static void fileUpload_Using_Apache_Http_Client() throws Exception {


        String charset = "UTF-8";
        // String query = String.format("user=%s&password=%s", URLEncoder.encode(username, charset), URLEncoder.encode(password, charset));

        File binaryFile = new File("/home/vigneshwaran/Downloads/sample.jpg");
        String encodedFileName = binaryFile.getName();
        encodedFileName = URLEncoder.encode(encodedFileName, charset);
        encodedFileName = encodedFileName.replace("+", "%20");
        System.out.println(encodedFileName);

        UPLOAD_URL = String.format(UPLOAD_URL, encodedFileName, tCookie);
        System.out.println(UPLOAD_URL);
//        System.out.println("uploading to " + UPLOAD_URL);


        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(UPLOAD_URL);

        httppost.setHeader("Cookie", dbCookies.toString());
        httppost.setHeader("Content-Type", "text/html");


        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody cbFile = new FileBody(binaryFile);


        mpEntity.addPart("file", cbFile);
        httppost.setEntity(mpEntity);
        System.out.println("executing request " + httppost.getRequestLine());
        System.out.println("Now uploading your file into dropbox.com");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        String uploadresponse = "";
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
            System.out.println(uploadresponse);
            if (uploadresponse.contains("winner")) {
                System.out.println("File Uploaded successfully into DropBox :)");
            }
        } else {
            System.out.println("Upload failed :(");
        }
//  

    }
}