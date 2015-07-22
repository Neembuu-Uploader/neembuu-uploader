/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

/**
 *
 * @author dinesh
 */
public class ScribdAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpGet httpGet;
    private String stringResponse;
    private String getURL;
    
    private Document doc;
    private String status;

    private String session_key = "";
    private String SCRIBD_API_KEY = "5t8cd1k0ww3iupw31bb2a";
    private String SCRIBD_API_SIGNATURE = "sec-b27x1xqgbvhrtccudzeh0s709n";
    private String SCRIBD_LOGIN_URL = "http://api.scribd.com/api?method=user.login&username=%s&password=%s&api_key=" + SCRIBD_API_KEY + "&api_sig=" + SCRIBD_API_SIGNATURE;
    private String SCRIBD_SESSION_KEY = "scribd_session_key";

    public ScribdAccount() {

        KEY_USERNAME = "scusername";
        KEY_PASSWORD = "scpassword";
        HOSTNAME = "Scribd.com";

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
        if (!username.isEmpty()) {
            if (username.equals(getUsername()) && password.equals(getPassword())) {
                return;
            } else {
                loginsuccessful = false;
                properties().setEncryptedProperty(SCRIBD_SESSION_KEY, "");
            }
        }


        if (!properties().getEncryptedProperty(SCRIBD_SESSION_KEY).isEmpty()) {
            hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            NULogger.getLogger().info("Already Login in to Scribd.com!");
            return;
        }

        loginsuccessful = false;
        try {
            getURL = String.format(SCRIBD_LOGIN_URL, getUsername(), getPassword());
            httpGet = new NUHttpGet(getURL);
            httpResponse = httpclient.execute(httpGet);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(stringResponse, "", Parser.xmlParser());
            status = doc.select("rsp").attr("stat");
            //FileUtils.saveInFile("ScribdAccount.xml", stringResponse);
            
            //NULogger.getLogger().log(Level.INFO, "Status:  : {0}", status);


            if ("ok".equals(status)) {
                session_key = doc.select("rsp session_key").text();
                NULogger.getLogger().log(Level.INFO, "session_key  : {0}", session_key);
                properties().setEncryptedProperty(SCRIBD_SESSION_KEY, session_key);
                loginsuccessful = true;
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("Scribd Login success :)");
            } else {
                //Handle errors: http://www.scribd.com/developers/platform/api/user_login
                int errorCode;
                
                errorCode = Integer.parseInt(doc.select("rsp error").attr("code"));
                
                if(errorCode == 401 || errorCode == 601 || errorCode == 613){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
                else{
                    String errorMessage;
                    errorMessage = doc.select("rsp error").attr("message");
                    throw new Exception("Scribd login error [" + errorCode + "]: " + errorMessage);
                }
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "Error in Scribd Login: {0}", e);
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);

        }
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        properties().setEncryptedProperty(SCRIBD_SESSION_KEY, "");
        username = "";
        password = "";
        session_key = "";
    }
}
