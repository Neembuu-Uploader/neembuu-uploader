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
 * @author dinesh
 */
public class ZohoDocsAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;

    private static StringBuilder zohodocscookies = new StringBuilder();

    public ZohoDocsAccount() {
        KEY_USERNAME = "zdusername";
        KEY_PASSWORD = "zdpassword";
        HOSTNAME = "ZohoDocs.com";
    }

    public static StringBuilder getZohodocscookies() {
        return zohodocscookies;
    }

    @Override
    public void disableLogin() {
        loginsuccessful = false;
        //These code are necessary for account only sites.
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {

        loginsuccessful = false;
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            NULogger.getLogger().info("Trying to log in to Zoho Docs");
            httpPost = new NUHttpPost("https://accounts.zoho.com/login");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("LOGIN_ID", getUsername()));
            formparams.add(new BasicNameValuePair("PASSWORD", getPassword()));
            formparams.add(new BasicNameValuePair("IS_AJAX", "true"));
            formparams.add(new BasicNameValuePair("remember", "-1"));
            formparams.add(new BasicNameValuePair("servicename", "ZohoPC"));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            

            NULogger.getLogger().info("Getting cookies........");
            //CookieUtils.printCookie(httpContext);
            
            zohodocscookies = new StringBuilder(CookieUtils.getAllCookies(httpContext));

            if (CookieUtils.existCookie(httpContext, "IAMAGENTTICKET_un")) {
                NULogger.getLogger().info("Zoho Docs Login Success");
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
            } else {
                //Handle errors
                //FileUtils.saveInFile("ZohoDocsAccount.html", stringResponse);
                if(stringResponse.contains("Invalid username or password")){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
                
                //General errors
                throw new Exception("Generic error: "+stringResponse);
            }
            
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "Error in ZohoDocs Login: {0}", e);
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }


    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
        zohodocscookies = new StringBuilder();
    }
}
