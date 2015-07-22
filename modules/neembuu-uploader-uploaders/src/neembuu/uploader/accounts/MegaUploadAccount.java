/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.NULogger;

/**
 *
 * @author dinesh
 */
public class MegaUploadAccount extends AbstractAccount {

    private URL u;
    private HttpURLConnection uc;
    private BufferedReader br;
    private long filelength;
    private String postURL = "";
    private String uploadID;
    private String downloadlink;
    private PrintWriter pw;
    private static String usercookie = "";
    private String megauploadlink;

    public MegaUploadAccount() {
        KEY_USERNAME = "muusername";
        KEY_PASSWORD = "mupassword";
        HOSTNAME = "MegaUpload.com";
    }

    public void login() {
        try {
            postData("login=1&redir=1&username=" + getUsername() + "&password=" + getPassword(), "http://megaupload.com/?c=login");
        } catch (IOException ex) {
            Logger.getLogger(MegaUploadAccount.class.getName()).log(Level.SEVERE, null, ex);

            NULogger.getLogger().log(Level.INFO, "Exception : {0}", ex);
        }
    }

    public void setHttpHeader(String cookie) throws IOException {

        uc = (HttpURLConnection) u.openConnection();
        uc.setDoOutput(true);
        uc.setRequestProperty("Host", "www.megaupload.com");
        uc.setRequestProperty("Connection", "keep-alive");
        uc.setRequestProperty("Referer", "http://www.megaupload.com/");
        uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
        uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        uc.setRequestProperty("Accept-Encoding", "html");
        uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        uc.setRequestMethod("POST");
        uc.setInstanceFollowRedirects(false);



    }
    /*
     * This method is used to write the POST data like username and password.
     * This takes the "content" value as a parameter which needs to be posted.
     *
     */

    public void writeHttpContent(String content) throws IOException {

        loginsuccessful = false;
        NULogger.getLogger().info(content);
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
                if (tmp.contains("user")) {
                    usercookie = tmp;
                    usercookie = usercookie.substring(0, usercookie.indexOf(";"));
                    loginsuccessful = true;
                    username = getUsername();
                    password = getPassword();
                } else {
                    loginsuccessful = false;
                    username = "";
                    password = "";
                    showWarningMessage( Translation.T().loginerror(), HOSTNAME);
                    accountUIShow().setVisible(true);
                    return;
                }
            }
            NULogger.getLogger().log(Level.INFO, "MegaUpload login successful.. user cookie : {0}", usercookie);
        } else {
            NULogger.getLogger().info("MegaUpload Invalid username or password");
            loginsuccessful = false;
            username = "";
            password = "";
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
            return;
        }



    }

    public void postData(String content, String posturl) throws IOException {
        u = new URL(posturl);
        setHttpHeader(usercookie);
        writeHttpContent(content);
        u = null;

    }

    public static String getUserCookie() {
        return usercookie;
    }

    public void disableLogin() {
        loginsuccessful = false;
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }
}
