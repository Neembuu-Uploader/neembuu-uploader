/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.NULogger;

/**
 *
 * @author Dinesh
 */
public class BadongoAccount extends AbstractAccount {

    static final String UPLOAD_ID_CHARS = "1234567890qwertyuiopasdfghjklzxcvbnm";
    private static String uid;
    private HttpURLConnection uc;
    private PrintWriter pw;
    private URL u;
    private static String usercookie = "";
    private static String pwdcookie = "";
    private BufferedReader br;
    

    public BadongoAccount() {
        KEY_USERNAME = "bogusername";
        KEY_PASSWORD = "bogpassword";
        HOSTNAME = "Badongo.com";
    }

    public void disableLogin() {
        loginsuccessful = false;
        usercookie = "";
        pwdcookie = "";
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    public void setHttpHeader(String cookie) throws Exception {


        uc = (HttpURLConnection) u.openConnection();
        uc.setDoOutput(true);
        uc.setRequestProperty("Host", "www.badongo.com");
        uc.setRequestProperty("Connection", "keep-alive");
        uc.setRequestProperty("Referer", "http://www.badongo.com/");
        uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
        uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        uc.setRequestProperty("Accept-Encoding", "html");
        uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        uc.setRequestProperty("Cookie", "badongoL=en; bT=%2F;");
        uc.setRequestMethod("POST");
        uc.setInstanceFollowRedirects(false);



    }
    /*
     * This method is used to write the POST data like username and password.
     * This takes the "content" value as a parameter which needs to be posted.
     *
     */

    public void writeHttpContent(String content) throws Exception {

        loginsuccessful = false;

        //System.out.println(content);
        pw = new PrintWriter(new OutputStreamWriter(uc.getOutputStream()), true);
        pw.print(content);
        pw.flush();
        pw.close();
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
//            String httpResp = br.readLine();


        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                String tmp = header.get(i);
                if (tmp.contains("badongoU")) {
                    usercookie = tmp;
                    usercookie = usercookie.substring(0, usercookie.indexOf(";"));
                }
                if (tmp.contains("badongoP")) {
                    pwdcookie = tmp;
                    pwdcookie = pwdcookie.substring(0, pwdcookie.indexOf(";"));
                }
            }
        }
        if (!usercookie.isEmpty() && !pwdcookie.isEmpty()) {
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            NULogger.getLogger().info("Badongo Login success :)");
        } else {
            NULogger.getLogger().info("Badongo Login failed :(");
            loginsuccessful = false;
            username = "";
            password = "";
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
        NULogger.getLogger().log(Level.INFO, "User cookie : {0}", usercookie);
        NULogger.getLogger().log(Level.INFO, "Pwd cookie : {0}", pwdcookie);


    }

    public void postData(String content, String posturl) throws Exception {
        u = new URL(posturl);
        setHttpHeader("");
        writeHttpContent(content);
        u = null;
        uc = null;
    }

    public void login() {

        try {
            postData("username=" + getUsername() + "&password=" + getPassword() + "&cap_id=&cap_secret=" + uid + "&bk=&do=login&cou=en&no_ssl=1", "http://www.badongo.com/login");
        } catch (Exception e) {
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), e.toString()});
            System.err.println(e);
        }
    }

    public static String getUsercookie() {
        return usercookie;
    }

    public static String getPwdcookie() {
        return pwdcookie;
    }
}
