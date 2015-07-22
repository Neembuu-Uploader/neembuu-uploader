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
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author dinesh
 */
public class SugarSyncAccount extends AbstractAccount {

    private String AUTH_API_URL = "https://api.sugarsync.com/authorization";
    
    /* The User-Agent HTTP Request header's value  */
    /* The template used for creating the request  */
    private String AUTH_REQUEST_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<authRequest>"
            + "<username>%s</username>"
            + "<password>%s</password>"
            + "<accessKeyId>%s</accessKeyId>"
            + "<privateAccessKey>%s</privateAccessKey>"
            + "</authRequest>";
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private HttpEntity httpEntity;
    private String stringResponse;
    private String location;
    
    private static String auth_token = "";

    public SugarSyncAccount() {
        KEY_USERNAME = "sugarusername";
        KEY_PASSWORD = "sugarpassword";
        HOSTNAME = "SugarSync.com";

    }

    public String getAuth_token() {
        return auth_token;
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
        try {
            
            String AUTH_REQUEST = String.format(AUTH_REQUEST_TEMPLATE, getUsername(), getPassword(), "MTc5MjY5ODEzMzI1MDQ0MDQwMjQ", "Mjc5NDgxOWU3ZjRmNGQxODgzMzY4N2QyNGUxN2VkODE");
            NULogger.getLogger().info("Trying to login into SugarSync.........");
            
            httpPost = new NUHttpPost(AUTH_API_URL);
            httpPost.setEntity(new StringEntity(AUTH_REQUEST, ContentType.TEXT_XML)); //Send a XML file
            httpResponse = httpclient.execute(httpPost);
            httpEntity = httpResponse.getEntity();
            stringResponse = EntityUtils.toString(httpEntity);
            
            //FileUtils.saveInFile("SugarSyncAccount.xml", stringResponse);
            
            if(httpResponse.getStatusLine().getStatusCode() == 401){
                Document doc;
                String error;
                doc = Jsoup.parse(stringResponse);
                error = doc.select("h3").text();
                
                if("invalid user credentials".equals(error)){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
                
                //Generic exception
                throw new Exception("401 HTTP code: "+stringResponse);
            }
            
            location = httpResponse.getLastHeader("Location").getValue();
            
            NULogger.getLogger().log(Level.INFO, "Location: {0}", location);
            
            if(location.contains(AUTH_API_URL)){
                NULogger.getLogger().info("SugarSync login successful :)");
                auth_token = location;
                loginsuccessful = true;
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                username = getUsername();
                password = getPassword();
            }
            else{
                throw new Exception("It doesn't contain the "+AUTH_API_URL);
            }
            

        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.INFO, "SugarSync Login failed: {0}", e);
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }
    
    private void resetLogin(){
        auth_token = "";
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
