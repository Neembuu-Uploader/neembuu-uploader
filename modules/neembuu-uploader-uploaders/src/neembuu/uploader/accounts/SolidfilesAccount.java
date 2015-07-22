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
import neembuu.uploader.utils.CookieUtils;
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

/**
 *
 * @author davidepastore
 */
public class SolidfilesAccount extends AbstractAccount{
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private String location;
    
    private String loginURL = "https://www.solidfiles.com/login/";

    public SolidfilesAccount() {
        KEY_USERNAME = "sldfusername";
        KEY_PASSWORD = "sldfpassword";
        HOSTNAME = "Solidfiles.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        
        try{
            
            NULogger.getLogger().info("Trying to log in to Solidfiles.com");
            NUHttpClientUtils.getData(loginURL, httpContext);
            
            
            
            httpPost = new NUHttpPost(loginURL);
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            
            formparams.add(new BasicNameValuePair("csrfmiddlewaretoken", CookieUtils.getCookieValue(httpContext, "csrftoken")));
            formparams.add(new BasicNameValuePair("next", ""));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            formparams.add(new BasicNameValuePair("username", getUsername()));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);

            
            if(httpResponse.getFirstHeader("Location") == null){
                stringResponse = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("SolidFilesAccount.html", stringResponse);
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }
            
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            NULogger.getLogger().info("Solidfiles Login Success!");
            
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "Error in Solidfiles Login {0}", e);
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
