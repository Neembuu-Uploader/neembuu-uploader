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
public class UGotFileAccount extends AbstractAccount {

    private URL u;
    private HttpURLConnection uc;
    private String tmp;
    private static String phpsessioncookie;

    public UGotFileAccount() {
        KEY_USERNAME = "ugfusername";
        KEY_PASSWORD = "ugfpassword";
        HOSTNAME = "UGotFile.com";
    }

    public void disableLogin() {
        loginsuccessful = false;
        phpsessioncookie = "";
    }

    private void initialize() throws Exception {
        NULogger.getLogger().info("Getting startup cookie from ugotfile.com");
        u = new URL("http://ugotfile.com/");
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();

        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                if (tmp.contains("PHPSESSID")) {
                    phpsessioncookie = tmp;
                }

            }
        }
        phpsessioncookie = phpsessioncookie.substring(0, phpsessioncookie.indexOf(";"));
        NULogger.getLogger().log(Level.INFO, "phpsessioncookie : {0}", phpsessioncookie);
    }

    public static String getPhpsessioncookie() {
        return phpsessioncookie;
    }

    public void loginUGotFile() throws Exception {

        loginsuccessful = false;
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        NULogger.getLogger().info("Trying to log in to ugotfile.com");
        HttpPost httppost = new HttpPost("http://ugotfile.com/user/login");
        httppost.setHeader("Cookie", phpsessioncookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("ugfLoginUserName", getUsername()));
        formparams.add(new BasicNameValuePair("ugfLoginPassword", getPassword()));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        if (httpresponse.getStatusLine().toString().contains("302")) {
            tmp = httpresponse.getLastHeader("Location").getValue();
            NULogger.getLogger().info("UGotFile Login success");
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
        } else {
            NULogger.getLogger().info("UGotFile login failed");
            loginsuccessful = false;
            username = "";
            password = "";
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }

    public void login() {


        try {
            initialize();
            loginUGotFile();
        } catch (Exception ex) {
            Logger.getLogger(UGotFileAccount.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
