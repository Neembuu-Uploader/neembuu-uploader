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
 * @author Dinesh
 */
public class UpBoothAccount extends AbstractAccount {

    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private CookieStore cookieStore;
    private NUHttpPost httpPost;
    private String sessionCookie = "";
    

    public UpBoothAccount() {
        KEY_USERNAME = "ubthusername";
        KEY_PASSWORD = "ubthpassword";
        HOSTNAME = "UpBooth.com";
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            loginsuccessful = false;
            NULogger.getLogger().info("Trying to log in to upbooth.com");

            httpPost = new NUHttpPost("http://upbooth.com/login.php?aff=1db2f3b654350bf4");


            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
//        formparams.add(new BasicNameValuePair("action", "login"));
            formparams.add(new BasicNameValuePair("loginUsername", getUsername()));
            formparams.add(new BasicNameValuePair("loginPassword", getPassword()));
            formparams.add(new BasicNameValuePair("submit", "Sign in"));
            formparams.add(new BasicNameValuePair("submitme", "1"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            HttpResponse httpresponse = httpclient.execute(httpPost, httpContext);
            if (httpresponse.getFirstHeader("Location") != null) {

                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                //Header[] allHeaders = httpresponse.getAllHeaders();
                //sessionCookie = "";
                //for (int i = 0; i < allHeaders.length; i++) {
                //    if (allHeaders[i].getValue().contains("filehosting")) {
                //        sessionCookie = allHeaders[i].getValue();
                //     }
                //}
                
                EntityUtils.consume(httpresponse.getEntity()); //Consume content
                
                //Get account additional info
                String response = NUHttpClientUtils.getData("http://upbooth.com/compare.php", httpContext);
                Document doc = Jsoup.parse(response);
                String accountType = doc.select("table.accountStateTable tbody tr td").eq(1).text();
                if(accountType.contains("Paid User")){
                    premium = true;
                    //NULogger.getLogger().info("It's a premium account.");
                }
                else {
                    premium = false;
                }
                
                //FileUtils.saveInFile("UpBoothAccount.html", EntityUtils.toString(httpresponse.getEntity()));
                //NULogger.getLogger().log(Level.INFO, "Session Cookie : {0}", sessionCookie);
                NULogger.getLogger().info("UpBooth.com login succeeded :)");
            } else {
                EntityUtils.consume(httpresponse.getEntity());
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }



        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);

        } catch (Exception e) {
            NULogger.getLogger().log(Level.INFO, "UpBooth.com Login failed {0}", e);
            resetLogin();
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
        sessionCookie = "";
    }
}
