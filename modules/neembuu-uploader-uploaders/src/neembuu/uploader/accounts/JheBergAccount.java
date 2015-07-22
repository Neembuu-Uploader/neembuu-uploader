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
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
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
import neembuu.uploader.utils.NUHttpClientUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Paralytic
 */
public class JheBergAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    private Document doc;
    private String csrfmiddlewaretoken;

    public JheBergAccount() {
        KEY_USERNAME = "jhebergusername";
        KEY_PASSWORD = "jhebergpassword";
        HOSTNAME = "JheBerg.net";
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

            NULogger.getLogger().info("** JheBerg.net ** => Attempting to login now ...");
            httpPost = new NUHttpPost("http://jheberg.net/login/");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("csrfmiddlewaretoken", csrfmiddlewaretoken));
            formparams.add(new BasicNameValuePair("username", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            formparams.add(new BasicNameValuePair("remember_me", "on"));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            responseString = EntityUtils.toString(httpResponse.getEntity());
            responseString = NUHttpClientUtils.getData("http://jheberg.net/", httpContext);
            
            if (responseString.contains("Logout")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("** JheBerg.net ** => Logged in successfully!");

            } else {
                //Generic exception
                throw new Exception("** JheBerg.net ** => Login error: Either login credentials were incorrect or this plugin is broken");
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

        NULogger.getLogger().info("** JheBerg.net ** => Initiating plugin, please wait ...");
        responseString = NUHttpClientUtils.getData("http://jheberg.net/", httpContext);
        doc = Jsoup.parse(responseString);
        
        csrfmiddlewaretoken = doc.select("form").first().select("input[name=csrfmiddlewaretoken]").attr("value");
        if (!csrfmiddlewaretoken.isEmpty()){
            NULogger.getLogger().info("** JheBerg.net ** => Login token found successfully, proceeding ...");
        } else {
            NULogger.getLogger().info("** JheBerg.net ** => Error! Unable to find the login token!");
        }
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
