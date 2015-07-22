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
public class UpStoreAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;

    public UpStoreAccount() {
        KEY_USERNAME = "upstoreusername";
        KEY_PASSWORD = "upstorepassword";
        HOSTNAME = "UpStore.net";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();

            NULogger.getLogger().info("Trying to log in to UpStore.net");
            httpPost = new NUHttpPost("http://upstore.net/account/login/");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("send", "Login"));
            formparams.add(new BasicNameValuePair("email", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            formparams.add(new BasicNameValuePair("url", "http%3A%2F%2Fupstore.net%2F"));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            
            if (httpResponse != null && httpResponse.toString().contains("usid=")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("UpStore.net login successful!");

            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("UpStoreAccount.html", responseString);
                Document doc = Jsoup.parse(responseString);
                String error = doc.select("span[class=error]").first().text();
                
                if("Wrong email or password.".equals(error)){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }
                
                if(error.contains("captcha")) {
                    throw new Exception("Login error: Too many failed login attempts (captcha)");
                }

                //Generic exception
                throw new Exception("Login error: Login Failed");
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

        NULogger.getLogger().info("Getting startup cookies & link from UpStore.net");
        responseString = NUHttpClientUtils.getData("http://upstore.net", httpContext);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
