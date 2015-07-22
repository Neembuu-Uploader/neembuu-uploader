/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.accounts.NUInvalidPasswordException;
import neembuu.uploader.exceptions.accounts.NUInvalidUserException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author dinesh
 */
public class FileDenAccount extends AbstractAccount {

    private static StringBuilder cookies = null;
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;

    public FileDenAccount() {
        KEY_USERNAME = "fdusername";
        KEY_PASSWORD = "fdpassword";
        HOSTNAME = "FileDen.com";
    }

    public StringBuilder getCookies() {
        return cookies;
    }

    @Override
    public void disableLogin() {
        resetLogin();
        //These code are necessary for account only sites.
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        loginsuccessful = false;
        try {

            cookies = new StringBuilder();
            NULogger.getLogger().info("Trying to log in to fileden.com");
            httpPost = new NUHttpPost("http://www.fileden.com/account.php?action=login");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("action", "login"));
            formparams.add(new BasicNameValuePair("task", "login"));
            formparams.add(new BasicNameValuePair("username", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            NULogger.getLogger().info("Getting cookies........");
            cookies = new StringBuilder(CookieUtils.getAllCookies(httpContext));
            //FileUtils.saveInFile("FileDenAccount.html", stringResponse);
            
            if (CookieUtils.existCookie(httpContext, "uploader_username")) {
                loginsuccessful = true;
                NULogger.getLogger().info("FileDen Login success :)");
                NULogger.getLogger().info(cookies.toString());
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);

                username = getUsername();
                password = getPassword();

            } else {
                //Handle errors
                String error;
                Document doc = Jsoup.parse(stringResponse);
                error = doc.select("div#wrapper table tbody tr td div#sub-content1 div div ul li h3").first().text();
                NULogger.getLogger().log(Level.INFO, "FileDenAccount Login error: {0}", error);
                if("That user does not exist.".equals(error)){
                    throw new NUInvalidUserException(getUsername(), getHOSTNAME());
                }
                
                if("The password you entered is incorrect, OR ...".equals(error)){
                    throw new NUInvalidPasswordException(getUsername(), getHOSTNAME());
                }
                //Generic error
                throw new Exception("FileDen Login failed with this error: "+error);
            }


        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "{0}: Error in FileDen Login  {1}", new Object[]{ getClass().getName(), e});
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
