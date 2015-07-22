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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NUHttpClientUtils;

/**
 *
 * @author Paralytic
 */
public class MediaFireAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    private String signature = "";
    public String sessionToken = "";

    public MediaFireAccount() {
        KEY_USERNAME = "mfusername";
        KEY_PASSWORD = "mfpassword";
        HOSTNAME = "MediaFire.com";
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

            NULogger.getLogger().info("** MediaFire.com ** => Retrieving the session token ...");
            httpPost = new NUHttpPost("https://www.mediafire.com/api/1.3/user/get_session_token.php");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("application_id", "45283"));
            // email + password + application_id
            signature = getUsername() + getPassword() + "45283";
            formparams.add(new BasicNameValuePair("signature", stringToSha(signature)));
            formparams.add(new BasicNameValuePair("email", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            sessionToken = StringUtils.stringBetweenTwoStrings(responseString, "<session_token>", "</session_token>");
            if (!sessionToken.isEmpty()) {
                NULogger.getLogger().info("** MediaFire.com ** => Received the session token successfully.");
            }
            responseString = StringUtils.stringBetweenTwoStrings(responseString, "<result>", "</result>");

            if (responseString.equals("Success")) {
                responseString = "http://www.mediafire.com/api/1.3/user/get_info.php?session_token=" +sessionToken;
                responseString = NUHttpClientUtils.getData(responseString, httpContext);
                responseString = StringUtils.stringBetweenTwoStrings(responseString, "<premium>", "</premium>");
                
                if (responseString.equals("yes")){
                    premium = true;
                    NULogger.getLogger().info("** MediaFire.com ** => Premium account detected.");
                }
                
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                NULogger.getLogger().info("** MediaFire.com ** => Logged in successfully!");
            } else {
                //Get error message
                String error = responseString;
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

        //NULogger.getLogger().info("Getting startup cookies & link from MediaFire.com");
        //responseString = NUHttpClientUtils.getData("MediaFire.com", httpContext);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
    
    private static String stringToSha(String inputStr)
    {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(inputStr.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return sha1;
    }

    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}