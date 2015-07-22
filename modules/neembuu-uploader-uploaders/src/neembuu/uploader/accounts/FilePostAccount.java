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
import neembuu.uploader.uploaders.common.StringUtils;
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
import org.json.JSONObject;

/**
 *
 * @author davidepastore
 */
public class FilePostAccount extends AbstractAccount{
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    private String loginURL;
    private String token;

    public FilePostAccount() {
        KEY_USERNAME = "flpstusername";
        KEY_PASSWORD = "flpstpassword";
        HOSTNAME = "FilePost.com";
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

            NULogger.getLogger().info("Trying to log in to FilePost.com");
            httpPost = new NUHttpPost(loginURL);

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("email", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            formparams.add(new BasicNameValuePair("recaptcha_response_field", ""));
            formparams.add(new BasicNameValuePair("token", token));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            NULogger.getLogger().log(Level.INFO, "executing request: {0}", httpPost.getRequestLine());
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            
            
            responseString = EntityUtils.toString(httpResponse.getEntity());
            JSONObject jSonObject = new JSONObject(responseString);
            jSonObject = jSonObject.getJSONObject("js");

            if (jSonObject.has("answer") && jSonObject.getJSONObject("answer").getBoolean("success")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                username = getUsername();
                password = getPassword();
                
                //Check the account type
                responseString = NUHttpClientUtils.getData("http://filepost.com/", httpContext);
                
                if(responseString.contains("Upgrade now &raquo;")){
                    premium = false;
                }
                else{
                    premium = true;
                }
                
                NULogger.getLogger().info("FilePost.com login successful.");
                NULogger.getLogger().log(Level.INFO, "FilePost.com premium? {0}", premium);

            } else {
                //Get error message
                //FileUtils.saveInFile("FilePostAccount.html", responseString);
                
                String error;
                error = jSonObject.getString("error");
                
                if("Incorrect e-mail/password combination".equals(error)){
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

        //NULogger.getLogger().info("Getting startup cookies & link from FilePost.com");
        responseString = NUHttpClientUtils.getData("http://filepost.com", httpContext);
        
        final String sid = CookieUtils.getCookieNameValue(httpContext, "SID");
        
        loginURL = "http://filepost.com/general/login_form/?" + sid + "&JsHttpRequest=" + System.currentTimeMillis() + "0-xml";
        token = StringUtils.stringBetweenTwoStrings(responseString, "token: '", "'");
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
