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
import neembuu.uploader.utils.NUHttpClientUtils;

/**
 *
 * @author Paralytic
 */
public class VideoWoodAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String vdwood_token = "";
    
    private Document doc;

    public VideoWoodAccount() {
        KEY_USERNAME = "vdwoodusername";
        KEY_PASSWORD = "vdwoodpassword";
        HOSTNAME = "VideoWood.tv";
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
            
            NULogger.getLogger().info("Trying to log in to VideoWood.tv");
            httpPost = new NUHttpPost("http://videowood.tv/login");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("_token", vdwood_token));
            formparams.add(new BasicNameValuePair("username", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            
            responseString = NUHttpClientUtils.getData("http://videowood.tv", httpContext);
            
            if (responseString.contains(getUsername())){
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("VideoWood.tv login successful!");
            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("VideoWoodAccount.html", responseString);
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

        NULogger.getLogger().info("Getting startup cookies & link from VideoWood.tv");
        responseString = NUHttpClientUtils.getData("http://videowood.tv/login", httpContext);
        doc = Jsoup.parse(responseString);
	vdwood_token = doc.select("form").select("input[name=_token]").attr("value");
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
