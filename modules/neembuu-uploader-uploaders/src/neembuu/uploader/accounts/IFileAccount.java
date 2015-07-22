/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class IFileAccount extends AbstractAccount {

    public static final String KEY_API_KEY = "ifile_api_key";
    private String loginresponse;
    private static String apikey;

    public IFileAccount() {
        KEY_USERNAME = "ifusername";
        KEY_PASSWORD = "ifpassword";
        HOSTNAME = "IFile.it";
    }

    public static String getApikey() {
        return apikey;
    }

    @Override
    public void disableLogin() {
        properties().setEncryptedProperty(KEY_API_KEY, "");
        loginsuccessful = false;
        //These code are necessary for account only sites.
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        if (!username.isEmpty()) {
            if (username.equals(getUsername()) && password.equals(getPassword())) {
                return;
            } else {
                loginsuccessful = false;
                properties().setEncryptedProperty(KEY_API_KEY, "");
            }
        }

        if (!properties().getEncryptedProperty(KEY_API_KEY).isEmpty()) {
            hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            return;
        }


        try {
            loginsuccessful = false;
            HttpParams params = new BasicHttpParams();
            params.setParameter(
                    "http.useragent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
            DefaultHttpClient httpclient = new DefaultHttpClient(params);

            NULogger.getLogger().info("Trying to log in to ifile.it");
            HttpPost httppost = new HttpPost("https://secure.ifile.it/api-fetch_apikey.api");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("username", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httppost.setEntity(entity);
            HttpResponse httpresponse = httpclient.execute(httppost);
            loginresponse = EntityUtils.toString(httpresponse.getEntity());
            NULogger.getLogger().log(Level.INFO, "response : {0}", loginresponse);
            httpclient.getConnectionManager().shutdown();
            if (loginresponse.contains("akey")) {
                apikey = StringUtils.stringBetweenTwoStrings(loginresponse, "akey\":\"", "\"");
                NULogger.getLogger().info("IFile Login sccuess :)");
                NULogger.getLogger().log(Level.INFO, "API key : {0}", apikey);
                loginsuccessful = true;
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("IFile Login success :)");
                properties().setEncryptedProperty(KEY_API_KEY, apikey);

            } else {
                properties().setEncryptedProperty(KEY_API_KEY, "");
                NULogger.getLogger().info("IFile Login failed :(");
                loginsuccessful = false;
                username = "";
                password = "";
                showWarningMessage( Translation.T().loginerror(), HOSTNAME);
                accountUIShow().setVisible(true);
            }
        } catch (Exception e) {
            NULogger.getLogger().log(Level.SEVERE, "{0}: Error in IFile Login", getClass().getName());

        }
    }
}
