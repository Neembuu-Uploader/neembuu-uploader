/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.Calendar;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.exceptions.accounts.NUInvalidPasswordException;
import neembuu.uploader.exceptions.accounts.NUInvalidUserException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.NULogger;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author dinesh
 * @author davidepastore
 */
public class GettAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    private String accessToken;
    private String type;
    private long maxFileSize;

    public GettAccount() {
        KEY_USERNAME = "gtusername";
        KEY_PASSWORD = "gtpassword";
        HOSTNAME = "Ge.tt";
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
            
            long time = Calendar.getInstance().getTimeInMillis();
            NULogger.getLogger().log(Level.INFO,"Trying to log in to Ge.tt: "+"http://ge.tt/u/login?t={0}", time);

            httpPost = new NUHttpPost("http://ge.tt/u/login?t="+time);
            
            //Parameters creation
            JSONObject jSonObject = new JSONObject();
            jSonObject.put("autologin", false);
            jSonObject.put("email", getUsername());
            jSonObject.put("password", getPassword());
            
            StringEntity entity = new StringEntity(jSonObject.toString(), Consts.UTF_8);
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            jSonObject = new JSONObject(responseString);
            
            if (jSonObject.has("accesstoken")) {

                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                
                //Get account additional info
                accessToken = jSonObject.getString("accesstoken");
                type = jSonObject.getString("accesstoken");
                jSonObject = jSonObject.getJSONObject("storage");
                maxFileSize = jSonObject.getLong("free");
                
                //FileUtils.saveInFile("GettAccount.html", responseString);
                //NULogger.getLogger().log(Level.INFO, "Account type : {0}", type);
                NULogger.getLogger().info("Ge.tt login succeeded :)");
            } else {
                
                //Handle errors
                if(jSonObject.has("statusCode")){
                    switch(jSonObject.getInt("statusCode")){
                        case 403:
                            throw new NUInvalidPasswordException(getUsername(), getHOSTNAME());
                        case 404:
                            throw new NUInvalidUserException(getUsername(), getHOSTNAME());
                    }
                }
                
                
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }



        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);

        } catch (Exception e) {
            NULogger.getLogger().log(Level.INFO, "Ge.tt Login failed {0}", e);
            resetLogin();
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }

    }
    
    
    /**
     * Get the max file size.
     * @return the max file size.
     */
    public long getMaxFileSize(){
        return maxFileSize;
    }

    private void resetLogin() {
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
