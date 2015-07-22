/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.uploaders.common.StringUtils;
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
import org.json.JSONObject;

/**
 *
 * @author Dinesh
 * @author davidepastore
 */
public class BayFilesAccount extends AbstractAccount {
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private JSONObject jSonOjbject;

    public BayFilesAccount() {
        KEY_USERNAME = "bfusername";
        KEY_PASSWORD = "bfpassword";
        HOSTNAME = "BayFiles.com";
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

            NULogger.getLogger().info("Trying to log in to bayfiles.com");
            httpPost = new NUHttpPost("http://bayfiles.net/ajax_login");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("action", "login"));
            formparams.add(new BasicNameValuePair("username", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);

            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            
            //NULogger.getLogger().log(Level.INFO, "BayFiles.com stringResponse: {0}", stringResponse);
            
            jSonOjbject = new JSONObject(stringResponse);
            
            if(jSonOjbject.has("reload")){
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("Bayfiles.com login succeeded :)");
            }            
            else {
                String error = jSonOjbject.getString("error");
                
                if("Login failed. Please try again".equals(error)){
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

        //NULogger.getLogger().info("Getting startup cookies & link from ArabLoads.com");
        //responseString = NUHttpClientUtils.getData("http://arabloads.com", httpContext);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
