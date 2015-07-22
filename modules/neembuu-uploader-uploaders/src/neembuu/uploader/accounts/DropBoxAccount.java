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
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.NUHttpClientUtils;
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
 * @author Paralytic
 */
public class DropBoxAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    private Document doc;
    private String cont = "";
    private String signup_data = "";
    private String signup_tag = "";
    private String recaptcha_resp = "";
    private String recaptcha_key = "";
    private String db_t_val = "";

    public DropBoxAccount() {
        KEY_USERNAME = "dbusername";
        KEY_PASSWORD = "dbpassword";
        HOSTNAME = "DropBox.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();

            NULogger.getLogger().info("Trying to log in to DropBox.com");
            httpPost = new NUHttpPost("https://www.dropbox.com/ajax_login");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("cont", cont));
            formparams.add(new BasicNameValuePair("is_xhr", "true"));
            formparams.add(new BasicNameValuePair("login_email", getUsername()));
            formparams.add(new BasicNameValuePair("login_password", getPassword()));
            formparams.add(new BasicNameValuePair("recaptcha_public_key", recaptcha_key));
            formparams.add(new BasicNameValuePair("recaptcha_response_field", recaptcha_resp));
            formparams.add(new BasicNameValuePair("remember_me", "True"));
            formparams.add(new BasicNameValuePair("signup_data", signup_data));
            formparams.add(new BasicNameValuePair("signup_tag", signup_tag));
            formparams.add(new BasicNameValuePair("t", db_t_val));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            
            responseString = EntityUtils.toString(httpResponse.getEntity());

            if (responseString.contains(getUsername()) && !CookieUtils.getCookieValue(httpContext, "bjar").isEmpty()) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                NULogger.getLogger().info("DropBox.com login successful!");

            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("DropBoxAccount.html", responseString);
                doc = Jsoup.parse(responseString);
                String error = doc.select(".err").first().text();
                
                if("Incorrect Login or Password".equals(error)){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }

                //Generic exception
                throw new Exception("Login error: " + error);
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), e});
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }

    }

    private void initialize() throws Exception {
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        NULogger.getLogger().info("Getting startup cookies & link from DropBox.com");
        responseString = NUHttpClientUtils.getData("https://www.dropbox.com/login", httpContext);
        doc = Jsoup.parse(responseString);
        
        cont = doc.select("form").first().select("input[name=cont]").attr("value");
        signup_data = doc.select("form").first().select("input[name=signup_data]").attr("value");
        signup_tag = doc.select("form").first().select("input[name=signup_data]").attr("value");
        recaptcha_resp = doc.select("form").first().select("input[name=recaptcha_response_field]").attr("value");
        recaptcha_key = doc.select("form").first().select("input[name=recaptcha_public_key]").attr("value");
        db_t_val = doc.select("input[name=t]").attr("value");
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
