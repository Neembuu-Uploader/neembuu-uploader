/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

/**
 *
 * @author Dinesh
 */
public class UploadBoxAccount extends AbstractAccount {

    private URL u;
    private HttpURLConnection uc;
    private String tmp;
    private static String sidcookie;

    public UploadBoxAccount() {
        KEY_USERNAME = "ubusername";
        KEY_PASSWORD = "ubpassword";
        HOSTNAME = "UploadBox.com";
    }

    public void disableLogin() {
        loginsuccessful = false;
        sidcookie = "";
    }

    public static String getSidcookie() {
        return sidcookie;
    }

    private void initialize() throws Exception {
        NULogger.getLogger().info("Getting startup cookie from uploadbox.com");
        u = new URL("http://uploadbox.com/");
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();

        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                if (tmp.contains("sid")) {
                    sidcookie = tmp;
                }

            }
        }
        sidcookie = sidcookie.substring(0, sidcookie.indexOf(";"));
        NULogger.getLogger().log(Level.INFO, "sidcookie : {0}", sidcookie);
    }

    public void loginUploadBox() throws Exception {
        loginsuccessful = false;
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        NULogger.getLogger().info("Trying to log in to uploadbox.com");
        HttpPost httppost = new HttpPost("http://www.uploadbox.com/en");
        httppost.setHeader("Cookie", sidcookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("login", getUsername()));
        formparams.add(new BasicNameValuePair("passwd", getPassword()));
        formparams.add(new BasicNameValuePair("ac", "auth"));
        formparams.add(new BasicNameValuePair("back", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        if (httpresponse.getStatusLine().toString().contains("302")) {
            loginsuccessful = true;
            NULogger.getLogger().info("UploadBox Login Success");
            username = getUsername();
            password = getPassword();
        } else {
            loginsuccessful = false;
            NULogger.getLogger().info("UploadBox Login failed");
            username = "";
            password = "";
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }

    public void login() {
        try {
            initialize();
            loginUploadBox();
        } catch (Exception ex) {
            Logger.getLogger(UploadBoxAccount.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
