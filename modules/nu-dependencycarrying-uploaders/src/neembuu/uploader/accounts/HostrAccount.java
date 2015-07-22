/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.logging.Level;
import javax.swing.JOptionPane;

//import neembuu.uploader.NeembuuUploader;
import neembuu.uploader.translation.Translation;

import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.uploaders.api._hostr.HostrApi;
import neembuu.uploader.uploaders.api._hostr.HostrApiBuilder;
import neembuu.uploader.utils.NULogger;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * If you have some problems with this plugin, take a look to <a href="https://hostr.co/developer">developers page</a>.
 * @author dinesh
 * @author davidepastore
 */
public class HostrAccount extends AbstractAccount {

    private DefaultHttpClient httpclient = (DefaultHttpClient) NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpGet httpGet;
    private String stringResponse;
    private long maxFileSize;
    private int dailyUploadAllowance;

    public HostrAccount() {
        KEY_USERNAME = "lhrsername";
        KEY_PASSWORD = "lhrpassword";
        HOSTNAME = "Hostr.co";
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
            httpGet = new NUHttpGet("https://api.hostr.co/user");
            
            //Basic Auth
            String credentials = getUsername()+ ":" + getPassword();
            String basicAuth = Base64.encodeBase64String(credentials.getBytes());
            httpGet.setHeader("Authorization", "Basic " + basicAuth);
            httpResponse = httpclient.execute(httpGet);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            //NULogger.getLogger().log(Level.SEVERE, "Response: {0}", stringResponse);
            
            JSONObject jsonObj = new JSONObject(stringResponse);
            if(jsonObj.has("error")){
                //Handle errors
                HostrApiBuilder localhostrApiBuilder = new HostrApiBuilder();
                HostrApi localhostrApi = localhostrApiBuilder
                        .setHostname(HOSTNAME)
                        .setJSONObject(jsonObj)
                        .setUsername(username)
                        .build();
                localhostrApi.handleErrors();
            }
            else{
                //Read info
                maxFileSize = jsonObj.getLong("max_filesize");
                dailyUploadAllowance = jsonObj.getInt("daily_upload_allowance");
                loginsuccessful = true;
                NULogger.getLogger().info("Hostr Login Success");
                NULogger.getLogger().log(Level.INFO, "Maxfilesize: {0}", maxFileSize);
                NULogger.getLogger().log(Level.INFO, "Daily Upload Allowance: {0}", dailyUploadAllowance);
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                username = getUsername();
                password = getPassword();
            }

        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);

        } catch (Exception ex) {
            NULogger.getLogger().log(Level.SEVERE, "Hostr Login Exception: {0}",ex);
            resetLogin();

            showWarningMessage(Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }
    
    /**
     * Get max file size.
     * @return max file size.
     */
    public long getMaxFileSize(){
        return maxFileSize;
    }
    
    /**
     * Get daily upload allowance.
     * @return daily upload allowance.
     */
    public int getDailyUploadAllowance(){
        return dailyUploadAllowance;
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
