/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.IntegerUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author Dinesh
 */
public class RapidShareAccount extends AbstractAccount {

    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;
    
    private static String rscookie;

    public RapidShareAccount() {
        KEY_USERNAME = "rsusername";
        KEY_PASSWORD = "rspassword";
        HOSTNAME = "RapidShare.com";
    }

    public static String getRscookie() {
        return rscookie;
    }

    @Override
    public void disableLogin() {
        resetLogin();
        //These code are necessary for account only sites.
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        try {
            httpGet = new NUHttpGet("http://api.rapidshare.com/cgi-bin/rsapi.cgi?sub=getaccountdetails&login=" + getUsername() + "&password=" + getPassword() + "&withpublicid=3&withcookie=1");
            
            httpResponse = httpclient.execute(httpGet, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            //NULogger.getLogger().log(Level.INFO, "String response: {0}", stringResponse);
            
            if (stringResponse.contains("ERROR")) {
                //Handle errors
                if(stringResponse.contains("Login failed. Password incorrect or account not found.")){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
                
                //Generic error
                throw new Exception("RapidShare Login with error :"+stringResponse);
            } else {
                //Getting cookie
                stringResponse = toJSON(stringResponse);
                JSONObject jsonObject = new JSONObject(stringResponse);

                rscookie = jsonObject.getString("cookie");
                NULogger.getLogger().log(Level.INFO, "cookie : {0}", rscookie);

                loginsuccessful = true;
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);

                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("RapidShare Login success :)");
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "Error in RapidShare Login \n Exception: {0}", e);
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }

    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
    
    /**
     * Edit the string response and transforms it into a well-formed json string.
     * @param string the response string.
     * @return A well-formed json string.
     */
    private String toJSON(String string) throws IOException{
        BufferedReader bufReader = new BufferedReader(new StringReader(string));
        String keyValueSeparator = ":";
        String keySeparator = ",";
        String returnString = "";
        
        int equalIndex;
        int lastCommaIndex;
        String keyValue;
        String keyName;
        String line = null;
        while( (line=bufReader.readLine()) != null ){
            equalIndex = line.indexOf("=");
            
            //Key value
            keyValue = line.substring(equalIndex+1);
            if(!IntegerUtils.isInteger(keyValue)){
                keyValue = "\""+keyValue+"\"";
            }
            
            //Key name
            keyName = line.substring(0, equalIndex);
            //NULogger.getLogger().info(keyName+": "+keyValue);
            returnString += keyName + keyValueSeparator + keyValue + keySeparator;
        }
        //Deleting last comma
        lastCommaIndex = returnString.lastIndexOf(",");
        returnString = returnString.substring(0, lastCommaIndex);
        
        //Adding braces
        returnString = "{" + returnString + "}";
        
        return returnString;
    }
}
