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
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

/**
 *
 * @author dinesh
 * @author davidepastore
 */
public class BoxDotComAccount extends AbstractAccount {

    public static final String KEY_AUTH_TOKEN = "box_auth_token";
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpGet httpGet;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String ticket;
    private String request_token;

    public BoxDotComAccount() {
        KEY_USERNAME = "boxusername";
        KEY_PASSWORD = "boxpassword";
        HOSTNAME = "Box.com";
    }

    public static String getAuth_token() {
        return properties().getEncryptedProperty(KEY_AUTH_TOKEN);
    }

    @Override
    public void disableLogin() {
        properties().setEncryptedProperty(KEY_AUTH_TOKEN, "");
        resetLogin();
        //These code are necessary for account only sites.
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {

        if (!properties().getEncryptedProperty(KEY_AUTH_TOKEN).isEmpty()) {
            hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
            NULogger.getLogger().log(Level.INFO, "BoxDotComAccount already logged.");
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            return;
        }


        try {
            initialize();
            
            responseString = NUHttpClientUtils.getData("https://app.box.com/login/", httpContext);
            //FileUtils.saveInFile("BoxDotComAccount.html", responseString);
            doc = Jsoup.parse(responseString);
            String requestToken = StringUtils.stringBetweenTwoStrings(responseString, "request_token = '", "'");
            
            httpPost = new NUHttpPost("https://app.box.com/login/");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("request_token", requestToken));
            formparams.add(new BasicNameValuePair("login", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());

            if (CookieUtils.existCookie(httpContext, "partial_id")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                NULogger.getLogger().info("Box.com login successful!");

            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
//                FileUtils.saveInFile("BoxDotComAccount.html", responseString);
                doc = Jsoup.parse(responseString);
                String error = doc.select("div.notification_message .notification_title").first().text();
                
                if(error.contains("Invalid login credentials")){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }

                //Generic exception
                throw new Exception("Login error: " + error);
            }

        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
            
        }catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "Error in Box Login: {0}", e);
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }
    
    private void initialize() throws Exception {
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        setCookieLocale();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        //NULogger.getLogger().info("Getting startup cookies & link from Bl.st");
        //responseString = NUHttpClientUtils.getData("https://www.bl.st/", httpContext);
    }
    
    
    /**
     * Set the cookie locale to en_US.
     */
    private void setCookieLocale(){
        cookieStore.addCookie(new BasicClientCookie2("box_locale", "en_US"));
        cookieStore.addCookie(new BasicClientCookie2("country", "US"));
        cookieStore.addCookie(new BasicClientCookie2("lang", "en-US"));
    }

    

    public void loginBox() throws Exception {
        
//https://www.box.net/api/1.0/auth/jtear5a2djtfxs9598apynmea62vko5d
        NULogger.getLogger().log(Level.INFO, "{0}Trying to log in to box.com", getClass());
        httpPost = new NUHttpPost("https://www.box.net/api/1.0/auth/" + ticket);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
//        formparams.add(new BasicNameValuePair("action", "login"));
        formparams.add(new BasicNameValuePair("login", getUsername()));
        formparams.add(new BasicNameValuePair("password", getPassword()));
        formparams.add(new BasicNameValuePair("__login", "1"));
        formparams.add(new BasicNameValuePair("dologin", "1"));
        formparams.add(new BasicNameValuePair("reg_step", ""));
        formparams.add(new BasicNameValuePair("submit1", "1"));
        formparams.add(new BasicNameValuePair("folder", ""));
        formparams.add(new BasicNameValuePair("skip_framework_login", "1"));
        formparams.add(new BasicNameValuePair("login_or_register_mode", "login"));
        formparams.add(new BasicNameValuePair("new_login_or_register_mode", ""));
        formparams.add(new BasicNameValuePair("request_token", request_token));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader(username, ticket);
        httpResponse = httpclient.execute(httpPost, httpContext);
        NULogger.getLogger().log(Level.INFO, "{0} Gonna print the response", getClass());
        responseString = EntityUtils.toString(httpResponse.getEntity());
        
        //FileUtils.saveInFile("BoxDotComAccount.html", stringResponse);
        doc = Jsoup.parse(responseString);
        Element result = doc.select("div.wrapper div.page div.content div.login div.right_column strong.platform_wrong_credentials").first();
        
        //If result Element doesn't exist: login ok!
        if(result == null){
            getUserInfo();
            loginsuccessful = true;
            hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
            username = getUsername();
            password = getPassword();
            NULogger.getLogger().info("Box Login success :)");
        }
        else{
            throw new NUInvalidLoginException(getUsername(), HOSTNAME);
        }
    }

