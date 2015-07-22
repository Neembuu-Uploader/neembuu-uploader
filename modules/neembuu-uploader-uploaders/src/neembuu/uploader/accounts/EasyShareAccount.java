/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author vigneshwaran
 */
public class EasyShareAccount extends AbstractAccount {

    public EasyShareAccount() {
        KEY_USERNAME = "esusername";
        KEY_PASSWORD = "espassword";
        HOSTNAME = "Easy-Share.com";
    }

    @Override
    public void login() {
        try {
            loginsuccessful = false;
            HttpParams params = new BasicHttpParams();
            params.setParameter(
                    "http.useragent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
            DefaultHttpClient eshttpclient = new DefaultHttpClient(params);

            NULogger.getLogger().info("Trying to log in to EasyShare");
            HttpPost httppost = new HttpPost("http://www.easy-share.com/accounts/login");
            httppost.setHeader("Referer", "http://www.easy-share.com/");
            httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("login", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            formparams.add(new BasicNameValuePair("remember", "1"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            ////////////////////////////////////////////////////////////////////
            httppost.setEntity(entity);
            HttpResponse httpresponse = eshttpclient.execute(httppost);
            //Check success
            if (httpresponse.getFirstHeader("Set-Cookie") == null) {
                NULogger.getLogger().info("Easy-share login not successful");
                loginsuccessful = false;
                username = "";
                password = "";
                showWarningMessage( Translation.T().loginerror(), HOSTNAME);
                accountUIShow().setVisible(true);
            } else {
                Iterator<Cookie> it = eshttpclient.getCookieStore().getCookies().iterator();
                Cookie escookie;
                while (it.hasNext()) {
                    escookie = it.next();
                    if (escookie.getName().equals("logacc") && escookie.getValue().equals("1")) {
                        NULogger.getLogger().info("EasyShare login successful");
                        loginsuccessful = true;
                        username = getUsername();
                        password = getPassword();
                        break;
                    }
                }
                if (!loginsuccessful) {
                    showWarningMessage( Translation.T().loginerror(), HOSTNAME);
                    accountUIShow().setVisible(true);
                }
            }
            EntityUtils.consume(httpresponse.getEntity());
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.SEVERE, "{0}: Error in $EasyShare Login", getClass().getName());
        }
    }

    public void disableLogin() {
        loginsuccessful = false;
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }
}
