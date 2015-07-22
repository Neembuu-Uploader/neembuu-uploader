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
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.uploaders.common.StringUtils;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Dinesh
 * @author davidepastore
 */
public class ZShareAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;
    
    private Document doc;

    private static String zsharelink = "";
    private static String zsharedomain;
    private static String xfsscookie;

    public ZShareAccount() {
        KEY_USERNAME = "zsusername";
        KEY_PASSWORD = "zspassword";
        HOSTNAME = "ZShare.ma";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().info("Zshare account disabled..");
    }

    public void loginZShare() throws Exception {
        loginsuccessful = false;

        NULogger.getLogger().info("Trying to log in to zshare.ma");
        httpPost = new NUHttpPost("http://www2.zshares.net");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("op", "login"));
        formparams.add(new BasicNameValuePair("login", getUsername()));
        formparams.add(new BasicNameValuePair("password", getPassword()));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        NULogger.getLogger().info("Getting cookies........");
        
        if(CookieUtils.existCookie(httpContext, "xfss")){
            xfsscookie = CookieUtils.getCookieNameValue(httpContext, "xfss");
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            EntityUtils.consume(httpResponse.getEntity());
            NULogger.getLogger().info("ZShare login ok!");
        }
        
        if (!loginsuccessful) {
            //Find the error string
            String errorMsg = StringUtils.stringStartingFromString(httpResponse.getLastHeader("Location").getValue(), "&msg=");
            NULogger.getLogger().log(Level.INFO, "errorMsg {0}", errorMsg);
            EntityUtils.consume(httpResponse.getEntity());

            if("Incorrect Login or Password".equals(errorMsg)){
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }
            else{
                //Other errors
                throw new Exception("General problem with login in ZShareAccount.");
            }
        }
        

    }

    public String getZsharedomain() {
        return zsharedomain;
    }

    public String getZsharelink() {
        return zsharelink;
    }
    
    
    public String getXfsscookie() {
        return xfsscookie;
    }

    
    public void initialize() throws Exception {
        NULogger.getLogger().info("Getting zshare dynamic upload link");
        httpGet = new NUHttpGet("http://www.zshare.ma/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        //FileUtils.saveInFile("zshareLogin.html", stringResponse);
        
        doc = Jsoup.parse(stringResponse);
        zsharedomain = doc.select("div#div_file form").attr("action");
        zsharelink = zsharedomain + StringUtils.uuid(12, 10) + "&js_on=1&utype=anon&upload_type=file"; 

        NULogger.getLogger().log(Level.INFO, "ZShare Domain {0}", zsharedomain);
        NULogger.getLogger().log(Level.INFO, "ZShare link {0}", zsharelink);

    }

    public void resetLogin(){
        loginsuccessful = false;
        xfsscookie = "";
        zsharedomain = "";
        zsharelink = "";
        username = "";
        password = "";
    }

    
    
    @Override
    public void login() {
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        try {
            initialize();
            loginZShare();
        } catch(NUException ex){
            ex.printError();
            resetLogin();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            NULogger.getLogger().log(Level.SEVERE, "Error in ZShare.net Login: {0}", e);
            resetLogin();
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }
}
