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
public class MultiUploadDotBizAccount extends AbstractAccount{
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;

    public MultiUploadDotBizAccount() {
        KEY_USERNAME = "mudbusername";
        KEY_PASSWORD = "mudbpassword";
        HOSTNAME = "MultiUpload.biz";
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

            NULogger.getLogger().info("Trying to log in to MultiUpload.biz");
            httpPost = new NUHttpPost("http://multiupload.biz/login");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("q", "login_reg"));
            formparams.add(new BasicNameValuePair("usr_login", getUsername()));
            formparams.add(new BasicNameValuePair("usr_password", getPassword()));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            Header lastHeader = httpResponse.getLastHeader("Location");

            if (lastHeader != null && lastHeader.getValue().contains("my_account")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("MultiUpload.biz login successful!");

            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("MultiUploadDotBizAccount.html", responseString);
                Document doc = Jsoup.parse(responseString);
                String error = doc.select("#login_form .err").first().text();
                
                if("Incorrect Login or Password".equals(error)){
                    throw new NUInvalidUserException(getUsername(), HOSTNAME);
                }
                
                if("Wrong password".equals(error)){
                    throw new NUInvalidPasswordException(getUsername(), HOSTNAME);
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

        //NULogger.getLogger().info("Getting startup cookies & link from MultiUpload.biz");
        //responseString = NUHttpClientUtils.getData("http://multiupload.biz", httpContext);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
 