    /**
     * Read information about user. Here you can read other important info.
     * @throws Exception 
     */
    private void getUserInfo() throws Exception {
        //https://www.box.net/api/1.0/rest?action=get_auth_token&api_key=vkf3k5dh0tg1ibvcikjcp8sx0f89d14u&ticket=

        //https://www.box.net/api/1.0/rest?action=get_auth_token&api_key=vkf3k5dh0tg1ibvcikjcp8sx0f89d14u&ticket=xybt9orxzo1xrr5vk4r0axne804y1tpk

        NULogger.getLogger().log(Level.INFO, "{0} Getting auth token value............", getClass());
        httpGet = new NUHttpGet("https://www.box.net/api/1.0/rest?action=get_auth_token&api_key=vkf3k5dh0tg1ibvcikjcp8sx0f89d14u&ticket=" + ticket);
        httpResponse = httpclient.execute(httpGet, httpContext);
        responseString = EntityUtils.toString(httpResponse.getEntity());
        //NULogger.getLogger().log(Level.INFO, "{0}Response : {1}", new Object[]{getClass(), stringResponse});
        
        doc = Jsoup.parse(responseString, "", Parser.xmlParser());
        String auth_token = doc.select("response auth_token").text();

        NULogger.getLogger().log(Level.INFO, "{0} Auth_token : {1}", new Object[]{getClass(), auth_token});
        properties().setEncryptedProperty(KEY_AUTH_TOKEN, auth_token);
    }
    
    
    private void resetLogin(){
        username = "";
        password = "";
        loginsuccessful = false;
    }
    
    
}

/** CASES
 * I. Neembuu Uploader is opened for the first time.
 * II. User logging in for the first time.
 * III. User opens Neembuu Uploader some other time and Box account details are already present.
 * IV. User opens and closes the Account Manager without making any changes to Box details.
 * V. User Disables Login Details
 * VI. User Changes Login Details
 * 
 * 
 * INITIAL REQUIREMENTS:
 * 1. Declare loginsuccessful = false.
 * 2. Declare usernamelocalcopy = "", passwordlocalcopy = "".
 * 
 * 
 * CASE I:
 *      When NeembuuUploader is opened for the first time, there is no BoxUsername 
 * or encrypted BoxPassword or AUTH_TOKEN stored in encrypted format in the 
 * properties file.
 * 
 *      On load, the AccountsManager will call the login method of each plugin 
 * only if it has the associated username or password stored in that file. So 
 * nothing happens now.
 * 
 * CASE II: 
 *      When the user gives login details for the first time, the username and password
 * are stored already in the properties file before this login function is called. Since
 * this is the first time, it means that our usernamelocalcopy and passwordlocalcopy are 
 * empty. So the first if condition is skipped. Also for the same reason, there is no 
 * AUTH_TOKEN previously stored in the properties file, so the second if block is skipped
 * as well.
 * 
 *      The normal login functions proceed. Now there are two possible results:
 *      * Login Success -
 *          1. Store the AUTH_TOKEN in encrypted format in the properties file.
 *          2. Username and password were already stored so never mind.
 *          3. Set usernamelocalcopy and passwordlocalcopy. This is important.
 *          4. Set loginsuccessful = true;
 *      * Login Failure -
 *          1. Clear both localcopies to empty strings.
 *          2. Clear AUTH_TOKEN property in file
 *          3. Set loginsuccessful = false;
 * 
 * CASE III:
 *       Now the localcopies are obviously empty, so the first if block is skipped.
 * But we now have the username, password and token stored in the file, so the second
 * if blocks condition becomes true. In that case, we should set up the local variables.
 *          1. Set up loginsuccessful = true;
 *          2. Set up local copies of username and password.
 *          3. Return without proceeding to login. Because now we have the token, it can
 *              be used to upload without login.
 *          4. Important! Since Login implied to be successful, Enable the checkbox.
 *      
 * 
 * CASE IV:
 *      Username and password local copies shouldn't be empty as they had been stored
 * during previous login (Case II, Case VI) or loaded up (Case III).
 * 
 *      So the first if condition becomes true. Now the local copies are checked
 * against the username and password file properties. They are the same, so nothing
 * happens.. Just return back.
 * 
 * 
 * CASE V:
 *      when the user clears the values in the username and password field, the
 * AcctManager clears the username and password properties and calls the 
 * disableLogin of the plugin. It should clear the local copies and the AUTH_TOKEN
 * property (which was not known for the AcctsMgr to clear) and set loginsuccessful=false.
 * 
 *      The Checkbox must be disabled!!
 * 
 * 
 * CASE VI:
 *      This is the craziest case. The user has already valid details but now enters
 * a different new one. After the user clicks save, the Acct Mgr saves the new username
 * and password in the properties file and calls the login method of the plugin.
 * 
 *      We still have the old username and password in the local copies. Since they are 
 * not empty, the first if condition becomes true. The inner if condition becomes false, 
 * because they are different. localcopy being old and getUsername/Password() being new.
 * so inner else is executed. The login successful is set to false and the old AUTH_TOKEN
 * is cleared.
 * 
 *      Then the usual login functions proceed.(Case II)
 * 
 */
