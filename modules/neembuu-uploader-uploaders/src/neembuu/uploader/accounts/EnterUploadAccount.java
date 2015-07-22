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
public class EnterUploadAccount extends AbstractAccount {

    private URL u;
    private PrintWriter pw;
    private HttpURLConnection uc;
    private BufferedReader br;
    private static String logincookie = "";
    private static String xfsscookie = "";

    public EnterUploadAccount() {
        KEY_USERNAME = "euusername";
        KEY_PASSWORD = "eupassword";
        HOSTNAME = "EnterUpload.com";
    }

    public void disableLogin() {
        loginsuccessful = false;
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();
        logincookie = "";
        xfsscookie = "";
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    public void login() {
        try {
            postData("op=login&redirect=&login=" + getUsername() + "&password=" + getPassword() + "&x=0&y=0", "http://www.enterupload.com/login.html");
        } catch (Exception e) {
            NULogger.getLogger().log(Level.SEVERE, "{0}: Error in EnterUpload Login", getClass().getName());
        }

    }

    public void setHttpHeader(String cookie) throws Exception {


        uc = (HttpURLConnection) u.openConnection();
        uc.setDoOutput(true);
        uc.setRequestProperty("Host", "www.enterupload.com");
        uc.setRequestProperty("Connection", "keep-alive");
        uc.setRequestProperty("Referer", "http://www.enterupload.com/");
        uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
        uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        uc.setRequestProperty("Accept-Encoding", "html");
        uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
        uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            uc.setRequestProperty("Cookie", "");
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
                if (tmp.contains("login")) {
                    logincookie = tmp;
                    logincookie = logincookie.substring(0, logincookie.indexOf(";"));
                }
                if (tmp.contains("xfss")) {
                    xfsscookie = tmp;
                    xfsscookie = xfsscookie.substring(0, xfsscookie.indexOf(";"));
                }
            }
        }
        if (!logincookie.isEmpty() && !xfsscookie.isEmpty()) {
            loginsuccessful = true;
            hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
            username = getUsername();
            password = getPassword();
            NULogger.getLogger().info("EnteUpload Login success :)");
        } else {
            username = "";
            password = "";
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
            NULogger.getLogger().info("EnterUpload Login failed :(");
        }
        NULogger.getLogger().log(Level.INFO, "login cookie : {0}", logincookie);
        NULogger.getLogger().log(Level.INFO, "xfss cookie : {0}", xfsscookie);

    }

    public void postData(String content, String posturl) throws Exception {


        u = new URL(posturl);
        setHttpHeader("");
        writeHttpContent(content);
        u = null;
        uc = null;

    }

    public static String getLogincookie() {
        return logincookie;
    }

    public static String getXfsscookie() {
        return xfsscookie;
    }
}
