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
import neembuu.uploader.exceptions.accounts.NUTooManyTries;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.Header;
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
 * @author davidepastore
 */
public class EasyBytezAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String rand;

    public EasyBytezAccount() {
        KEY_USERNAME = "esybytzusername";
        KEY_PASSWORD = "esybytzpassword";
        HOSTNAME = "EasyBytez.com";
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

            NULogger.getLogger().info("Trying to log in to EasyBytez.com");
            httpPost = new NUHttpPost("http://www.easybytez.com/");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("op", "login2"));
            formparams.add(new BasicNameValuePair("login", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            formparams.add(new BasicNameValuePair("rand", rand));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            Header lastHeader = httpResponse.getLastHeader("Location");

            if (lastHeader != null && lastHeader.getValue().contains("op=my_files")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                NULogger.getLogger().info("EasyBytez.com login successful!");

            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("EasyBytezAccount.html", responseString);
                
                Document doc = Jsoup.parse(responseString);
                
                if(responseString.contains("Your IP is temporarily blocked")){
                    throw new NUTooManyTries(getUsername(), HOSTNAME);
                }
                
                final String error = doc.getElementsByClass("err").first().text();
                
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
            e.printStackTrace();
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), e});
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }

    }

    private void initialize() throws Exception {
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        NULogger.getLogger().info("Getting startup cookies & link from EasyBytez.com");
        responseString = NUHttpClientUtils.getData("http://www.easybytez.com/login2.html", httpContext);
        Document doc = Jsoup.parse(responseString);
        rand = doc.select("input[name=rand]").first().val();
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
